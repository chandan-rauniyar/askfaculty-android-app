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

sealed interface ActivationUiState {
    object Idle : ActivationUiState
    object Loading : ActivationUiState
    object Success : ActivationUiState
    data class Error(val message: String) : ActivationUiState
}

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivationUiState>(ActivationUiState.Idle)
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    fun activate(token: String, password: String) {
        if (token.isBlank() || password.isBlank()) {
            _uiState.value = ActivationUiState.Error("Token and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ActivationUiState.Loading
            try {
                authRepository.activate(token.trim(), password)
                _uiState.value = ActivationUiState.Success
            } catch (e: Exception) {
                val appError = AppError.from(e)
                val friendlyMessage = when (appError) {
                    is AppError.InvalidActivationToken -> "Invalid or expired activation token."
                    is AppError.NetworkError -> "Cannot reach the server. Please check your connection."
                    else -> appError.message
                }
                _uiState.value = ActivationUiState.Error(friendlyMessage)
            }
        }
    }
}
