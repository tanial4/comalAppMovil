package com.example.comalapp.ui.components.student

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comalapp.ui.components.shared.BrandLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTopBar(
    notificationCount: Int,
    cartItemCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        title = {
            BrandLogo(
                modifier = Modifier.size(width = 120.dp, height = 40.dp),
            )
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ) {
                                Text(
                                    text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = null,
                    )
                }
            }

            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ) {
                                Text(
                                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}