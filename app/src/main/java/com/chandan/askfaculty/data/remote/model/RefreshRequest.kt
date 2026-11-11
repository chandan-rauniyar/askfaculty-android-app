package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
