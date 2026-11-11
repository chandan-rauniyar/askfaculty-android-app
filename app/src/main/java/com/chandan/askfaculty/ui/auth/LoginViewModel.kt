package com.chandan.askfaculty.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandan.askfaculty.data.remote.AppError
import com.chandan.askfaculty.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                authRepository.login(email.trim().lowercase(), password)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                val appError = AppError.from(e)
                val friendlyMessage = when (appError) {
                    is AppError.InvalidCredentials -> "Invalid email or password."
                    is AppError.AccountInactive -> "Your account is inactive. Please contact college support."
                    is AppError.NetworkError -> "Cannot reach the server. Please check your connection."
                    else -> appError.message
                }
                _uiState.value = LoginUiState.Error(friendlyMessage)
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
