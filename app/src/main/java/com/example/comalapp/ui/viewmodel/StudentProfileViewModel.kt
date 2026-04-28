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
import kotlinx.coroutines.launch

data class StudentProfileUiState(
    val user: User? = null,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class StudentProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userResult = userRepository.getUserById(uid)
            val ordersResult = orderRepository.getUserOrderHistory(uid)

            userResult.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message,
                )
                return@launch
            }

            val user = userResult.getOrNull()
            val orders = ordersResult.getOrNull() ?: emptyList()

            _uiState.value = _uiState.value.copy(
                user = user,
                totalOrders = orders.size,
                completedOrders = orders.count { it.status == "delivered" },
                totalSpent = orders
                    .filter { it.status == "delivered" }
                    .sumOf { it.total },
                isLoading = false,
            )
        }
    }

    fun logout() {
        authRepository.logout()
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
            return StudentProfileViewModel(authRepository, userRepository, orderRepository) as T
        }
    }
}