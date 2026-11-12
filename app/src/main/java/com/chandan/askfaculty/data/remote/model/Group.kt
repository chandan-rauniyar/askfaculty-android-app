package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    val type: String,
    val departmentId: String? = null,
    val programId: String? = null,
    val batchId: String? = null,
    val sectionId: String? = null,
    val subjectId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class UnreadSummary(
    val unreadQuestionsCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val groups: Map<String, GroupUnreadDetail> = emptyMap()
)

@Serializable
data class GroupUnreadDetail(
    val unreadQuestions: Int = 0,
    val unreadMessages: Int = 0
)
