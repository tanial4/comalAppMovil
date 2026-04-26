package com.example.comalapp.ui.components.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comalapp.ui.theme.OrderStatusColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCard(
    title: String,
    type: String,
    createdAt: Date?,
    isRead: Boolean,
    modifier: Modifier = Modifier,
) {
    val icon = when (type) {
        "ready"     -> Icons.Outlined.Inventory2
        "preparing" -> Icons.Outlined.Timer
        "delivered" -> Icons.Outlined.CheckCircle
        else        -> Icons.Outlined.Timer
    }

    val iconTint = when (type) {
        "ready"     -> OrderStatusColor.ready
        "preparing" -> OrderStatusColor.prep
        "delivered" -> OrderStatusColor.delivered
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val formattedDate = createdAt?.let {
        SimpleDateFormat("d 'de' MMMM, HH:mm", Locale("es")).format(it)
    } ?: ""

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (formattedDate.isNotEmpty()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .then(
                            Modifier.padding(start = 8.dp)
                        ),
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = iconTint)
                    }
                }
            }
        }
    }
}