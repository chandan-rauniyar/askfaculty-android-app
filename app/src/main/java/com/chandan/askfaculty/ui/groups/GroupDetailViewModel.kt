package com.chandan.askfaculty.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandan.askfaculty.data.remote.AppError
import com.chandan.askfaculty.data.remote.model.Group
import com.chandan.askfaculty.data.remote.model.GroupMember
import com.chandan.askfaculty.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GroupDetailUiState {
    object Loading : GroupDetailUiState
    data class Success(val group: Group, val members: List<GroupMember>) : GroupDetailUiState
    data class Error(val message: String) : GroupDetailUiState
}

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupDetailUiState>(GroupDetailUiState.Loading)
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroupDetail(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupDetailUiState.Loading
            try {
                val group = groupRepository.getGroupDetail(groupId)
                val members = try {
                    groupRepository.getGroupMembers(groupId)
                } catch (_: Exception) {
                    emptyList()
                }
                _uiState.value = GroupDetailUiState.Success(group, members)
            } catch (e: Exception) {
                val appError = AppError.from(e)
                _uiState.value = GroupDetailUiState.Error(appError.message)
            }
        }
    }
}
