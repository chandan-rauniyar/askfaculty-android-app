package com.chandan.askfaculty.data.repository

import com.chandan.askfaculty.data.remote.GroupApiService
import com.chandan.askfaculty.data.remote.model.Group
import com.chandan.askfaculty.data.remote.model.GroupDto
import com.chandan.askfaculty.data.remote.model.GroupMember
import com.chandan.askfaculty.data.remote.model.UnreadSummary
import com.chandan.askfaculty.data.remote.unwrap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

data class GroupsSnapshot(
    val groups: List<GroupDto>,
    val unreadByGroup: Map<String, Int>,
    val bellCount: Int,
)

@Singleton
class GroupRepository @Inject constructor(
    private val groupApiService: GroupApiService
) {
    suspend fun load(): Result<GroupsSnapshot> = safe {
        coroutineScope {
            val groups = async { fetchAllGroups() }
            val unread = async {
                try {
                    val summary = groupApiService.getUnreadSummary().unwrap()
                    summary.groups.entries.associate { it.key to (it.value.unreadQuestions + it.value.unreadMessages) }
                } catch (_: Exception) {
                    emptyMap<String, Int>()
                }
            }
            val bell = async {
                try {
                    groupApiService.getNotificationUnread().unwrap().count
                } catch (_: Exception) {
                    0
                }
            }
            GroupsSnapshot(groups.await(), unread.await(), bell.await())
        }
    }

    suspend fun groupUnread(id: String): Result<Int> = safe {
        groupApiService.getGroupUnread(id).unwrap().count
    }

    private suspend fun fetchAllGroups(): List<GroupDto> {
        val all = mutableListOf<GroupDto>()
        var page = 1
        while (true) {
            val res = groupApiService.getGroups(page = page, limit = 50).unwrap()
            all += res.items
            if (res.items.isEmpty() || all.size >= res.total) break
            page++
        }
        return all
    }

    suspend fun getGroupDetail(groupId: String): Group {
        return groupApiService.getGroupDetail(groupId).unwrap()
    }

    suspend fun getGroupMembers(groupId: String): List<GroupMember> {
        return groupApiService.getGroupMembers(groupId).unwrap().items
    }

    suspend fun getUnreadSummary(): UnreadSummary {
        return groupApiService.getUnreadSummary().unwrap()
    }

    private inline fun <T> safe(block: () -> T): Result<T> =
        try { Result.success(block()) } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
}
