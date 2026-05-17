package com.example.comalapp.ui.screens.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.shared.FilterChipGroup
import com.example.comalapp.ui.components.shared.FilterChipOption
import com.example.comalapp.ui.components.worker.WorkerProductCard
import com.example.comalapp.ui.components.worker.WorkerScaffold
import com.example.comalapp.ui.theme.violet
import com.example.comalapp.ui.viewmodel.WorkerProductsViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign

@Composable
fun WorkerProductsScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: WorkerProductsViewModel = viewModel(
        factory = WorkerProductsViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            productRepository = container.productRepository,
            categoryRepository = container.categoryRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val categoryOptions = remember(uiState.categories) {
        listOf(FilterChipOption(key = null, label = "Todos")) +
                uiState.categories.map { FilterChipOption(key = it.id, label = it.name) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    WorkerScaffold(
        currentRoute = currentRoute,
        label = "TRABAJADOR",
        title = "Productos",
        subtitle = "${uiState.filteredProducts.count { it.available }} disponibles",
        userName = uiState.user?.fullName ?: "",
        userEmail = uiState.user?.email ?: "",
        onLogout = onLogout,
        onNavigate = onNavigate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = violet)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                FilterChipGroup(
                    options = categoryOptions,
                    selectedKey = uiState.selectedCategoryId,
                    onSelectionChange = { viewModel.selectCategory(it) },
                    selectedColor = violet,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                if (uiState.filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(80.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin productos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (uiState.selectedCategoryId != null)
                                    "No hay productos en esta categoría"
                                else
                                    "Los productos del menú aparecerán aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.filteredProducts,
                            key = { it.id },
                        ) { product ->
                            WorkerProductCard(
                                product = product,
                                onAvailabilityChange = { available ->
                                    viewModel.setAvailability(product, available)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}