package com.chandan.askfaculty.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandan.askfaculty.data.remote.model.UnreadSummary
import com.chandan.askfaculty.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _unreadSummary = MutableStateFlow(UnreadSummary())
    val unreadSummary: StateFlow<UnreadSummary> = _unreadSummary.asStateFlow()

    init {
        fetchUnreadSummary()
    }

    fun fetchUnreadSummary() {
        viewModelScope.launch {
            try {
                val summary = groupRepository.getUnreadSummary()
                _unreadSummary.value = summary
            } catch (_: Exception) {
                // Keep default empty summary on network error
            }
        }
    }
}
