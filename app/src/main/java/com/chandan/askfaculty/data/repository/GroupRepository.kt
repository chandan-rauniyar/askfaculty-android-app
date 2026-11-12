package com.chandan.askfaculty.data.repository

import com.chandan.askfaculty.data.remote.GroupApiService
import com.chandan.askfaculty.data.remote.model.Group
import com.chandan.askfaculty.data.remote.model.UnreadSummary
import com.chandan.askfaculty.data.remote.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupApiService: GroupApiService
) {
    suspend fun getGroups(): List<Group> {
        return groupApiService.getGroups().unwrap()
    }

    suspend fun getUnreadSummary(): UnreadSummary {
        return groupApiService.getUnreadSummary().unwrap()
    }
}
