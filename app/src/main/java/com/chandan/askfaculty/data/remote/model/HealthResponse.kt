package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val checks: Map<String, String> = emptyMap()
)
