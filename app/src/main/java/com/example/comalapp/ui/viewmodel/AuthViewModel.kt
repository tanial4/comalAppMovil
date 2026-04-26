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

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class LoginSuccess(val role: String) : AuthUiState()
    data object RegisterSuccess : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.LoginSuccess(user.role)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Error al iniciar sesión")
                }
        }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        expediente: String,
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, password)
                .onSuccess {
                    val uid = authRepository.currentUserId() ?: run {
                        _uiState.value = AuthUiState.Error("No se pudo obtener el usuario registrado")
                        return@launch
                    }
                    val user = User(
                        uid = uid,
                        email = email,
                        fullName = fullName,
                        expediente = expediente,
                        role = "student",
                    )
                    userRepository.createUser(user)
                        .onSuccess { _uiState.value = AuthUiState.RegisterSuccess }
                        .onFailure { error ->
                            _uiState.value = AuthUiState.Error(error.message ?: "Error al crear el perfil")
                        }
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Error al registrarse")
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository, userRepository) as T
        }
    }
}