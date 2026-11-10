package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ApiResponse
import com.chandan.askfaculty.data.remote.model.HealthResponse
import retrofit2.http.GET

interface HealthApiService {
    @GET("health/ready")
    suspend fun getHealthReady(): ApiResponse<HealthResponse>
}
