package com.example.comalapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.comalapp.data.model.User
import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class LoginSuccess(val role: String) : AuthUiState()
    data object RegisterSuccess : AuthUiState()
    data object ForgotPasswordSuccess : AuthUiState()
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
                    _uiState.value = AuthUiState.Error(mapAuthError(error))
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
                            _uiState.value = AuthUiState.Error(mapAuthError(error))
                        }
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapAuthError(error))
                }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _uiState.value = AuthUiState.ForgotPasswordSuccess }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapAuthError(error))
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository, userRepository) as T
    }
}

private fun mapAuthError(error: Throwable): String {
    val code = (error as? FirebaseAuthException)?.errorCode ?: ""
    val message = error.message ?: ""

    return when {
        code.contains("wrong-password", ignoreCase = true)
                || code.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true)
                || code.contains("invalid-credential", ignoreCase = true)
                || message.contains("credential", ignoreCase = true)
                || message.contains("password", ignoreCase = true)
                || message.contains("identifier", ignoreCase = true) ->
            "Correo electrónico o contraseña incorrectos"
        code.contains("user-not-found", ignoreCase = true)
                || message.contains("user-not-found", ignoreCase = true) ->
            "No existe una cuenta con este correo"
        code.contains("invalid-email", ignoreCase = true)
                || message.contains("badly formatted", ignoreCase = true) ->
            "El correo electrónico no es válido"
        code.contains("user-disabled", ignoreCase = true) ->
            "Esta cuenta ha sido deshabilitada"
        code.contains("email-already-in-use", ignoreCase = true)
                || message.contains("already in use", ignoreCase = true) ->
            "Ya existe una cuenta con este correo"
        code.contains("weak-password", ignoreCase = true)
                || message.contains("at least 6", ignoreCase = true) ->
            "La contraseña debe tener al menos 6 caracteres"
        code.contains("network-request-failed", ignoreCase = true)
                || message.contains("network", ignoreCase = true) ->
            "Error de conexión. Verifica tu internet"
        code.contains("too-many-requests", ignoreCase = true)
                || message.contains("too many", ignoreCase = true) ->
            "Demasiados intentos fallidos. Intenta más tarde"
        code.contains("operation-not-allowed", ignoreCase = true) ->
            "Operación no permitida"
        else -> "Ocurrió un error inesperado. Intenta de nuevo"
    }
}