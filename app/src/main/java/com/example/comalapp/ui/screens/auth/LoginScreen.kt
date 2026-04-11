package com.example.comalapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.comalapp.ui.components.AppButton
import com.example.comalapp.ui.components.AppButtonVariant
import com.example.comalapp.ui.components.AppTextField
import com.example.comalapp.ui.components.AppTextFieldType
import com.example.comalapp.ui.components.BrandLogo

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        contentScale = ContentScale.Fit,
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

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Iniciar sesión",
                        onClick = { onLoginSuccess("student") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppButton(
                        text = "Crear cuenta",
                        onClick = onNavigateToRegister,
                        variant = AppButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}