package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.NotificationRepository
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WorkerQrScannerUiState(
    val user: User? = null,
    val isProcessing: Boolean = false,
    val deliverySuccess: Boolean = false,
    val deliveredOrderId: String? = null,
    val qrError: Boolean = false,
    val error: String? = null,
)

class WorkerQrScannerViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerQrScannerUiState())
    val uiState: StateFlow<WorkerQrScannerUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    fun processScannedQr(scannedQr: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val orders = orderRepository.observeActiveOrders().first().getOrNull() ?: emptyList()
            val match = orders.find { it.status == "ready" && it.qrCode == scannedQr }
            if (match == null) {
                _uiState.value = _uiState.value.copy(isProcessing = false, qrError = true)
                return@launch
            }
            orderRepository.updateOrderStatus(
                orderId = match.id,
                currentStatus = match.status,
                newStatus = "delivered",
                requestedByRole = "worker",
            ).onSuccess {
                notificationRepository.createNotification(
                    userId = match.userId,
                    orderId = match.id,
                    title = "Orden entregada",
                    message = "Tu orden #${match.id.takeLast(5).uppercase()} fue entregada. ¡Buen provecho!",
                    type = "delivered",
                )
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    deliverySuccess = true,
                    deliveredOrderId = match.id,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = error.message,
                )
            }
        }
    }

    fun clearQrError() {
        _uiState.value = _uiState.value.copy(qrError = false)
    }

    fun clearDeliverySuccess() {
        _uiState.value = _uiState.value.copy(deliverySuccess = false, deliveredOrderId = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val orderRepository: OrderRepository,
        private val notificationRepository: NotificationRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WorkerQrScannerViewModel(
                authRepository,
                userRepository,
                orderRepository,
                notificationRepository,
            ) as T
    }
}