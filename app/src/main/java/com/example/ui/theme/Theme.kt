package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ActitudFuerteDarkColorScheme = darkColorScheme(
    primary = LimeGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E2800),
    onPrimaryContainer = LimeGreen,
    secondary = TextLightGray,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextLightGray,
    tertiary = LimeGreenDark,
    onTertiary = Color.Black,
    background = BlackBackground,
    onBackground = TextLightGray,
    surface = SurfaceDark,
    onSurface = TextLightGray,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    outlineVariant = Color(0xFF1E1E1E),
    error = StatusSanctionedColor,
    onError = Color.White
)

@Composable
fun ActitudFuerteTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ActitudFuerteDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ActitudFuerteTheme(content = content)
}
