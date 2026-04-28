package com.example.comalapp.ui.components.student

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StudentScaffold(
    currentRoute: String?,
    title: String,
    notificationCount: Int,
    cartItemCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    subtitle: String = "",
    snackbarHost: @Composable () -> Unit = {},
    extraTopBarContent: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
        topBar = {
            StudentTopBar(
                title = title,
                subtitle = subtitle,
                label = label,
                notificationCount = notificationCount,
                cartItemCount = cartItemCount,
                onNotificationsClick = onNotificationsClick,
                onCartClick = onCartClick,
                extraContent = extraTopBarContent,
            )
        },
        bottomBar = {
            StudentBottomNav(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
            )
        },
        content = content,
    )
}