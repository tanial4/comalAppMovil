package com.example.comalapp.ui.components.worker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.comalapp.ui.navigation.AppDestinations

data class WorkerNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val workerNavItems = listOf(
    WorkerNavItem("Inicio", Icons.Outlined.Home, AppDestinations.WORKER_HOME),
    WorkerNavItem("Ordenes", Icons.Outlined.ListAlt, AppDestinations.WORKER_ORDERS),
    WorkerNavItem("Productos", Icons.Outlined.Inventory2, AppDestinations.WORKER_PRODUCTS),
    WorkerNavItem("Escanear", Icons.Outlined.QrCodeScanner, AppDestinations.WORKER_QR_SCANNER),
)

private val workerSecondaryRoutes = mapOf(
    AppDestinations.WORKER_ORDER_DETAIL to AppDestinations.WORKER_ORDERS,
)

@Composable
fun WorkerBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeRoute = workerSecondaryRoutes[currentRoute] ?: currentRoute

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        workerNavItems.forEach { item ->
            val selected = activeRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) onNavigate(item.route)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }
}