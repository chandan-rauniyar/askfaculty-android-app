package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ApiResponse
import com.chandan.askfaculty.data.remote.model.Group
import com.chandan.askfaculty.data.remote.model.UnreadSummary
import retrofit2.http.GET

interface GroupApiService {
    @GET("groups")
    suspend fun getGroups(): ApiResponse<List<Group>>

    @GET("me/unread-summary")
    suspend fun getUnreadSummary(): ApiResponse<UnreadSummary>
}
