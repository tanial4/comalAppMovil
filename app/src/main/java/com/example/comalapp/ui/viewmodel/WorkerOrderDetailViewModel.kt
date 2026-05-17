package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Order
import com.example.comalapp.data.repository.NotificationRepository
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.ProductRepository
import com.example.comalapp.ui.components.student.OrderSummaryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkerOrderDetailUiState(
    val order: Order? = null,
    val summaryItems: List<OrderSummaryItem> = emptyList(),
    val isLoading: Boolean = false,
    val qrError: Boolean = false,
    val deliverySuccess: Boolean = false,
    val error: String? = null,
)

class WorkerOrderDetailViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerOrderDetailUiState())
    val uiState: StateFlow<WorkerOrderDetailUiState> = _uiState.asStateFlow()

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
                    productRepository.getAllProducts()
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

    private fun notifyStudent(title: String, message: String, type: String) {
        val order = _uiState.value.order ?: return
        viewModelScope.launch {
            notificationRepository.createNotification(
                userId = order.userId,
                orderId = orderId,
                title = title,
                message = message,
                type = type,
            )
        }
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
                requestedByRole = "worker",
            ).onSuccess {
                when (nextStatus) {
                    "preparing" -> notifyStudent(
                        title = "Orden en preparación",
                        message = "Tu orden #${orderId.takeLast(5).uppercase()} está siendo preparada por el equipo.",
                        type = "preparing",
                    )
                    "ready" -> notifyStudent(
                        title = "¡Tu orden está lista!",
                        message = "Tu orden #${orderId.takeLast(5).uppercase()} está lista para recoger en el mostrador.",
                        type = "ready",
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
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
            ).onSuccess {
                notifyStudent(
                    title = "Orden entregada",
                    message = "Tu orden #${orderId.takeLast(5).uppercase()} fue entregada. ¡Buen provecho!",
                    type = "delivered",
                )
                _uiState.value = _uiState.value.copy(deliverySuccess = true)
            }.onFailure { error ->
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

    class Factory(
        private val orderId: String,
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository,
        private val notificationRepository: NotificationRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WorkerOrderDetailViewModel(
                orderId,
                orderRepository,
                productRepository,
                notificationRepository,
            ) as T
    }
}