package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Order
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.ProductRepository
import com.example.comalapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val user: User? = null,
    val orders: List<Order> = emptyList(),
    val availableProductCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val totalRevenue: Double
        get() = orders.filter { it.status == "delivered" }.sumOf { it.total }
    val totalOrders: Int
        get() = orders.size
    val activeOrders: Int
        get() = orders.count { it.status in setOf("pending", "preparing", "ready") }
    val recentOrders: List<Order>
        get() = orders.sortedByDescending { it.createdAt }.take(5)
}

class AdminDashboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadAdminUser()
        observeData()
    }

    private fun loadAdminUser() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            orderRepository.observeAllOrders()
                .combine(productRepository.observeAllProducts()) { ordersResult, productsResult ->
                    Pair(
                        ordersResult.getOrNull() ?: emptyList(),
                        productsResult.getOrNull()?.count { it.available } ?: 0,
                    )
                }
                .collect { (orders, productCount) ->
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        availableProductCount = productCount,
                        isLoading = false,
                    )
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
        private val productRepository: ProductRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminDashboardViewModel(
                authRepository,
                userRepository,
                orderRepository,
                productRepository,
            ) as T
        }
    }
}