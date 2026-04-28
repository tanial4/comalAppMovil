package com.example.comalapp.ui.components.admin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdminScaffold(
    currentRoute: String?,
    title: String,
    userName: String,
    userEmail: String,
    onLogout: () -> Unit,
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
            AdminTopBar(
                title = title,
                subtitle = subtitle,
                label = label,
                userName = userName,
                userEmail = userEmail,
                onLogout = onLogout,
            )
        },
        bottomBar = {
            AdminBottomNav(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
            )
        },
        content = content,
    )
}