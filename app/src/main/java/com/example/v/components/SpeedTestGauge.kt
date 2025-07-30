// In components/SpeedTestGauge.kt
package com.example.v.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedTestGauge(
    downloadSpeed: Float,
    uploadSpeed: Float,
    ping: Float,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Colors for each gauge
    val downloadColor = Color(0xFF4CAF50) // Green
    val uploadColor = Color(0xFF9C27B0)   // Purple
    val pingColor = Color(0xFFFF9800)     // Orange

    // Animation for needle movement
    val downloadProgress by animateFloatAsState(
        targetValue = downloadSpeed / 150f, // Max 150 Mbps
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f),
        label = "downloadNeedle"
    )

    val uploadProgress by animateFloatAsState(
        targetValue = uploadSpeed / 50f, // Max 50 Mbps
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f),
        label = "uploadNeedle"
    )

    val pingProgress by animateFloatAsState(
        targetValue = ping / 200f, // Max 200 ms
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f),
        label = "pingNeedle"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Download Speed Gauge
        SingleGauge(
            value = downloadSpeed,
            maxValue = 150f,
            unit = "Mbps",
            label = "Download",
            color = downloadColor,
            progress = downloadProgress,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.size(160.dp)
        )

        // Upload Speed Gauge
        SingleGauge(
            value = uploadSpeed,
            maxValue = 50f,
            unit = "Mbps",
            label = "Upload",
            color = uploadColor,
            progress = uploadProgress,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.size(160.dp)
        )

        // Ping Gauge
        SingleGauge(
            value = ping,
            maxValue = 200f,
            unit = "ms",
            label = "Ping",
            color = pingColor,
            progress = pingProgress,
            isDarkTheme = isDarkTheme,
            isLowerBetter = true,
            modifier = Modifier.size(160.dp)
        )
    }
}

@Composable
private fun SingleGauge(
    value: Float,
    maxValue: Float,
    unit: String,
    label: String,
    color: Color,
    progress: Float,
    isDarkTheme: Boolean,
    isLowerBetter: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sweepAngle = 240f
    val startAngle = 150f
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val gaugeBackground = if (isDarkTheme) Color(0xFF2D2D2D) else Color.LightGray

    // Calculate needle angle (0.5 means middle)
    val needleAngle = startAngle + (sweepAngle * progress.coerceIn(0f, 1f))

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 20f

            // Draw gauge background arc
            drawArc(
                color = gaugeBackground,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 24f)
            )

            // Draw progress arc
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 24f)
            )

            // Draw needle
            val needleLength = radius * 0.8f
            val needleEnd = Offset(
                center.x + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat(),
                center.y + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
            )

            drawLine(
                color = Color.Red,
                start = center,
                end = needleEnd,
                strokeWidth = 4f
            )

            // Draw needle center
            drawCircle(
                color = backgroundColor,
                radius = 8f,
                center = center
            )
            drawCircle(
                color = Color.Red,
                radius = 4f,
                center = center
            )

            // Draw scale markers
            val markerLength = 12f
            val markerRadius = radius - 12f
            for (i in 0..10) {
                val angle = startAngle + (sweepAngle * i / 10f)
                val markerStart = Offset(
                    center.x + (markerRadius - markerLength) * cos(Math.toRadians(angle.toDouble())).toFloat(),
                    center.y + (markerRadius - markerLength) * sin(Math.toRadians(angle.toDouble())).toFloat()
                )
                val markerEnd = Offset(
                    center.x + markerRadius * cos(Math.toRadians(angle.toDouble())).toFloat(),
                    center.y + markerRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                )

                drawLine(
                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                    start = markerStart,
                    end = markerEnd,
                    strokeWidth = 2f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp)
        ) {
            Text(
                text = "%.1f".format(value),
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.6f)
            )

            // Quality indicator for ping
            if (isLowerBetter) {
                Spacer(modifier = Modifier.height(4.dp))
                val quality = when {
                    value < 50 -> "Excellent"
                    value < 100 -> "Good"
                    value < 150 -> "Fair"
                    else -> "Poor"
                }
                val qualityColor = when {
                    value < 50 -> Color.Green
                    value < 100 -> Color(0xFF4CAF50)
                    value < 150 -> Color.Yellow
                    else -> Color.Red
                }
                Text(
                    text = quality,
                    style = MaterialTheme.typography.labelSmall,
                    color = qualityColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .background(qualityColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

// Helper function to convert degrees to radians
private fun Math.toRadians(degrees: Double): Double {
    return degrees * Math.PI / 180
}