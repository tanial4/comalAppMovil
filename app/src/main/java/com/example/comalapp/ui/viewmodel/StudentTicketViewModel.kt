package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Order
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.ProductRepository
import com.example.comalapp.ui.components.student.OrderSummaryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentTicketUiState(
    val order: Order? = null,
    val summaryItems: List<OrderSummaryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class StudentTicketViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentTicketUiState())
    val uiState: StateFlow<StudentTicketUiState> = _uiState.asStateFlow()

    init {
        observeOrder()
        loadItems()
    }

    private fun observeOrder() {
        viewModelScope.launch {
            orderRepository.observeOrder(orderId).collect { result ->
                result.onSuccess { order ->
                    _uiState.value = _uiState.value.copy(order = order)
                }
            }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            orderRepository.getOrderItems(orderId)
                .onSuccess { orderItems ->
                    productRepository.getAvailableProducts()
                        .onSuccess { products ->
                            val items = orderItems.mapNotNull { item ->
                                val product = products.find { it.id == item.productId }
                                    ?: return@mapNotNull null
                                OrderSummaryItem(
                                    product = product,
                                    quantity = item.quantity,
                                    subtotal = item.subtotal,
                                )
                            }
                            _uiState.value = _uiState.value.copy(
                                summaryItems = items,
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
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false,
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val orderId: String,
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentTicketViewModel(orderId, orderRepository, productRepository) as T
        }
    }
}