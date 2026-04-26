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
        loadData()
        observeActiveOrder()
    }

    private fun loadData() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userResult = userRepository.getUserById(uid)
            val productsResult = productRepository.getAvailableProducts()

            _uiState.value = _uiState.value.copy(
                user = userResult.getOrNull(),
                products = productsResult.getOrNull() ?: emptyList(),
                isLoading = false,
                error = userResult.exceptionOrNull()?.message
                    ?: productsResult.exceptionOrNull()?.message,
            )
        }
    }

    private fun observeActiveOrder() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            orderRepository.observeActiveOrders().collect { result ->
                result
                    .onSuccess { orders ->
                        _uiState.value = _uiState.value.copy(
                            activeOrder = orders.firstOrNull { it.userId == uid },
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
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