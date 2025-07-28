package com.example.v.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun SpeedTestGauge(
    currentSpeed: Float,
    maxSpeed: Float = 100f,
    label: String,
    unit: String = "Mbps",
    color: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeed,
        animationSpec = tween(2000, easing = EaseOutCubic),
        label = "speed_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawSpeedGauge(
                    speed = animatedSpeed,
                    maxSpeed = maxSpeed,
                    color = color
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", animatedSpeed),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun DrawScope.drawSpeedGauge(
    speed: Float,
    maxSpeed: Float,
    color: Color
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2 - 20.dp.toPx()
    val strokeWidth = 8.dp.toPx()

    // Background arc
    drawArc(
        color = Color.Gray.copy(alpha = 0.3f),
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Progress arc
    val sweepAngle = (speed / maxSpeed) * 270f
    drawArc(
        color = color,
        startAngle = 135f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Speed indicator needle
    val angle = 135f + sweepAngle
    val needleLength = radius * 0.8f
    val needleEnd = Offset(
        center.x + needleLength * cos(Math.toRadians(angle.toDouble())).toFloat(),
        center.y + needleLength * sin(Math.toRadians(angle.toDouble())).toFloat()
    )

    drawLine(
        color = color,
        start = center,
        end = needleEnd,
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Center dot
    drawCircle(
        color = color,
        radius = 6.dp.toPx(),
        center = center
    )
}