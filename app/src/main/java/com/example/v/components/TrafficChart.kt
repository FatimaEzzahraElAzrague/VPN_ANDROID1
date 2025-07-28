package com.example.v.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.v.screens.TrafficDataPoint

@Composable
fun TrafficChart(
    trafficData: List<TrafficDataPoint>,
    modifier: Modifier = Modifier,
    showDownload: Boolean = true,
    showUpload: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2000, easing = EaseOutCubic),
        label = "chart_animation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Traffic Analysis",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (showDownload) {
                    LegendItem("Download", Color(0xFF4CAF50))
                }
                if (showUpload) {
                    LegendItem("Upload", Color(0xFF2196F3))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawTrafficChart(
                    trafficData = trafficData,
                    progress = animatedProgress,
                    showDownload = showDownload,
                    showUpload = showUpload
                )
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun DrawScope.drawTrafficChart(
    trafficData: List<TrafficDataPoint>,
    progress: Float,
    showDownload: Boolean,
    showUpload: Boolean
) {
    if (trafficData.isEmpty()) return

    val padding = 40f
    val chartWidth = size.width - 2 * padding
    val chartHeight = size.height - 2 * padding

    val maxDownload = trafficData.maxOfOrNull { it.downloadSpeed } ?: 1f
    val maxUpload = trafficData.maxOfOrNull { it.uploadSpeed } ?: 1f
    val maxValue = maxOf(maxDownload, maxUpload)

    val stepX = chartWidth / (trafficData.size - 1).coerceAtLeast(1)

    // Draw grid lines
    drawGridLines(padding, chartWidth, chartHeight, maxValue)

    // Draw download line
    if (showDownload) {
        val downloadPath = Path()
        trafficData.forEachIndexed { index, data ->
            val x = padding + index * stepX
            val y = padding + chartHeight - (data.downloadSpeed / maxValue * chartHeight)

            if (index == 0) {
                downloadPath.moveTo(x, y)
            } else {
                downloadPath.lineTo(x, y)
            }
        }

        drawPath(
            path = downloadPath,
            color = Color(0xFF4CAF50),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    intervals = floatArrayOf(0f, chartWidth * (1f - progress))
                )
            )
        )
    }

    // Draw upload line
    if (showUpload) {
        val uploadPath = Path()
        trafficData.forEachIndexed { index, data ->
            val x = padding + index * stepX
            val y = padding + chartHeight - (data.uploadSpeed / maxValue * chartHeight)

            if (index == 0) {
                uploadPath.moveTo(x, y)
            } else {
                uploadPath.lineTo(x, y)
            }
        }

        drawPath(
            path = uploadPath,
            color = Color(0xFF2196F3),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    intervals = floatArrayOf(0f, chartWidth * (1f - progress))
                )
            )
        )
    }
}

fun DrawScope.drawGridLines(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    maxValue: Float
) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val gridStroke = Stroke(width = 1.dp.toPx())

    // Horizontal grid lines
    for (i in 0..4) {
        val y = padding + (chartHeight / 4) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = gridStroke.width
        )
    }

    // Vertical grid lines
    for (i in 0..5) {
        val x = padding + (chartWidth / 5) * i
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = gridStroke.width
        )
    }
}