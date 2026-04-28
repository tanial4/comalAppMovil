package com.example.comalapp.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.comalapp.ui.components.admin.AdminScaffold
import com.example.comalapp.ui.components.admin.OrderStatusDistributionCard
import com.example.comalapp.ui.components.admin.RecentOrdersCard
import com.example.comalapp.ui.components.admin.StatCard
import com.example.comalapp.ui.navigation.AppDestinations
import com.example.comalapp.ui.viewmodel.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            orderRepository = container.orderRepository,
            productRepository = container.productRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    AdminScaffold(
        currentRoute = currentRoute,
        label = "ADMINISTRADOR",
        title = "Panel de control",
        subtitle = "Bienvenido, ${uiState.user?.fullName ?: ""}",
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        value = "$${"%.0f".format(uiState.totalRevenue)}",
                        label = "Ingresos",
                        sublabel = "total acumulado",
                        icon = Icons.Outlined.TrendingUp,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = uiState.totalOrders.toString(),
                        label = "Órdenes",
                        sublabel = "registradas",
                        icon = Icons.Outlined.ShoppingBag,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        value = uiState.activeOrders.toString(),
                        label = "Activas",
                        sublabel = "en curso ahora",
                        icon = Icons.Outlined.ListAlt,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = uiState.availableProductCount.toString(),
                        label = "Productos",
                        sublabel = "disponibles",
                        icon = Icons.Outlined.Inventory2,
                        modifier = Modifier.weight(1f),
                    )
                }

                RecentOrdersCard(
                    orders = uiState.recentOrders,
                    onSeeAll = { onNavigate(AppDestinations.ADMIN_ORDERS) },
                )

                OrderStatusDistributionCard(orders = uiState.orders)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}