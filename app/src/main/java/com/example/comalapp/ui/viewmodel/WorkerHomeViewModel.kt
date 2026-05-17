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
import java.util.Calendar

data class WorkerHomeUiState(
    val user: User? = null,
    val activeOrders: List<Order> = emptyList(),
    val availableProductCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val pendingOrders: Int
        get() = activeOrders.count { it.status == "pending" }
    val preparingOrders: Int
        get() = activeOrders.count { it.status == "preparing" }
    val readyOrders: Int
        get() = activeOrders.count { it.status == "ready" }
    val urgentOrders: List<Order>
        get() = activeOrders
            .filter { it.status == "pending" }
            .sortedBy { it.createdAt?.seconds }
            .take(5)
    val greeting: String
        get() = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11  -> "Buenos días"
            in 12..17 -> "Buenas tardes"
            else      -> "Buenas noches"
        }
}

class WorkerHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerHomeUiState())
    val uiState: StateFlow<WorkerHomeUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeData()
    }

    private fun loadUser() {
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
            orderRepository.observeActiveOrders()
                .combine(productRepository.observeAvailableProducts()) { ordersResult, productsResult ->
                    Pair(
                        ordersResult.getOrNull() ?: emptyList(),
                        productsResult.getOrNull()?.size ?: 0,
                    )
                }
                .collect { (orders, productCount) ->
                    _uiState.value = _uiState.value.copy(
                        activeOrders = orders,
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
            return WorkerHomeViewModel(
                authRepository,
                userRepository,
                orderRepository,
                productRepository,
            ) as T
        }
    }
}