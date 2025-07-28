package com.example.v.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedConnectButton(
    isConnected: Boolean,
    isConnecting: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "connect_button")

    // Pulse animation when connected
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotation animation when connecting
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isConnecting) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Ripple effect
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isConnected) 0.3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ripple"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ripple effect background
        if (isConnected) {
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
            ) {
                drawRippleEffect(rippleAlpha)
            }
        }

        // Main button
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = when {
                            isConnecting -> listOf(Color(0xFFFF9800), Color(0xFFE65100))
                            isConnected -> listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                            else -> listOf(Color(0xFFFF6B35), Color(0xFFE65100))
                        }
                    )
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (isConnecting) Modifier.rotate(rotation) else Modifier
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        isConnecting -> "CONNECTING..."
                        isConnected -> "DISCONNECT"
                        else -> "CONNECT"
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun DrawScope.drawRippleEffect(alpha: Float) {
    val center = Offset(size.width / 2, size.height / 2)
    val maxRadius = size.minDimension / 2

    for (i in 1..3) {
        val radius = maxRadius * (0.6f + 0.2f * i) * alpha
        drawCircle(
            color = Color(0xFF4CAF50).copy(alpha = alpha / i),
            radius = radius,
            center = center
        )
    }
}