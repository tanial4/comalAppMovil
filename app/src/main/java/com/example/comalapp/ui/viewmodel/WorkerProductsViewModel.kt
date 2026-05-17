package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Category
import com.example.comalapp.data.model.Product
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.CategoryRepository
import com.example.comalapp.data.repository.ProductRepository
import com.example.comalapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WorkerProductsUiState(
    val user: User? = null,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredProducts: List<Product>
        get() = if (selectedCategoryId == null) products
        else products.filter { it.categoryId == selectedCategoryId }
}

class WorkerProductsViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerProductsUiState())
    val uiState: StateFlow<WorkerProductsUiState> = _uiState.asStateFlow()

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
            productRepository.observeAllProducts()
                .combine(categoryRepository.observeCategories()) { productsResult, categoriesResult ->
                    Pair(
                        productsResult.getOrNull() ?: emptyList(),
                        categoriesResult.getOrNull() ?: emptyList(),
                    )
                }
                .collect { (products, categories) ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        categories = categories,
                        isLoading = false,
                    )
                }
        }
    }

    fun setAvailability(product: Product, available: Boolean) {
        viewModelScope.launch {
            productRepository.updateProduct(
                product = product.copy(available = available),
                imageUri = null,
            ).onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val productRepository: ProductRepository,
        private val categoryRepository: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WorkerProductsViewModel(
                authRepository,
                userRepository,
                productRepository,
                categoryRepository,
            ) as T
    }
}