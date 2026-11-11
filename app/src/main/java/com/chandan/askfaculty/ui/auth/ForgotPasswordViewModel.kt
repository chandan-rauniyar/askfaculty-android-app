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

sealed interface ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState
    object Loading : ForgotPasswordUiState
    object Success : ForgotPasswordUiState
    data class Error(val message: String) : ForgotPasswordUiState
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun submit(email: String) {
        if (email.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("Email cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            try {
                authRepository.forgotPassword(email)
                _uiState.value = ForgotPasswordUiState.Success
            } catch (e: Exception) {
                val appError = AppError.from(e)
                val friendlyMessage = when (appError) {
                    is AppError.NetworkError -> "Cannot reach the server. Please check your connection."
                    else -> "If the email exists, password reset instructions have been sent."
                }
                _uiState.value = ForgotPasswordUiState.Success
            }
        }
    }
}
