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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.shared.AppButton
import com.example.comalapp.ui.components.shared.AppButtonVariant
import com.example.comalapp.ui.components.shared.AppTextField
import com.example.comalapp.ui.components.shared.AppTextFieldType
import com.example.comalapp.ui.components.shared.BrandLogo
import com.example.comalapp.ui.viewmodel.AuthUiState
import com.example.comalapp.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
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
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        if (uiState is AuthUiState.ForgotPasswordSuccess) {
                            SuccessContent(
                                email = email,
                                onBack = onNavigateBack,
                            )
                        } else {
                            FormContent(
                                email = email,
                                onEmailChange = { email = it },
                                isLoading = uiState is AuthUiState.Loading,
                                onSubmit = { viewModel.sendPasswordReset(email) },
                                onBack = onNavigateBack,
                            )
                        }
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

@Composable
private fun FormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    BrandLogo(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Recuperar contraseña",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(24.dp))

    AppTextField(
        value = email,
        onValueChange = onEmailChange,
        label = "Correo electrónico",
        placeholder = "tu@universidad.edu",
        type = AppTextFieldType.Email,
        imeAction = ImeAction.Done,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(24.dp))

    AppButton(
        text = "Enviar enlace",
        onClick = onSubmit,
        enabled = email.isNotBlank() && !isLoading,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    AppButton(
        text = "Volver al inicio de sesión",
        onClick = onBack,
        variant = AppButtonVariant.Secondary,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SuccessContent(
    email: String,
    onBack: () -> Unit,
) {
    Spacer(modifier = Modifier.height(8.dp))

    Icon(
        imageVector = Icons.Outlined.MarkEmailRead,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Correo enviado",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Revisa tu bandeja de entrada en $email y sigue las instrucciones para restablecer tu contraseña.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(24.dp))

    AppButton(
        text = "Volver al inicio de sesión",
        onClick = onBack,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(8.dp))
}