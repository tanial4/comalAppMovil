package com.example.comalapp.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comalapp.data.model.Product
import com.example.comalapp.ui.components.student.ActiveOrderBanner
import com.example.comalapp.ui.components.student.CategoryCard
import com.example.comalapp.ui.components.student.ProductCard
import com.example.comalapp.ui.components.student.QuickAccessCard
import com.example.comalapp.ui.components.student.StudentScaffold
import com.example.comalapp.ui.navigation.AppDestinations
import java.util.Calendar

@Composable
fun StudentHomeScreen(
    currentRoute: String,
    notificationCount: Int,
    cartItemCount: Int,
    userName: String,
    hasActiveOrder: Boolean,
    activeOrderId: String,
    activeOrderStatus: String,
    activeOrderProductCount: Int,
    activeOrderEstimatedMinutes: Int?,
    products: List<Product>,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onViewOrderStatus: () -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    StudentScaffold(
        currentRoute = currentRoute,
        notificationCount = notificationCount,
        cartItemCount = cartItemCount,
        onNotificationsClick = onNotificationsClick,
        onCartClick = onCartClick,
        onNavigate = onNavigate,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            GreetingHeader(
                userName = userName,
                modifier = Modifier.padding(innerPadding),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                if (hasActiveOrder) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ActiveOrderBanner(
                        orderId = activeOrderId,
                        status = activeOrderStatus,
                        productCount = activeOrderProductCount,
                        estimatedMinutes = activeOrderEstimatedMinutes,
                        onViewStatusClick = onViewOrderStatus,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Acciones rápidas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuickAccessCard(
                        label = "Ver menú",
                        icon = Icons.Outlined.RestaurantMenu,
                        iconBackgroundColor = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigate(AppDestinations.STUDENT_MENU) },
                        modifier = Modifier.weight(1f),
                    )
                    QuickAccessCard(
                        label = "Mi carrito",
                        icon = Icons.Outlined.ShoppingCart,
                        iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onCartClick,
                        modifier = Modifier.weight(1f),
                    )
                    QuickAccessCard(
                        label = "Mis órdenes",
                        icon = Icons.Outlined.ListAlt,
                        iconBackgroundColor = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigate(AppDestinations.STUDENT_ORDER_HISTORY) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader(
                    title = "Categorías",
                    onSeeAllClick = { onNavigate(AppDestinations.STUDENT_MENU) },
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    CategoryCard(
                        label = "Cafés",
                        icon = Icons.Outlined.RestaurantMenu,
                        iconTint = androidx.compose.ui.graphics.Color(0xFFB45309),
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFFFEF3C7),
                        onClick = { },
                    )
                }
                item {
                    CategoryCard(
                        label = "Comida",
                        icon = Icons.Outlined.RestaurantMenu,
                        iconTint = androidx.compose.ui.graphics.Color(0xFF92400E),
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFFFEF3C7),
                        onClick = { },
                    )
                }
                item {
                    CategoryCard(
                        label = "Bebidas",
                        icon = Icons.Outlined.RestaurantMenu,
                        iconTint = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        onClick = { },
                        selected = true,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader(
                    title = "Lo más pedido",
                    onSeeAllClick = { onNavigate(AppDestinations.STUDENT_MENU) },
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(products) { product ->
                    ProductCard(
                        name = product.name,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        onAddToCart = { onAddToCart(product) },
                        modifier = Modifier.width(160.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GreetingHeader(
    userName: String,
    modifier: Modifier = Modifier,
) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11  -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else      -> "Buenas noches"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Text(
            text = greeting.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userName.ifBlank { "👋" },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.height(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Cafetería abierta · 7:00 am – 5:00 pm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "Ver todo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}