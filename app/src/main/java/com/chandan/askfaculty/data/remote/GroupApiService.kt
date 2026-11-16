package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ApiResponse
import com.chandan.askfaculty.data.remote.model.CountDto
import com.chandan.askfaculty.data.remote.model.Group
import com.chandan.askfaculty.data.remote.model.GroupDto
import com.chandan.askfaculty.data.remote.model.GroupMember
import com.chandan.askfaculty.data.remote.model.PaginatedResponse
import com.chandan.askfaculty.data.remote.model.UnreadSummary
import com.chandan.askfaculty.data.remote.model.UnreadSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupApiService {
    @GET("groups")
    suspend fun getGroups(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("status") status: String? = null
    ): ApiResponse<PaginatedResponse<GroupDto>>

    @GET("groups/{id}")
    suspend fun getGroupDetail(@Path("id") groupId: String): ApiResponse<Group>

    @GET("groups/{id}/members")
    suspend fun getGroupMembers(@Path("id") groupId: String): ApiResponse<PaginatedResponse<GroupMember>>

    @GET("me/unread-summary")
    suspend fun getUnreadSummary(): ApiResponse<UnreadSummary>

    @GET("notifications/unread-count")
    suspend fun getNotificationUnread(): ApiResponse<CountDto>

    @GET("groups/{groupId}/unread-count")
    suspend fun getGroupUnread(@Path("groupId") groupId: String): ApiResponse<CountDto>
}
