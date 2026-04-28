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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class ProgressStep(
    val status: String,
    val label: String,
    val icon: ImageVector,
)

private val progressSteps = listOf(
    ProgressStep("pending",   "Recibida",         Icons.Outlined.Timer),
    ProgressStep("preparing", "En\npreparación",  Icons.Outlined.Restaurant),
    ProgressStep("ready",     "Lista para\nrecoger", Icons.Outlined.Inventory2),
    ProgressStep("delivered", "Entregada",        Icons.Outlined.CheckCircle),
)

private val statusOrder = listOf("pending", "preparing", "ready", "delivered")

@Composable
fun OrderProgressBar(
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
                text = "Progreso",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                progressSteps.forEachIndexed { index, step ->
                    val stepIndex = statusOrder.indexOf(step.status)
                    val isCompleted = stepIndex <= currentIndex
                    val isCurrent = stepIndex == currentIndex

                    val circleColor = when {
                        isCurrent || isCompleted -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val iconTint = when {
                        isCurrent || isCompleted -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = circleColor,
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            minLines = 2,
                            maxLines = 2,
                        )
                    }

                    if (index < progressSteps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .height(2.dp)
                                .padding(bottom = 22.dp)
                                .background(
                                    color = if (stepIndex < currentIndex)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline,
                                ),
                        )
                    }
                }
            }
        }
    }
}