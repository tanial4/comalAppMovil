package com.example.comalapp.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.comalapp.ui.components.student.NotificationCard
import com.example.comalapp.ui.components.student.StudentScaffold
import com.example.comalapp.ui.viewmodel.StudentNotificationsViewModel

@Composable
fun StudentNotificationsScreen(
    currentRoute: String?,
    notificationCount: Int,
    cartItemCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onOrderClick: (String) -> Unit,
    notificationsViewModel: StudentNotificationsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        notificationsViewModel.markAllAsRead()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            notificationsViewModel.clearError()
        }
    }

    StudentScaffold(
        currentRoute = currentRoute,
        label = "ESTUDIANTE",
        title = "Notificaciones",
        notificationCount = notificationCount,
        cartItemCount = cartItemCount,
        onNotificationsClick = onNotificationsClick,
        onCartClick = onCartClick,
        onNavigate = onNavigate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin notificaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Aquí aparecerán las actualizaciones de tus órdenes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.notifications,
                        key = { it.id },
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                if (!notification.read) {
                                    notificationsViewModel.markAsRead(notification.id)
                                }
                                if (notification.orderId.isNotEmpty()) {
                                    onOrderClick(notification.orderId)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}