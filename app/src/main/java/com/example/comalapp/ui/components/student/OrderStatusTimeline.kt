package com.example.comalapp.ui.components.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class OrderStep(
    val status: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val orderSteps = listOf(
    OrderStep("pending",   "Recibida",          "Esperando confirmación", Icons.Outlined.Timer),
    OrderStep("preparing", "En preparación",    "Pendiente",              Icons.Outlined.Restaurant),
    OrderStep("ready",     "Lista para recoger","Pendiente",              Icons.Outlined.Inventory2),
    OrderStep("delivered", "Entregada",         "Pendiente",              Icons.Outlined.CheckCircle),
)

private val statusOrder = listOf("pending", "preparing", "ready", "delivered")

@Composable
fun OrderStatusTimeline(
    currentStatus: String,
    modifier: Modifier = Modifier,
) {
    val currentIndex = statusOrder.indexOf(currentStatus)

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
            Text(
                text = "Estado del pedido",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            orderSteps.forEachIndexed { index, step ->
                val stepIndex = statusOrder.indexOf(step.status)
                val isCompleted = stepIndex <= currentIndex
                val isCurrent = stepIndex == currentIndex

                val iconBackground = when {
                    isCurrent  -> MaterialTheme.colorScheme.primary
                    isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else        -> MaterialTheme.colorScheme.surfaceVariant
                }

                val iconTint = when {
                    isCurrent || isCompleted -> MaterialTheme.colorScheme.onPrimary
                    else                     -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val labelColor = when {
                    isCompleted -> MaterialTheme.colorScheme.onSurface
                    else        -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val subtitleText = when {
                    isCurrent  -> step.subtitle.replace("Pendiente", getActiveSubtitle(step.status))
                    isCompleted -> "Completado"
                    else        -> "Pendiente"
                }

                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = iconBackground,
                                    shape = MaterialTheme.shapes.medium,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        if (index < orderSteps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(
                                        color = if (stepIndex < currentIndex)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else
                                            MaterialTheme.colorScheme.outline,
                                    ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = labelColor,
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun getActiveSubtitle(status: String): String = when (status) {
    "pending"   -> "Esperando confirmación"
    "preparing" -> "Tu orden está siendo preparada"
    "ready"     -> "Pasa a recoger tu orden"
    "delivered" -> "Orden entregada"
    else        -> ""
}