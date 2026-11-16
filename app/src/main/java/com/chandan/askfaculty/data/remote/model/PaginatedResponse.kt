package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val page: Int,
    val limit: Int,
    val total: Int,
    val items: List<T>
)
