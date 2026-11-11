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

sealed interface ResetPasswordUiState {
    object Idle : ResetPasswordUiState
    object Loading : ResetPasswordUiState
    object Success : ResetPasswordUiState
    data class Error(val message: String) : ResetPasswordUiState
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun resetPassword(token: String, password: String) {
        if (token.isBlank() || password.isBlank()) {
            _uiState.value = ResetPasswordUiState.Error("Token and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            try {
                authRepository.resetPassword(token.trim(), password)
                _uiState.value = ResetPasswordUiState.Success
            } catch (e: Exception) {
                val appError = AppError.from(e)
                val friendlyMessage = when (appError) {
                    is AppError.InvalidResetToken -> "Invalid or expired password reset token."
                    is AppError.NetworkError -> "Cannot reach the server. Please check your connection."
                    else -> appError.message
                }
                _uiState.value = ResetPasswordUiState.Error(friendlyMessage)
            }
        }
    }
}
