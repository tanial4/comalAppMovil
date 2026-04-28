package com.example.comalapp.ui.components.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comalapp.ui.theme.OrderStatusColor

@Composable
fun ActiveOrderBanner(
    orderId: String,
    status: String,
    productCount: Int,
    estimatedMinutes: Int?,
    onViewStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (status) {
        "pending"   -> "Pendiente"
        "preparing" -> "En preparación"
        "ready"     -> "Listo para recoger"
        else        -> status
    }

    val statusColor = when (status) {
        "pending"   -> OrderStatusColor.pendient
        "preparing" -> OrderStatusColor.prep
        "ready"     -> OrderStatusColor.ready
        else        -> OrderStatusColor.delivered
    }

    val subtitle = buildString {
        append("$productCount ${if (productCount == 1) "producto" else "productos"}")
        if (estimatedMinutes != null) append(" · listo en ~$estimatedMinutes min")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ListAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp),
                )

                Column {
                    Text(
                        text = "Orden activa · #$orderId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = statusColor,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    )
                }
            }

            TextButton(onClick = onViewStatusClick) {
                Text(
                    text = "Ver estado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}