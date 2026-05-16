package com.example.comalapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.shared.AppButton
import com.example.comalapp.ui.components.shared.AppButtonVariant
import com.example.comalapp.ui.components.shared.AppTextField
import com.example.comalapp.ui.components.shared.AppTextFieldType
import com.example.comalapp.ui.components.shared.BrandLogo
import com.example.comalapp.ui.components.shared.ConfirmDialog
import com.example.comalapp.ui.viewmodel.AuthUiState
import com.example.comalapp.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.LoginSuccess -> onLoginSuccess(state.role)
            is AuthUiState.Error -> {
                authError = state.message
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    if (authError != null) {
        ConfirmDialog(
            title = "Error al iniciar sesión",
            message = authError!!,
            confirmText = "Entendido",
            confirmColor = MaterialTheme.colorScheme.primary,
            dismissText = null,
            onConfirm = { authError = null },
            onDismiss = { authError = null },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BrandLogo(
                            modifier = Modifier
                                .height(80.dp)
                                .fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Bienvenido de nuevo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AppTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            placeholder = "tu@universidad.edu",
                            type = AppTextFieldType.Email,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            type = AppTextFieldType.Password,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = onNavigateToForgotPassword,
                                modifier = Modifier.align(Alignment.CenterEnd),
                            ) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AppButton(
                            text = "Iniciar sesión",
                            onClick = { viewModel.login(email, password) },
                            enabled = uiState !is AuthUiState.Loading
                                    && email.isNotBlank()
                                    && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppButton(
                            text = "Crear cuenta",
                            onClick = onNavigateToRegister,
                            variant = AppButtonVariant.Secondary,
                            enabled = uiState !is AuthUiState.Loading,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}