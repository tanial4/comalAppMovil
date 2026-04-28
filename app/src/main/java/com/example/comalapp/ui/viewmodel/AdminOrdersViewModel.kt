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
import java.util.Date

enum class OrderSortOrder { NEWEST, OLDEST }

data class AdminOrdersUiState(
    val adminUser: User? = null,
    val orders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String? = null,
    val sortOrder: OrderSortOrder = OrderSortOrder.NEWEST,
    val fromDate: Date? = null,
    val toDate: Date? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredOrders: List<Order>
        get() {
            var result = orders

            if (searchQuery.isNotBlank()) {
                result = result.filter {
                    it.id.takeLast(5).contains(searchQuery, ignoreCase = true)
                }
            }

            if (statusFilter != null) {
                result = result.filter { it.status == statusFilter }
            }

            fromDate?.let { from ->
                result = result.filter {
                    it.createdAt?.toDate()?.after(from) == true
                }
            }

            toDate?.let { to ->
                result = result.filter {
                    it.createdAt?.toDate()?.before(to) == true
                }
            }

            result = when (sortOrder) {
                OrderSortOrder.NEWEST -> result.sortedByDescending { it.createdAt?.seconds }
                OrderSortOrder.OLDEST -> result.sortedBy { it.createdAt?.seconds }
            }

            return result
        }

    val totalRevenue: Double
        get() = orders.filter { it.status == "delivered" }.sumOf { it.total }

    val completedOrders: Int
        get() = orders.count { it.status == "delivered" }
}

class AdminOrdersViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrdersUiState())
    val uiState: StateFlow<AdminOrdersUiState> = _uiState.asStateFlow()

    init {
        loadAdminUser()
        observeOrders()
    }

    private fun loadAdminUser() {
        val uid = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserById(uid).onSuccess { admin ->
                _uiState.value = _uiState.value.copy(adminUser = admin)
            }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            orderRepository.observeAllOrders().collect { result ->
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

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onStatusFilterChange(status: String?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
    }

    fun onSortOrderChange(sortOrder: OrderSortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
    }

    fun onFromDateChange(date: Date?) {
        _uiState.value = _uiState.value.copy(fromDate = date)
    }

    fun onToDateChange(date: Date?) {
        _uiState.value = _uiState.value.copy(toDate = date)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            statusFilter = null,
            sortOrder = OrderSortOrder.NEWEST,
            fromDate = null,
            toDate = null,
        )
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminOrdersViewModel(authRepository, userRepository, orderRepository) as T
        }
    }
}