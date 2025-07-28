package com.example.v.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimatedConnectButton(
    isConnected: Boolean,
    isConnecting: Boolean = false,
    daysLeft: Int = 45,
    selectedRegion: String = "France, Paris",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "connect_button")
    val ghostShieldBlue = Color(0xFF4285F4)
    val ghostShieldDarkBlue = Color(0xFF1A73E8)
    val ghostShieldGreen = Color(0xFF34A853)
    val ghostShieldDarkGreen = Color(0xFF1E8E3E)

    // Animation values
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isConnecting) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isConnected) 0.7f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Premium badge
        Text(
            text = "Premium • ${daysLeft}d left",
            color = ghostShieldBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Region selector
        Text(
            text = selectedRegion,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Main button container
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            if (isConnected) {
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                ) {
                    drawGlowEffect(ghostShieldGreen.copy(alpha = glowAlpha))
                }
            }

            // Button
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = when {
                                isConnecting -> listOf(ghostShieldBlue, ghostShieldDarkBlue)
                                isConnected -> listOf(ghostShieldGreen, ghostShieldDarkGreen)
                                else -> listOf(ghostShieldBlue, ghostShieldDarkBlue)
                            },
                            center = Offset(0.3f, 0.3f)
                        )
                    )
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shield icon with connection animation
                    Box(
                        modifier = Modifier.size(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Shield base
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawShieldIcon(
                                color = Color.White,
                                isConnected = isConnected,
                                isConnecting = isConnecting
                            )
                        }

                        // Animated checkmark when connected
                        if (isConnected) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(pulseScale * 0.9f)
                            ) {
                                drawCheckmark(Color.White)
                            }
                        }

                        // Loading animation when connecting
                        if (isConnecting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .rotate(rotation)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Connection status text
                    Text(
                        text = when {
                            isConnecting -> "CONNECTING..."
                            isConnected -> "DISCONNECT"
                            else -> "CONNECT NOW"
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Connection details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connection Details",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ConnectionDetailRow(
                label = "Status",
                value = if (isConnected) "Connected" else "Disconnected"
            )
            ConnectionDetailRow(
                label = "Local IP",
                value = if (isConnected) "192.168.1.100" else "--"
            )
            ConnectionDetailRow(
                label = "Server IP",
                value = if (isConnected) "185.159.157.12" else "--"
            )
            ConnectionDetailRow(
                label = "Session Duration",
                value = if (isConnected) "00:00:00" else "--"
            )
        }
    }
}

@Composable
private fun ConnectionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun DrawScope.drawShieldIcon(color: Color, isConnected: Boolean, isConnecting: Boolean) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val shieldWidth = size.width * 0.7f
    val shieldHeight = size.height * 0.8f

    // Shield body
    drawPath(
        path = Path().apply {
            moveTo(centerX, centerY - shieldHeight / 2)
            quadraticBezierTo(
                centerX + shieldWidth / 2,
                centerY - shieldHeight / 4,
                centerX + shieldWidth / 2,
                centerY
            )
            quadraticBezierTo(
                centerX + shieldWidth / 2,
                centerY + shieldHeight / 3,
                centerX,
                centerY + shieldHeight / 2
            )
            quadraticBezierTo(
                centerX - shieldWidth / 2,
                centerY + shieldHeight / 3,
                centerX - shieldWidth / 2,
                centerY
            )
            quadraticBezierTo(
                centerX - shieldWidth / 2,
                centerY - shieldHeight / 4,
                centerX,
                centerY - shieldHeight / 2
            )
        },
        color = color,
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawCheckmark(color: Color) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val size = size.width * 0.4f

    drawPath(
        path = Path().apply {
            moveTo(centerX - size * 0.3f, centerY)
            lineTo(centerX - size * 0.1f, centerY + size * 0.2f)
            lineTo(centerX + size * 0.3f, centerY - size * 0.2f)
        },
        color = color,
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawGlowEffect(color: Color) {
    val center = Offset(size.width / 2, size.height / 2)
    val maxRadius = size.minDimension / 2

    // Glow layers
    for (i in 1..5) {
        val radius = maxRadius * (0.5f + 0.1f * i)
        drawCircle(
            color = color.copy(alpha = color.alpha / i),
            radius = radius,
            center = center,
            blendMode = BlendMode.Screen
        )
    }
}