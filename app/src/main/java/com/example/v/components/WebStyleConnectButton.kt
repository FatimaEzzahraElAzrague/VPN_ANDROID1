package com.example.v.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WebStyleConnectButton(
    isConnected: Boolean,
    isConnecting: Boolean = false,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryOrange = Color(0xFFFF6C36)
    val isDarkTheme = MaterialTheme.colorScheme.surface == Color(0xFF1A1D2E)
    
    // Determine button state and colors
    val buttonColor = when {
        isConnecting -> primaryOrange.copy(alpha = 0.7f)
        isConnected -> Color(0xFF34A853) // Green when connected
        else -> primaryOrange
    }
    
    val iconColor = Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF2A2D3E)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main circular button - large size like web app
        Box(
            modifier = Modifier
                .size(180.dp) // Large size to match web app
                .clip(CircleShape)
                .background(buttonColor)
                .clickable(enabled = !isConnecting) { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                // Show loading indicator when connecting
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            } else {
                // Show power icon
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = if (isConnected) "Disconnect" else "Connect",
                    tint = iconColor,
                    modifier = Modifier.size(48.dp) // Large icon to match web app
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button text - "CONNECT" like web app
        Text(
            text = when {
                isConnecting -> "CONNECTING..."
                isConnected -> "CONNECTED"
                else -> "CONNECT"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isConnected) {
                if (isDarkTheme) Color.White else Color(0xFF34A853)
            } else {
                textColor
            }
        )

        // Optional: Show connection time when connected
        if (isConnected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "00:00:00", // You can replace with actual connection time
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 