package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val id: String,
    val userId: String,
    val groupId: String,
    val role: String,
    val user: User? = null
)
