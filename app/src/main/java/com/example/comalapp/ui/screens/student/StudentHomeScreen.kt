package com.example.comalapp.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comalapp.data.model.Product
import com.example.comalapp.ui.components.student.ActiveOrderBanner
import com.example.comalapp.ui.components.student.CategoryCard
import com.example.comalapp.ui.components.student.ProductCard
import com.example.comalapp.ui.components.student.ProductDetailSheet
import com.example.comalapp.ui.components.student.QuickAccessCard
import com.example.comalapp.ui.components.student.StudentScaffold
import com.example.comalapp.ui.navigation.AppDestinations
import com.example.comalapp.ui.viewmodel.StudentHomeViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    currentRoute: String?,
    notificationCount: Int,
    cartItemCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onViewOrderStatus: (String) -> Unit,
    onAddToCart: (Product, Int) -> Unit,
    homeViewModel: StudentHomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11  -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else      -> "Buenas noches"
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            homeViewModel.clearError()
        }
    }

    StudentScaffold(
        currentRoute = currentRoute,
        label = greeting.uppercase(),
        title = uiState.user?.fullName?.split(" ")?.firstOrNull() ?: "Bienvenido",
        notificationCount = notificationCount,
        cartItemCount = cartItemCount,
        onNotificationsClick = onNotificationsClick,
        onCartClick = onCartClick,
        onNavigate = onNavigate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
        extraTopBarContent = {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Cafetería abierta · 7:00 am – 5:00 pm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
            }
        },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                uiState.activeOrder?.let { order ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        ActiveOrderBanner(
                            orderId = order.id,
                            status = order.status,
                            productCount = order.productCount,
                            estimatedMinutes = null,
                            onViewStatusClick = { onViewOrderStatus(order.id) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Acciones rápidas",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
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
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Lo más pedido",
                        onSeeAllClick = { onNavigate(AppDestinations.STUDENT_MENU) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.products) { product ->
                            ProductCard(
                                name = product.name,
                                price = product.price,
                                imageUrl = product.imageUrl,
                                onClick = { selectedProduct = product },
                                modifier = Modifier.width(160.dp),
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        selectedProduct?.let { product ->
            ProductDetailSheet(
                product = product,
                categoryName = "",
                onDismiss = { selectedProduct = null },
                onAddToCart = { p, qty -> onAddToCart(p, qty) },
                sheetState = sheetState,
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
        modifier = Modifier.fillMaxWidth(),
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