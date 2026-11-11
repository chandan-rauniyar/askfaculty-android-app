package com.chandan.askfaculty.data.repository

import com.chandan.askfaculty.data.local.SessionManager
import com.chandan.askfaculty.data.remote.AuthApiService
import com.chandan.askfaculty.data.remote.model.AuthSession
import com.chandan.askfaculty.data.remote.model.LoginRequest
import com.chandan.askfaculty.data.remote.model.User
import com.chandan.askfaculty.data.remote.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): User {
        val session: AuthSession = authApiService.login(LoginRequest(email, password)).unwrap()
        sessionManager.saveSession(session.accessToken, session.refreshToken, session.user)
        return session.user
    }

    suspend fun fetchMe(): User {
        val user: User = authApiService.getMe().unwrap()
        sessionManager.updateUser(user)
        return user
    }
}
