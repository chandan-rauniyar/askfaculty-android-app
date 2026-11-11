package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User
)
