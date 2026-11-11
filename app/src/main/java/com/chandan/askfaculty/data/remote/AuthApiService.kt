package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ActivateRequest
import com.chandan.askfaculty.data.remote.model.ApiResponse
import com.chandan.askfaculty.data.remote.model.AuthSession
import com.chandan.askfaculty.data.remote.model.EmailRequest
import com.chandan.askfaculty.data.remote.model.LoginRequest
import com.chandan.askfaculty.data.remote.model.RefreshRequest
import com.chandan.askfaculty.data.remote.model.ResetPasswordRequest
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

    @POST("auth/activate")
    suspend fun activate(@Body request: ActivateRequest): ApiResponse<JsonObject>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: EmailRequest): ApiResponse<JsonObject>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse<JsonObject>

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<User>
}
