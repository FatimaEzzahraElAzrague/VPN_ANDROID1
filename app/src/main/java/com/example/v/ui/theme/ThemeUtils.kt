package com.example.v.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared theme utilities for consistent styling across the app
 */

@Composable
fun getGradientBackground(isDarkTheme: Boolean): Brush {
    return if (isDarkTheme) {
        Brush.radialGradient(
            colors = listOf(
                DarkBlack,
                DarkOxfordBlue,
                DarkGunmetal,
                DarkOxfordBlue,
                DarkBlack
            ),
            radius = 1200f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF), // Pure white
                Color(0xFFFFF8F5), // Very light orange tint
                Color(0xFFFFF0E6), // Light orange tint
                Color(0xFFFFE8D6), // Medium light orange
                Color(0xFFFFDCC4), // Medium orange
                Color(0xFFFFF0E6), // Light orange tint
                Color(0xFFFFF8F5), // Very light orange tint
                Color(0xFFFFFFFF)  // Pure white
            ),
            radius = 1200f
        )
    }
}

@Composable
fun getPrimaryTextColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) LightWhite else LightCharcoal
}

@Composable
fun getSecondaryTextColor(): Color {
    return LightCadetGray
}

@Composable
fun getCardBackgroundColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) DarkGunmetalSecondary else LightWhite
}

@Composable
fun getCircleColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) DarkGunmetal else LightCadetGray
}

@Composable
fun getSearchBackgroundColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) DarkGunmetal else LightWhite
}

@Composable
fun getOrangeColor(): Color {
    return OrangeCrayola
}

/**
 * Common button styling
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangeCrayola,
            contentColor = LightWhite
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = LightWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrangeCrayola
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Common card styling - now uses fine lines instead of cards
 */
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        content()
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            thickness = 0.5.dp,
            color = getSecondaryTextColor().copy(alpha = 0.3f)
        )
    }
}

/**
 * Common text field styling
 */
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeCrayola,
            unfocusedBorderColor = getSecondaryTextColor(),
            focusedLabelColor = OrangeCrayola,
            unfocusedLabelColor = getSecondaryTextColor(),
            cursorColor = OrangeCrayola
        ),
        singleLine = true
    )
}

/**
 * Theme toggle button styling
 */
@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onThemeToggle,
        modifier = modifier
            .size(48.dp)
            .background(
                color = getCardBackgroundColor(isDarkTheme),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle theme",
            tint = getPrimaryTextColor(isDarkTheme),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Common text styling
 */
@Composable
fun TitleText(
    text: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = getPrimaryTextColor(isDarkTheme),
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        modifier = modifier
    )
}

@Composable
fun SubtitleText(
    text: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = getSecondaryTextColor(),
        modifier = modifier
    )
}

@Composable
fun BodyText(
    text: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = getPrimaryTextColor(isDarkTheme),
        modifier = modifier
    )
}