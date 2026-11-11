package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.BuildConfig
import com.chandan.askfaculty.data.local.SessionManager
import com.chandan.askfaculty.data.remote.model.ApiResponse
import com.chandan.askfaculty.data.remote.model.RefreshRequest
import com.chandan.askfaculty.data.remote.model.TokenPair
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val json: Json
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        var priorCount = 0
        var prior = response.priorResponse
        while (prior != null) {
            priorCount++
            prior = prior.priorResponse
        }
        if (priorCount >= 2) {
            return null
        }

        val refreshToken = sessionManager.refreshToken ?: return null

        return runBlocking {
            mutex.withLock {
                val currentAccessToken = sessionManager.accessToken
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                if (!requestToken.isNullOrBlank() && requestToken != currentAccessToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .build()
                }

                try {
                    val rawBase = BuildConfig.API_BASE_URL.removeSuffix("/")
                    val apiBase = if (rawBase.endsWith("/api/v1")) "$rawBase/" else "$rawBase/api/v1/"

                    val refreshBody = json.encodeToString(RefreshRequest.serializer(), RefreshRequest(refreshToken))
                        .toRequestBody("application/json".toMediaType())

                    val refreshRequest = Request.Builder()
                        .url("${apiBase}auth/refresh")
                        .post(refreshBody)
                        .build()

                    val client = OkHttpClient.Builder().build()
                    val refreshResponse = client.newCall(refreshRequest).execute()

                    if (refreshResponse.isSuccessful) {
                        val responseBody = refreshResponse.body?.string()
                        if (responseBody != null) {
                            val apiResponse = json.decodeFromString(ApiResponse.serializer(TokenPair.serializer()), responseBody)
                            if (apiResponse.success && apiResponse.data != null) {
                                val tokenPair = apiResponse.data
                                sessionManager.saveSession(tokenPair.accessToken, tokenPair.refreshToken)
                                return@withLock response.request.newBuilder()
                                    .header("Authorization", "Bearer ${tokenPair.accessToken}")
                                    .build()
                            }
                        }
                    }

                    sessionManager.clearSession()
                    null
                } catch (e: Exception) {
                    sessionManager.clearSession()
                    null
                }
            }
        }
    }
}
