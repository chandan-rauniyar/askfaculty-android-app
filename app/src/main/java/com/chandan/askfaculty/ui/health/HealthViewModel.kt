package com.chandan.askfaculty.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandan.askfaculty.data.remote.AppError
import com.chandan.askfaculty.data.remote.HealthApiService
import com.chandan.askfaculty.data.remote.model.HealthResponse
import com.chandan.askfaculty.data.remote.unwrap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HealthUiState {
    object Idle : HealthUiState
    object Loading : HealthUiState
    data class Success(val health: HealthResponse) : HealthUiState
    data class Error(val message: String) : HealthUiState
}

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthApiService: HealthApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Idle)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun checkHealth() {
        viewModelScope.launch {
            _uiState.value = HealthUiState.Loading
            try {
                val response = healthApiService.getHealthReady().unwrap()
                _uiState.value = HealthUiState.Success(response)
            } catch (e: Exception) {
                val appError = AppError.from(e)
                _uiState.value = HealthUiState.Error(appError.message)
            }
        }
    }
}
