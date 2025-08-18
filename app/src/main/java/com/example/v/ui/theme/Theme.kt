package com.example.v.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LightOrangeCrayola,
    onPrimary = LightWhite,
    primaryContainer = LightOrangeCrayola.copy(alpha = 0.2f),
    onPrimaryContainer = LightCharcoal,

    secondary = LightCadetGray,
    onSecondary = LightWhite,

    background = LightSeasalt,
    onBackground = LightCharcoal,

    surface = LightWhite,
    onSurface = LightCharcoal,

    error = Color(0xFFB00020),
    onError = LightWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkOrangeCrayola,
    onPrimary = Color.White,
    primaryContainer = DarkOrangeCrayola.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,

                secondary = Color(0xFF1A1A1A),
    onSecondary = Color.White,

    background = DarkOxfordBlue,
    onBackground = Color.White,

                surface = Color(0xFF1A1A1A),
    onSurface = DarkOnSurface,
                surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = DarkOnSurfaceSecondary,

    error = Color(0xFFCF6679),
    onError = Color.White
)

@Composable
fun VPNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}