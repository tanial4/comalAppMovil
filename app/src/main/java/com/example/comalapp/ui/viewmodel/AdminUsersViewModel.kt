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

data class UserWithStats(
    val user: User,
    val orderCount: Int,
    val totalSpent: Double,
    val lastOrderDate: java.util.Date?,
)

data class AdminUsersUiState(
    val adminUser: User? = null,
    val usersWithStats: List<UserWithStats> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredUsers: List<UserWithStats>
        get() = if (searchQuery.isBlank()) usersWithStats
        else usersWithStats.filter {
            it.user.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.user.email.contains(searchQuery, ignoreCase = true)
        }
}

class AdminUsersViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

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
                    val users = (usersResult.getOrNull() ?: emptyList())
                        .filter { it.role == "student" }
                    users.map { user ->
                        val userOrders = orders.filter { it.userId == user.uid }
                        UserWithStats(
                            user = user,
                            orderCount = userOrders.size,
                            totalSpent = userOrders
                                .filter { it.status == "delivered" }
                                .sumOf { it.total },
                            lastOrderDate = userOrders
                                .maxByOrNull { it.createdAt?.seconds ?: 0 }
                                ?.createdAt?.toDate(),
                        )
                    }
                }
                .collect { usersWithStats ->
                    _uiState.value = _uiState.value.copy(
                        usersWithStats = usersWithStats,
                        isLoading = false,
                    )
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun deleteUser(uid: String) {
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
            return AdminUsersViewModel(authRepository, userRepository, orderRepository) as T
        }
    }
}