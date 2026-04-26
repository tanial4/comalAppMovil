package com.example.comalapp.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comalapp.ui.components.shared.AppButton
import com.example.comalapp.ui.components.student.CartItemCard
import com.example.comalapp.ui.components.student.StudentScaffold
import com.example.comalapp.ui.viewmodel.StudentCartViewModel
import com.example.comalapp.ui.navigation.AppDestinations
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon

@Composable
fun StudentCartScreen(
    currentRoute: String?,
    notificationCount: Int,
    cartItemCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onOrderConfirmed: () -> Unit,
    cartViewModel: StudentCartViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearError()
        }
    }

    LaunchedEffect(uiState.orderConfirmed) {
        if (uiState.orderConfirmed) {
            cartViewModel.resetConfirmed()
            onOrderConfirmed()
        }
    }

    StudentScaffold(
        currentRoute = currentRoute,
        label = "MI CARRITO",
        title = "${uiState.totalItemCount} ${if (uiState.totalItemCount == 1) "producto" else "productos"}",
        notificationCount = notificationCount,
        cartItemCount = cartItemCount,
        onNotificationsClick = onNotificationsClick,
        onCartClick = onCartClick,
        onNavigate = onNavigate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.items.isEmpty() && !uiState.isLoading) {
                EmptyCartState(
                    onGoToMenu = { onNavigate(AppDestinations.STUDENT_MENU) },
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.product.id },
                        ) { item ->
                            CartItemCard(
                                name = item.product.name,
                                price = item.product.price,
                                imageUrl = item.product.imageUrl,
                                quantity = item.quantity,
                                onIncrement = { cartViewModel.increment(item.product.id) },
                                onDecrement = { cartViewModel.decrement(item.product.id) },
                                onDelete = { cartViewModel.delete(item.product.id) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "$${"%.2f".format(uiState.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "$${"%.2f".format(uiState.subtotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AppButton(
                        text = "Confirmar orden",
                        onClick = { cartViewModel.confirmOrder() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EmptyCartState(
    onGoToMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingBag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tu carrito está vacío",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Agrega productos del menú para comenzar tu orden",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppButton(
            text = "Ver menú",
            onClick = onGoToMenu,
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}