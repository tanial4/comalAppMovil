package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WorkerWithStats(
    val user: User,
    val orderCount: Int,
)

data class AdminWorkersUiState(
    val adminUser: User? = null,
    val workers: List<WorkerWithStats> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredWorkers: List<WorkerWithStats>
        get() = if (searchQuery.isBlank()) workers
        else workers.filter {
            it.user.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.user.expediente.contains(searchQuery, ignoreCase = true)
        }
}

class AdminWorkersViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWorkersUiState())
    val uiState: StateFlow<AdminWorkersUiState> = _uiState.asStateFlow()

    init {
        loadAdminUser()
        observeData()
    }

    private fun loadAdminUser() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid).onSuccess { admin ->
                _uiState.value = _uiState.value.copy(adminUser = admin)
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.observeAllUsers()
                .combine(orderRepository.observeAllOrders()) { usersResult, ordersResult ->
                    val orders = ordersResult.getOrNull() ?: emptyList()
                    (usersResult.getOrNull() ?: emptyList())
                        .filter { it.role == "worker" }
                        .map { worker ->
                            WorkerWithStats(
                                user = worker,
                                orderCount = orders.count { it.userId == worker.uid },
                            )
                        }
                }
                .collect { workers ->
                    _uiState.value = _uiState.value.copy(
                        workers = workers,
                        isLoading = false,
                    )
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun deleteWorker(uid: String) {
        viewModelScope.launch {
            userRepository.deleteUser(uid)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val orderRepository: OrderRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminWorkersViewModel(authRepository, userRepository, orderRepository) as T
        }
    }
}