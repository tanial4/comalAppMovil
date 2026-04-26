package com.example.comalapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary              = primary,
    onPrimary            = backgroundCard,
    primaryContainer     = blueAccent,
    onPrimaryContainer   = backgroundCard,

    secondary            = blueHighlight,
    onSecondary          = backgroundCard,
    secondaryContainer   = violet,
    onSecondaryContainer = backgroundCard,

    background           = background,
    onBackground         = primaryText,

    surface              = backgroundCard,
    onSurface            = primaryText,
    surfaceVariant       = mutedBackground,
    onSurfaceVariant     = middleText,

    outline              = secondaryBackground,
    outlineVariant       = mutedBackground,

    error                = danger,
    onError              = backgroundCard,


)

@Composable
fun ComalAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}