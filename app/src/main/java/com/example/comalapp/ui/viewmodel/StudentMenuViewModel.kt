package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Category
import com.example.comalapp.data.model.Product
import com.example.comalapp.data.repository.CategoryRepository
import com.example.comalapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentMenuUiState(
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredProducts: List<Product>
        get() = if (selectedCategoryId == null) products
        else products.filter { it.categoryId == selectedCategoryId }
}

class StudentMenuViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentMenuUiState())
    val uiState: StateFlow<StudentMenuUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        observeCategories()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            productRepository.getAvailableProducts()
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
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

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { result ->
                result
                    .onSuccess { categories ->
                        _uiState.value = _uiState.value.copy(categories = categories)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
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
        private val productRepository: ProductRepository,
        private val categoryRepository: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentMenuViewModel(productRepository, categoryRepository) as T
        }
    }
}