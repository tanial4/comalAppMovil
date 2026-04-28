package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.Category
import com.example.comalapp.data.model.Product
import com.example.comalapp.data.repository.CategoryRepository
import com.example.comalapp.data.repository.ProductRepository
import com.example.comalapp.ui.components.shared.SelectOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

data class AdminProductFormUiState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val available: Boolean = true,
    val imageUri: String? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategory: SelectOption? = null,
    val isLoading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class AdminProductFormViewModel(
    private val productId: String?,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductFormUiState())
    val uiState: StateFlow<AdminProductFormUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
        if (productId != null) loadProduct()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { result ->
                result.onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            productRepository.getAllProducts()
                .onSuccess { products ->
                    val product = products.find { it.id == productId } ?: return@onSuccess
                    _uiState.value = _uiState.value.copy(
                        name = product.name,
                        description = product.description,
                        price = product.price.toString(),
                        available = product.available,
                        imageUri = product.imageUrl.ifEmpty { null },
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

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(price = value)
    }

    fun onAvailableChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(available = value)
    }

    fun onImageSelected(uri: String) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun onCategorySelected(option: SelectOption) {
        _uiState.value = _uiState.value.copy(selectedCategory = option)
    }

    fun save() {
        val state = _uiState.value
        val price = state.price.toDoubleOrNull() ?: run {
            _uiState.value = _uiState.value.copy(error = "Precio inválido")
            return
        }
        val categoryId = state.selectedCategory?.key ?: run {
            _uiState.value = _uiState.value.copy(error = "Selecciona una categoría")
            return
        }
        val imageUri = state.imageUri?.let { Uri.parse(it) }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (productId == null) {
                if (imageUri == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Selecciona una imagen",
                        isLoading = false,
                    )
                    return@launch
                }
                val product = Product(
                    name = state.name,
                    description = state.description,
                    price = price,
                    available = state.available,
                    categoryId = categoryId,
                )
                productRepository.createProduct(product, imageUri)
                    .onSuccess { _uiState.value = _uiState.value.copy(saved = true, isLoading = false) }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    }
            } else {
                val product = Product(
                    id = productId,
                    name = state.name,
                    description = state.description,
                    price = price,
                    available = state.available,
                    categoryId = categoryId,
                    imageUrl = state.imageUri ?: "",
                )
                productRepository.updateProduct(product, imageUri)
                    .onSuccess { _uiState.value = _uiState.value.copy(saved = true, isLoading = false) }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val productId: String?,
        private val productRepository: ProductRepository,
        private val categoryRepository: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminProductFormViewModel(productId, productRepository, categoryRepository) as T
        }
    }
}