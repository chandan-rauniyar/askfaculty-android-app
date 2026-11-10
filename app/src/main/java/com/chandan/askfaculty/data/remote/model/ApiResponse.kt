package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorResponse? = null,
    val meta: ApiMeta = ApiMeta()
)

@Serializable
data class ApiErrorResponse(
    val code: String,
    val message: String,
    val details: List<ApiErrorDetail> = emptyList()
)

@Serializable
data class ApiErrorDetail(
    val field: String? = null,
    val message: String
)

@Serializable
data class ApiMeta(
    val requestId: String? = null
)
