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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import com.example.v.components.AnimatedConnectButton
import kotlinx.coroutines.launch
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
    var isConnecting by remember { mutableStateOf(false) }
    var connectionDuration by remember { mutableStateOf(0L) }

    // Timer for connection duration
    LaunchedEffect(isConnected) {
        if (isConnected) {
            val startTime = System.currentTimeMillis()
            while (isConnected) {
                connectionDuration = System.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(1000)
            }
        } else {
            connectionDuration = 0L
        }
    }

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
            // VPN Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) {
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    } else {
                        Color(0xFFFF6B35).copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Security else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF6B35),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isConnected) "SECURE" else "NOT SECURE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF6B35)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Connection status (legacy text)
            Text(
                text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated Connect/Disconnect button
            AnimatedConnectButton(
                isConnected = isConnected,
                isConnecting = isConnecting,
                onClick = {
                    if (!isConnected && !isConnecting) {
                        isConnecting = true
                        // Simulate connection delay
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(3000)
                            isConnecting = false
                            onConnectToggle()
                        }
                    } else if (isConnected) {
                        onConnectToggle()
                    }
                }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Server info and connection details (only shown when connected)
            if (isConnected) {
                // Server selection card
                if (selectedServer != null) {
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
                } else {
                    // Default server info when none selected
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
                                text = "🌍",
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Selected Server",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Optimal location",
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection duration
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatDuration(connectionDuration),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // IP Address info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your IP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "185.243.218.27",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Data Used",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2.4 GB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
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