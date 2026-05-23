package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ShonenColorScheme = darkColorScheme(
    primary = ShonenTextPrimary,
    secondary = NeonCyan,
    tertiary = NeonGreen,
    background = ShonenBackground,
    surface = ShonenSurface,
    onPrimary = ShonenTextInverse,
    onSecondary = ShonenTextInverse,
    onTertiary = ShonenTextInverse,
    onBackground = ShonenTextPrimary,
    onSurface = ShonenTextPrimary,
    surfaceVariant = ShonenSurfaceVariant,
    error = NeonRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force our high-contrast tactical dark theme as requested
    MaterialTheme(
        colorScheme = ShonenColorScheme,
        typography = Typography,
        content = content
    )
}
