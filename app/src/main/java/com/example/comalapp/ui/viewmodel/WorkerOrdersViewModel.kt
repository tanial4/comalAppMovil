package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Order
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkerOrdersUiState(
    val user: User? = null,
    val orders: List<Order> = emptyList(),
    val selectedStatus: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredOrders: List<Order>
        get() = if (selectedStatus == null) orders
        else orders.filter { it.status == selectedStatus }
}

class WorkerOrdersViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerOrdersUiState())
    val uiState: StateFlow<WorkerOrdersUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeOrders()
    }

    private fun loadUser() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            orderRepository.observeActiveOrders().collect { result ->
                result
                    .onSuccess { orders ->
                        _uiState.value = _uiState.value.copy(
                            orders = orders,
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

    fun selectStatus(status: String?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WorkerOrdersViewModel(authRepository, userRepository, orderRepository) as T
    }
}