package com.example.comalapp.ui.screens.admin

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.shared.AppButton
import com.example.comalapp.ui.components.shared.AppButtonVariant
import com.example.comalapp.ui.components.shared.AppImagePicker
import com.example.comalapp.ui.components.shared.AppSelectField
import com.example.comalapp.ui.components.shared.AppSwitch
import com.example.comalapp.ui.components.shared.AppTextField
import com.example.comalapp.ui.components.shared.AppTextFieldType
import com.example.comalapp.ui.components.shared.BrandLogo
import com.example.comalapp.ui.components.shared.SelectOption
import com.example.comalapp.ui.viewmodel.AdminProductFormViewModel

@Composable
fun AdminProductFormScreen(
    productId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: AdminProductFormViewModel = viewModel(
        factory = AdminProductFormViewModel.Factory(
            productId = productId,
            productRepository = container.productRepository,
            categoryRepository = container.categoryRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
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
                        text = "ADMINISTRADOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (productId == null) "Nuevo producto" else "Editar producto",
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
            AppImagePicker(
                imageUri = uiState.imageUri,
                onImageSelected = { viewModel.onImageSelected(it) },
                modifier = Modifier.fillMaxWidth(),
            )

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
                            text = "NOMBRE DEL PRODUCTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = "",
                            placeholder = "Ej: Café Espresso",
                            type = AppTextFieldType.Text,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DESCRIPCIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.onDescriptionChange(it) },
                            label = "",
                            placeholder = "Describe el producto...",
                            type = AppTextFieldType.Text,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "PRECIO (\$)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AppTextField(
                                value = uiState.price,
                                onValueChange = { viewModel.onPriceChange(it) },
                                label = "",
                                placeholder = "0",
                                type = AppTextFieldType.Number,
                                imeAction = ImeAction.Next,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "CATEGORÍA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AppSelectField(
                                selected = uiState.selectedCategory,
                                options = uiState.categories.map {
                                    SelectOption(
                                        key = it.id,
                                        label = it.name
                                            .split(" ")
                                            .joinToString(" ") { w ->
                                                w.replaceFirstChar { c -> c.uppercase() }
                                            },
                                    )
                                },
                                onOptionSelected = { viewModel.onCategorySelected(it) },
                                label = "",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    AppSwitch(
                        checked = uiState.available,
                        onCheckedChange = { viewModel.onAvailableChange(it) },
                        label = "Producto disponible",
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppButton(
                text = if (productId == null) "Crear producto" else "Actualizar producto",
                onClick = { viewModel.save() },
                enabled = uiState.name.isNotBlank()
                        && uiState.price.isNotBlank()
                        && uiState.selectedCategory != null
                        && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                text = "Cancelar",
                onClick = onBack,
                variant = AppButtonVariant.Danger,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}