package com.example.v.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.v.components.WebStyleConnectButton

@Composable
fun EngineStyleSpeedTest(
    download: Float,
    upload: Float,
    ping: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedGauge(speed = download, label = "DOWNLOAD", color = Color(0xFF4CAF50))
        SpeedGauge(speed = ping, label = "PING", color = Color(0xFFFF9800))
        SpeedGauge(speed = upload, label = "UPLOAD", color = Color(0xFFE040FB))
    }
}

@Composable
fun SpeedGauge(speed: Float, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val strokeWidth = 15.dp.toPx()

            // Background arc (gauge)
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground arc (progress)
            val sweepAngle = (270f * (speed / 100f)).coerceIn(0f, 270f)
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Needle position
            val needleAngle = Math.toRadians((135 + sweepAngle).toDouble())
            val needleLength = radius - strokeWidth * 1.5f
            val center = Offset(size.width / 2, size.height / 2)
            val needleEnd = Offset(
                (center.x + needleLength * kotlin.math.cos(needleAngle)).toFloat(),
                (center.y + needleLength * kotlin.math.sin(needleAngle)).toFloat()
            )

            drawLine(
                color = color,
                start = center,
                end = needleEnd,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = color.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
            Text(
                text = if (label == "PING") "${speed.toInt()} ms" else "${speed.toInt()} Mbps",
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}