package com.example.comalapp.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.comalapp.ui.viewmodel.AdminWorkerFormViewModel

@Composable
fun AdminWorkerFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: AdminWorkerFormViewModel = viewModel(
        factory = AdminWorkerFormViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDiscardConfirm) {
        ConfirmDialog(
            title = "Descartar cambios",
            message = "¿Estás seguro de que deseas salir? Los datos ingresados se perderán.",
            confirmText = "Descartar",
            dismissText = "Seguir editando",
            onConfirm = {
                showDiscardConfirm = false
                onBack()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 48.dp, bottom = 20.dp),
                ) {
                    BrandLogo(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "PERSONAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nuevo trabajador",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "NOMBRE COMPLETO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = uiState.fullName,
                            onValueChange = { viewModel.onFullNameChange(it) },
                            label = "",
                            placeholder = "Ej: Juan Pérez",
                            type = AppTextFieldType.Text,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "NÚMERO DE EMPLEADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = uiState.employeeNumber,
                            onValueChange = { viewModel.onEmployeeNumberChange(it) },
                            label = "",
                            placeholder = "Ej: 555-1001",
                            type = AppTextFieldType.Text,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CORREO ELECTRÓNICO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            label = "",
                            placeholder = "trabajador@comal.com",
                            type = AppTextFieldType.Email,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CONTRASEÑA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            label = "",
                            type = AppTextFieldType.Password,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppButton(
                text = "Crear trabajador",
                onClick = { viewModel.save() },
                enabled = uiState.fullName.isNotBlank()
                        && uiState.email.isNotBlank()
                        && uiState.password.isNotBlank()
                        && uiState.employeeNumber.isNotBlank()
                        && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                text = "Cancelar",
                onClick = { showDiscardConfirm = true },
                variant = AppButtonVariant.Secondary,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}