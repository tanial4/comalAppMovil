package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminWorkerFormUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val employeeNumber: String = "",
    val isLoading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class AdminWorkerFormViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWorkerFormUiState())
    val uiState: StateFlow<AdminWorkerFormUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onEmployeeNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(employeeNumber = value)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.register(state.email, state.password)
                .onSuccess {
                    val uid = authRepository.currentUserId() ?: run {
                        _uiState.value = _uiState.value.copy(
                            error = "No se pudo obtener el usuario creado",
                            isLoading = false,
                        )
                        return@launch
                    }
                    val user = User(
                        uid = uid,
                        email = state.email,
                        fullName = state.fullName,
                        expediente = state.employeeNumber,
                        role = "worker",
                    )
                    userRepository.createUser(user)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(
                                saved = true,
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
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminWorkerFormViewModel(authRepository, userRepository) as T
        }
    }
}