package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ApiResponse
import com.chandan.askfaculty.data.remote.model.AuthSession
import com.chandan.askfaculty.data.remote.model.LoginRequest
import com.chandan.askfaculty.data.remote.model.RefreshRequest
import com.chandan.askfaculty.data.remote.model.TokenPair
import com.chandan.askfaculty.data.remote.model.User
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthSession>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): ApiResponse<TokenPair>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest): ApiResponse<JsonObject>

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<User>
}
