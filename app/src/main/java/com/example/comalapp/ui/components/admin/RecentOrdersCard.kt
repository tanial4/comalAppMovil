package com.example.comalapp.ui.components.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.comalapp.data.model.Order
import com.example.comalapp.ui.theme.OrderStatusColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentOrdersCard(
    orders: List<Order>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Órdenes recientes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onSeeAll) {
                    Text(
                        text = "Ver todas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            orders.forEach { order ->
                RecentOrderRow(order = order)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RecentOrderRow(
    order: Order,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (order.status) {
        "pending"   -> "Pendiente"
        "preparing" -> "En preparación"
        "ready"     -> "Lista"
        "delivered" -> "Entregada"
        "cancelled" -> "Cancelada"
        else        -> order.status
    }

    val statusColor = when (order.status) {
        "pending"   -> OrderStatusColor.pendient
        "preparing" -> OrderStatusColor.prep
        "ready"     -> OrderStatusColor.ready
        "delivered" -> OrderStatusColor.delivered
        else        -> MaterialTheme.colorScheme.error
    }

    val timeAgo = order.createdAt?.toDate()?.let { getTimeAgo(it) } ?: ""

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "#${order.id.takeLast(5).uppercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.1f),
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = statusColor.copy(alpha = 0.3f),
                    ),
                    modifier = Modifier.height(24.dp),
                )
            }
            Text(
                text = "Estudiante",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${"%.2f".format(order.total)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun getTimeAgo(date: Date): String {
    val diff = System.currentTimeMillis() - date.time
    val minutes = diff / 60000
    return when {
        minutes < 1  -> "ahora"
        minutes < 60 -> "hace $minutes min"
        else         -> "hace ${minutes / 60} h"
    }
}