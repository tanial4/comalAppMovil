package com.example.comalapp.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.comalapp.data.model.Order
import com.example.comalapp.ui.theme.OrderStatusColor

data class OrderStatusCount(
    val label: String,
    val count: Int,
    val color: Color,
)

@Composable
fun OrderStatusDistributionCard(
    orders: List<Order>,
    modifier: Modifier = Modifier,
) {
    val total = orders.size.takeIf { it > 0 } ?: 1

    val statusCounts = listOf(
        OrderStatusCount("Pendiente",     orders.count { it.status == "pending" },   OrderStatusColor.pendient),
        OrderStatusCount("En preparación",orders.count { it.status == "preparing" }, OrderStatusColor.prep),
        OrderStatusCount("Lista",         orders.count { it.status == "ready" },     OrderStatusColor.ready),
        OrderStatusCount("Entregada",     orders.count { it.status == "delivered" }, OrderStatusColor.delivered),
    )

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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Estado de órdenes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            statusCounts.forEach { item ->
                StatusDistributionRow(
                    label = item.label,
                    count = item.count,
                    fraction = item.count.toFloat() / total,
                    color = item.color,
                )
            }
        }
    }
}

@Composable
private fun StatusDistributionRow(
    label: String,
    count: Int,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color),
            )
        }
    }
}