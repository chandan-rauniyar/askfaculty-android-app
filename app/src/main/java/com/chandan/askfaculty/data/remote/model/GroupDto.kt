package com.chandan.askfaculty.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SectionDto(val id: String = "", val name: String = "")

@Serializable
data class DepartmentDto(val id: String? = null, val name: String? = null)

@Serializable
data class SubjectDto(
    val id: String = "",
    val code: String? = null,
    val name: String = "",
    val department: DepartmentDto? = null,
)

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: String = "",
    val status: String? = null,
    val chatMode: String? = null,
    val sectionId: String? = null,
    val subjectId: String? = null,
    val section: SectionDto? = null,
    val subject: SubjectDto? = null,
)

@Serializable
data class UnreadItem(val groupId: String, val count: Int = 0)

@Serializable
data class UnreadSummaryDto(val items: List<UnreadItem> = emptyList())

@Serializable
data class CountDto(val count: Int = 0)

enum class UserRole { STUDENT, FACULTY, COLLEGE_ADMIN }

enum class GroupFilter(val label: String) {
    All("All"),
    Classes("Classes"),
    Subjects("Subjects"),
    Other("Other")
}

data class GroupsUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val groups: List<GroupDto> = emptyList(),
    val unreadByGroup: Map<String, Int> = emptyMap(),
    val bellCount: Int = 0,
    val filter: GroupFilter = GroupFilter.All,
) {
    val totalUnread: Int get() = unreadByGroup.values.sum()
}
