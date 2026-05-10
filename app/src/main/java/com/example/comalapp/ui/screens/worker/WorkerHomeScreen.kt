package com.example.comalapp.ui.screens.worker

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
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.worker.UrgentOrdersCard
import com.example.comalapp.ui.components.worker.WorkerQuickAccessCard
import com.example.comalapp.ui.components.worker.WorkerScaffold
import com.example.comalapp.ui.components.worker.WorkerStatCard
import com.example.comalapp.ui.navigation.AppDestinations
import com.example.comalapp.ui.theme.OrderStatusColor
import com.example.comalapp.ui.theme.violet
import com.example.comalapp.ui.viewmodel.WorkerHomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WorkerHomeScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onOrderClick: (String) -> Unit,
    onSeeAllOrders: () -> Unit,
    onScanQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: WorkerHomeViewModel = viewModel(
        factory = WorkerHomeViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            orderRepository = container.orderRepository,
            productRepository = container.productRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val today = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es"))
        .format(Calendar.getInstance().time)

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    WorkerScaffold(
        currentRoute = currentRoute,
        label = "TRABAJADOR",
        title = "${uiState.greeting},\n${uiState.user?.fullName ?: ""}",
        subtitle = today,
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
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkerStatCard(
                        value = uiState.pendingOrders.toString(),
                        label = "Pendientes",
                        icon = Icons.Outlined.Timer,
                        iconBackgroundColor = OrderStatusColor.pendient.copy(alpha = 0.15f),
                        iconTint = OrderStatusColor.pendient,
                        modifier = Modifier.weight(1f),
                    )
                    WorkerStatCard(
                        value = uiState.preparingOrders.toString(),
                        label = "En preparación",
                        icon = Icons.Outlined.RestaurantMenu,
                        iconBackgroundColor = OrderStatusColor.prep.copy(alpha = 0.15f),
                        iconTint = OrderStatusColor.prep,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkerStatCard(
                        value = uiState.readyOrders.toString(),
                        label = "Listas",
                        icon = Icons.Outlined.ListAlt,
                        iconBackgroundColor = OrderStatusColor.ready.copy(alpha = 0.15f),
                        iconTint = OrderStatusColor.ready,
                        modifier = Modifier.weight(1f),
                    )
                    WorkerStatCard(
                        value = uiState.availableProductCount.toString(),
                        label = "Disponibles",
                        icon = Icons.Outlined.Inventory2,
                        iconBackgroundColor = violet.copy(alpha = 0.15f),
                        iconTint = violet,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkerQuickAccessCard(
                        title = "Ver órdenes",
                        subtitle = "${uiState.activeOrders.size} activas",
                        icon = Icons.Outlined.ListAlt,
                        backgroundColor = violet,
                        onClick = onSeeAllOrders,
                        modifier = Modifier.weight(1f),
                    )
                    WorkerQuickAccessCard(
                        title = "Escanear QR",
                        subtitle = "Confirmar entrega",
                        icon = Icons.Outlined.QrCodeScanner,
                        backgroundColor = Color(0xFF00BCD4),
                        onClick = onScanQr,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (uiState.urgentOrders.isNotEmpty()) {
                    UrgentOrdersCard(
                        orders = uiState.urgentOrders,
                        onSeeAll = onSeeAllOrders,
                        onOrderClick = onOrderClick,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}