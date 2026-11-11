package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = sessionManager.accessToken

        val requestBuilder = originalRequest.newBuilder()
        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
