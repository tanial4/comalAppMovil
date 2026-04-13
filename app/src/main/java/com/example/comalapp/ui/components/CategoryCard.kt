package com.example.comalapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CategoryCard(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val shape = MaterialTheme.shapes.medium

    Card(
        modifier = modifier
            .clip(shape)
            .clickable { onClick() }
            .then(
                if (selected) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = shape,
                ) else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}