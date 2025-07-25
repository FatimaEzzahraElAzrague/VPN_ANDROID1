package com.example.v.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.models.Server
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isConnected: Boolean,
    selectedServer: Server?,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onConnectToggle: () -> Unit,
    onServerClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    } else {
                        listOf(
                            Color(0xFFF0F4F8),
                            Color(0xFFE2E8F0)
                        )
                    }
                )
            )
    ) {
        // World map background
        WorldMapBackground(isDarkTheme = isDarkTheme)

        // Theme toggle button
        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Connection status
            Text(
                text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Connect/Disconnect button
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isConnected) {
                                listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                            } else {
                                listOf(Color(0xFFFF6B35), Color(0xFFE65100))
                            }
                        )
                    )
                    .clickable { onConnectToggle() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Power,
                        contentDescription = if (isConnected) "Disconnect" else "Connect",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isConnected) "DISCONNECT" else "CONNECT",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Server info (only shown when connected)
            if (isConnected && selectedServer != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServerClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedServer.flagEmoji,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedServer.country,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedServer.city,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Change server",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Connection details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Connection Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ConnectionDetailRow("Local IP", "192.168.1.100")
                        ConnectionDetailRow("Server IP", "185.243.218.27")
                        ConnectionDetailRow("Session Duration", "00:15:32")
                        ConnectionDetailRow("Data Transfer", "2.4 MB ↓ / 1.1 MB ↑")
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WorldMapBackground(isDarkTheme: Boolean) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawWorldMap(isDarkTheme)
    }
}

fun DrawScope.drawWorldMap(isDarkTheme: Boolean) {
    val mapColor = if (isDarkTheme) Color(0xFF2A2A3E) else Color(0xFFE2E8F0)
    val dotColor = if (isDarkTheme) Color(0xFF3A3A5E) else Color(0xFFCBD5E0)

    // Draw simplified world map dots
    val dotRadius = 2.dp.toPx()
    val spacing = 40.dp.toPx()

    for (x in 0 until (size.width / spacing).toInt()) {
        for (y in 0 until (size.height / spacing).toInt()) {
            val xPos = x * spacing + (spacing / 2)
            val yPos = y * spacing + (spacing / 2)

            // Create a simple world map pattern
            if (isLandArea(xPos / size.width, yPos / size.height)) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(xPos, yPos)
                )
            }
        }
    }
}

fun isLandArea(x: Float, y: Float): Boolean {
    // Simplified world map logic
    return when {
        // North America
        x in 0.1f..0.35f && y in 0.2f..0.5f -> true
        // Europe
        x in 0.45f..0.6f && y in 0.2f..0.4f -> true
        // Asia
        x in 0.6f..0.9f && y in 0.15f..0.6f -> true
        // Africa
        x in 0.45f..0.6f && y in 0.4f..0.8f -> true
        // South America
        x in 0.25f..0.4f && y in 0.5f..0.9f -> true
        // Australia
        x in 0.75f..0.85f && y in 0.7f..0.8f -> true
        else -> false
    }
}