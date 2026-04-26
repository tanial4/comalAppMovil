package com.example.comalapp.ui.components.student

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StudentScaffold(
    currentRoute: String,
    notificationCount: Int,
    cartItemCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            StudentTopBar(
                notificationCount = notificationCount,
                cartItemCount = cartItemCount,
                onNotificationsClick = onNotificationsClick,
                onCartClick = onCartClick,
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