package com.chandan.askfaculty.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandan.askfaculty.data.remote.model.GroupFilter
import com.chandan.askfaculty.data.remote.model.GroupsUiState
import com.chandan.askfaculty.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repo: GroupRepository
) : ViewModel() {
    private val _state = MutableStateFlow(GroupsUiState())
    val state: StateFlow<GroupsUiState> = _state.asStateFlow()

    fun refresh(userInitiated: Boolean = false) {
        _state.update { it.copy(refreshing = userInitiated, loading = it.groups.isEmpty() && !userInitiated) }
        viewModelScope.launch {
            repo.load()
                .onSuccess { s ->
                    _state.update {
                        it.copy(loading = false, refreshing = false, error = null,
                            groups = s.groups, unreadByGroup = s.unreadByGroup, bellCount = s.bellCount)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, refreshing = false, error = e.message ?: "Something went wrong") }
                }
        }
    }

    fun setFilter(f: GroupFilter) = _state.update { it.copy(filter = f) }

    fun markGroupReadLocally(groupId: String) =
        _state.update { it.copy(unreadByGroup = it.unreadByGroup + (groupId to 0)) }

    fun refreshGroupUnread(groupId: String) = viewModelScope.launch {
        repo.groupUnread(groupId).onSuccess { c ->
            _state.update { it.copy(unreadByGroup = it.unreadByGroup + (groupId to c)) }
        }
    }
}
