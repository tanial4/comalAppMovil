package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Notification
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentNotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val unreadCount: Int get() = notifications.count { !it.read }
}

class StudentNotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentNotificationsUiState())
    val uiState: StateFlow<StudentNotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val userId = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            notificationRepository.observeUserNotifications(userId).collect { result ->
                result
                    .onSuccess { notifications ->
                        _uiState.value = _uiState.value.copy(
                            notifications = notifications.sortedByDescending { it.createdAt },
                            isLoading = false,
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message,
                            isLoading = false,
                        )
                    }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value.notifications
                .filter { !it.read }
                .forEach { notificationRepository.markAsRead(it.id) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val notificationRepository: NotificationRepository,
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentNotificationsViewModel(notificationRepository, authRepository) as T
        }
    }
}