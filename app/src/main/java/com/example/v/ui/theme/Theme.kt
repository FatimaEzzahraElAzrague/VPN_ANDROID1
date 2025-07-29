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
    onPrimary = DarkBlack,
    primaryContainer = DarkOrangeCrayola.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,

    secondary = DarkGunmetal,
    onSecondary = Color.White,

    background = DarkOxfordBlue,
    onBackground = Color.White,

    surface = DarkGunmetalSecondary,
    onSurface = Color.White,

    error = Color(0xFFCF6679),
    onError = DarkBlack
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