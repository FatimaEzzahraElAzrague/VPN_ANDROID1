// components/ConnectionButton.kt
package com.example.v.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ConnectionButton(
    isConnected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryOrange = Color(0xFFFF6C36)
    val isDarkTheme = MaterialTheme.colorScheme.surface == Color(0xFF1A1D2E)
    val buttonColor = if (isConnected) primaryOrange else Color(0xFF2A2D3E)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF2A2D3E)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main circular button
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isConnected) {
                // Connected state - show power icon
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = "Disconnect",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Disconnected state - show power icon outline
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = "Connect",
                    tint = if (isDarkTheme) Color.White else primaryOrange,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button text
        Text(
            text = if (isConnected) "CONNECTED" else "TAP TO CONNECT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isConnected) primaryOrange else textColor
        )

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