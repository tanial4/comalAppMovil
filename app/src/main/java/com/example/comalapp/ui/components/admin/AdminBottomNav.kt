package com.example.comalapp.ui.components.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.People
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

data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val adminNavItems = listOf(
    AdminNavItem("Panel", Icons.Outlined.Dashboard, AppDestinations.ADMIN_DASHBOARD),
    AdminNavItem("Productos", Icons.Outlined.Inventory2, AppDestinations.ADMIN_PRODUCTS),
    AdminNavItem("Ordenes", Icons.Outlined.ListAlt, AppDestinations.ADMIN_ORDERS),
    AdminNavItem("Usuarios", Icons.Outlined.People, AppDestinations.ADMIN_USERS),
    AdminNavItem("Personal", Icons.Outlined.Group, AppDestinations.ADMIN_WORKERS),
)

private val adminSecondaryRoutes = mapOf(
    AppDestinations.ADMIN_PRODUCT_FORM to AppDestinations.ADMIN_PRODUCTS,
    AppDestinations.ADMIN_ORDER_DETAIL to AppDestinations.ADMIN_ORDERS,
    AppDestinations.ADMIN_USER_DETAIL to AppDestinations.ADMIN_USERS,
)

@Composable
fun AdminBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeRoute = adminSecondaryRoutes[currentRoute] ?: currentRoute

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        adminNavItems.forEach { item ->
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