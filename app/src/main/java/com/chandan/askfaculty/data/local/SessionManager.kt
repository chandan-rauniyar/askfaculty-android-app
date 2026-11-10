package com.chandan.askfaculty.data.local

import com.chandan.askfaculty.data.remote.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Session(
    val accessToken: String?,
    val refreshToken: String?,
    val user: User?
) {
    val isLoggedIn: Boolean get() = !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    private val _session = MutableStateFlow(
        Session(
            accessToken = tokenStorage.getAccessToken(),
            refreshToken = tokenStorage.getRefreshToken(),
            user = null
        )
    )
    val session: StateFlow<Session> = _session.asStateFlow()

    fun saveSession(accessToken: String, refreshToken: String, user: User? = null) {
        tokenStorage.saveTokens(accessToken, refreshToken)
        _session.value = Session(accessToken, refreshToken, user)
    }

    fun updateUser(user: User?) {
        val current = _session.value
        _session.value = current.copy(user = user)
    }

    fun clearSession() {
        tokenStorage.clear()
        _session.value = Session(null, null, null)
    }

    val accessToken: String? get() = _session.value.accessToken
    val refreshToken: String? get() = _session.value.refreshToken
}
