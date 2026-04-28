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
import com.example.comalapp.ui.components.student.OrderSummaryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminOrderDetailUiState(
    val order: Order? = null,
    val adminUser: User? = null,
    val summaryItems: List<OrderSummaryItem> = emptyList(),
    val isLoading: Boolean = false,
    val qrError: Boolean = false,
    val error: String? = null,
)

class AdminOrderDetailViewModel(
    private val orderId: String,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderDetailUiState())
    val uiState: StateFlow<AdminOrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadAdminUser()
        observeOrder()
        loadItems()
    }

    private fun loadAdminUser() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(adminUser = user)
            }
        }
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
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false,
                    )
                }
        }
    }

    fun validateQrAndDeliver(scannedQr: String) {
        val order = _uiState.value.order ?: return
        if (scannedQr != order.qrCode) {
            _uiState.value = _uiState.value.copy(qrError = true)
            return
        }
        viewModelScope.launch {
            orderRepository.updateOrderStatus(
                orderId = orderId,
                currentStatus = order.status,
                newStatus = "delivered",
                requestedByRole = "worker",
            ).onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    fun revertStatus() {
        val order = _uiState.value.order ?: return
        val previousStatus = when (order.status) {
            "preparing" -> "pending"
            "ready"     -> "preparing"
            else        -> return
        }
        viewModelScope.launch {
            orderRepository.updateOrderStatus(
                orderId = orderId,
                currentStatus = order.status,
                newStatus = previousStatus,
                requestedByRole = _uiState.value.adminUser?.role ?: "admin",
            ).onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    fun clearQrError() {
        _uiState.value = _uiState.value.copy(qrError = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun advanceStatus() {
        val order = _uiState.value.order ?: return
        val nextStatus = when (order.status) {
            "pending"   -> "preparing"
            "preparing" -> "ready"
            else        -> return
        }
        viewModelScope.launch {
            orderRepository.updateOrderStatus(
                orderId = orderId,
                currentStatus = order.status,
                newStatus = nextStatus,
                requestedByRole = _uiState.value.adminUser?.role ?: "admin",
            ).onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    class Factory(
        private val orderId: String,
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminOrderDetailViewModel(
                orderId,
                authRepository,
                userRepository,
                orderRepository,
                productRepository,
            ) as T
        }
    }
}