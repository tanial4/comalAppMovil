package com.example.comalapp.ui.components.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Undo
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
import com.example.comalapp.ui.components.shared.AppButton
import com.example.comalapp.ui.components.shared.AppButtonVariant
import com.example.comalapp.ui.theme.ready

@Composable
fun OrderActionsCard(
    status: String,
    onAdvanceStatus: () -> Unit,
    onRevertStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val advanceLabel = when (status) {
        "pending"   -> "Iniciar preparación"
        "preparing" -> "Marcar como lista"
        "ready"     -> "Confirmar entrega"
        else        -> null
    }

    val advanceVariant = when (status) {
        "preparing" -> AppButtonVariant.Secondary
        else        -> AppButtonVariant.Primary
    }

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
            when (status) {
                "delivered" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = ready,
                            modifier = Modifier.size(20.dp),
                        )
                        Column {
                            Text(
                                text = "Orden completada",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Esta orden fue entregada exitosamente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = "Cambiar estado",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    advanceLabel?.let { label ->
                        AppButton(
                            text = label,
                            onClick = onAdvanceStatus,
                            variant = if (status == "preparing") AppButtonVariant.Primary
                            else AppButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}