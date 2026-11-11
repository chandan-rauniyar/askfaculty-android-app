package com.chandan.askfaculty.data.repository

import com.chandan.askfaculty.data.local.SessionManager
import com.chandan.askfaculty.data.remote.AuthApiService
import com.chandan.askfaculty.data.remote.model.ActivateRequest
import com.chandan.askfaculty.data.remote.model.AuthSession
import com.chandan.askfaculty.data.remote.model.EmailRequest
import com.chandan.askfaculty.data.remote.model.LoginRequest
import com.chandan.askfaculty.data.remote.model.RefreshRequest
import com.chandan.askfaculty.data.remote.model.ResetPasswordRequest
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

    suspend fun logout() {
        try {
            val token = sessionManager.refreshToken
            if (!token.isNullOrBlank()) {
                authApiService.logout(RefreshRequest(token))
            }
        } catch (_: Exception) {
            // Even if network logout fails, clear local session
        } finally {
            sessionManager.clearSession()
        }
    }

    suspend fun activate(token: String, password: String) {
        authApiService.activate(ActivateRequest(token, password)).unwrap()
    }

    suspend fun forgotPassword(email: String) {
        authApiService.forgotPassword(EmailRequest(email.trim().lowercase())).unwrap()
    }

    suspend fun resetPassword(token: String, password: String) {
        authApiService.resetPassword(ResetPasswordRequest(token, password)).unwrap()
    }

    suspend fun fetchMe(): User {
        val user: User = authApiService.getMe().unwrap()
        sessionManager.updateUser(user)
        return user
    }
}
