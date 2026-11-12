package com.chandan.askfaculty.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandan.askfaculty.data.remote.AppError
import com.chandan.askfaculty.data.remote.model.Group
import com.chandan.askfaculty.data.remote.model.UnreadSummary
import com.chandan.askfaculty.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GroupsUiState {
    object Loading : GroupsUiState
    data class Success(val groups: List<Group>, val unreadSummary: UnreadSummary) : GroupsUiState
    data class Error(val message: String) : GroupsUiState
}

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupsUiState>(GroupsUiState.Loading)
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = GroupsUiState.Loading
            try {
                val groups = groupRepository.getGroups()
                val unreadSummary = try {
                    groupRepository.getUnreadSummary()
                } catch (_: Exception) {
                    UnreadSummary()
                }
                _uiState.value = GroupsUiState.Success(groups, unreadSummary)
            } catch (e: Exception) {
                val appError = AppError.from(e)
                _uiState.value = GroupsUiState.Error(appError.message)
            }
        }
    }
}
