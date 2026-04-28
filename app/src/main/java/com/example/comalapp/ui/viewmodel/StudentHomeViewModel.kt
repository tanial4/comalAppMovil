package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Order
import com.example.comalapp.data.model.Product
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

data class StudentHomeUiState(
    val user: User? = null,
    val products: List<Product> = emptyList(),
    val activeOrder: Order? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class StudentHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

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
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            productRepository.observeAvailableProducts()
                .combine(orderRepository.observeActiveOrders()) { productsResult, ordersResult ->
                    Pair(
                        productsResult.getOrNull() ?: emptyList(),
                        ordersResult.getOrNull()?.firstOrNull { it.userId == uid },
                    )
                }
                .collect { (products, activeOrder) ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        activeOrder = activeOrder,
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
        private val productRepository: ProductRepository,
        private val orderRepository: OrderRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentHomeViewModel(
                authRepository,
                userRepository,
                productRepository,
                orderRepository,
            ) as T
        }
    }
}