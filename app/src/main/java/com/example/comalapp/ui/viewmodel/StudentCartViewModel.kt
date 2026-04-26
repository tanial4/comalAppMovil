package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Product
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    val quantity: Int,
)

data class StudentCartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val orderConfirmed: Boolean = false,
    val error: String? = null,
) {
    val subtotal: Double get() = items.sumOf { it.product.price * it.quantity }
    val totalItemCount: Int get() = items.sumOf { it.quantity }
}

class StudentCartViewModel(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentCartUiState())
    val uiState: StateFlow<StudentCartUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product) {
        val current = _uiState.value.items.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _uiState.value = _uiState.value.copy(items = current)
    }

    fun increment(productId: String) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map { item ->
                if (item.product.id == productId) item.copy(quantity = item.quantity + 1)
                else item
            }
        )
    }

    fun decrement(productId: String) {
        val current = _uiState.value.items.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index < 0) return
        if (current[index].quantity <= 1) {
            current.removeAt(index)
        } else {
            current[index] = current[index].copy(quantity = current[index].quantity - 1)
        }
        _uiState.value = _uiState.value.copy(items = current)
    }

    fun delete(productId: String) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.filter { it.product.id != productId }
        )
    }

    fun confirmOrder() {
        val userId = authRepository.currentUserId() ?: return
        val pairs = _uiState.value.items.map { it.product to it.quantity }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            orderRepository.createOrder(userId, pairs)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        items = emptyList(),
                        isLoading = false,
                        orderConfirmed = true,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al confirmar la orden",
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetConfirmed() {
        _uiState.value = _uiState.value.copy(orderConfirmed = false)
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentCartViewModel(orderRepository, authRepository) as T
        }
    }
}