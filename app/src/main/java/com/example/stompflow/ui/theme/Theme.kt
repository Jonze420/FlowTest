package com.example.stompflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = TextPrimary,
    primaryContainer = OrangeLight,
    onPrimaryContainer = DarkBackground,
    secondary = AccentBlue,
    onSecondary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = AccentRed,
    onError = TextPrimary
)

@Composable
fun StompFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
