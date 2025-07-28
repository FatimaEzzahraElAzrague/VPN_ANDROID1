package com.example.v.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.v.screens.TrafficData
import com.example.v.ui.theme.VPNTheme

@Composable
fun TrafficChart(
    trafficData: List<TrafficData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = VPNTheme.DarkCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Traffic Analysis",
                style = MaterialTheme.typography.titleMedium,
                color = VPNTheme.DarkTextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (trafficData.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    drawTrafficChart(trafficData)
                }
            } else {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VPNTheme.DarkTextSecondary
                )
            }
        }
    }
}

fun DrawScope.drawTrafficChart(trafficData: List<TrafficData>) {
    if (trafficData.isEmpty()) return
    
    val width = size.width
    val height = size.height
    val padding = 20f
    
    val chartWidth = width - (padding * 2)
    val chartHeight = height - (padding * 2)
    
    // Find min and max values for scaling
    val maxBytes = trafficData.maxOfOrNull { it.bytesIn + it.bytesOut } ?: 0L
    val minBytes = trafficData.minOfOrNull { it.bytesIn + it.bytesOut } ?: 0L
    
    if (maxBytes == minBytes) return
    
    // Draw grid lines
    val gridColor = VPNTheme.DarkTextSecondary.copy(alpha = 0.2f)
    for (i in 0..4) {
        val y = padding + (chartHeight / 4) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1f
        )
    }
    
    // Draw data points and lines
    val pointRadius = 3f
    val lineColor = VPNTheme.DarkPrimary
    val pointColor = VPNTheme.DarkPrimary
    
    val path = Path()
    var isFirst = true
    
    trafficData.forEachIndexed { index, data ->
        val x = padding + (chartWidth / (trafficData.size - 1)) * index
        val normalizedValue = (data.bytesIn + data.bytesOut - minBytes).toFloat() / (maxBytes - minBytes)
        val y = height - padding - (chartHeight * normalizedValue)
        
        // Draw point
        drawCircle(
            color = pointColor,
            radius = pointRadius,
            center = Offset(x, y)
        )
        
        // Draw line
        if (isFirst) {
            path.moveTo(x, y)
            isFirst = false
        } else {
            path.lineTo(x, y)
        }
    }
    
    // Draw the line
    drawPath(
        path = path,
        color = lineColor,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2f
        )
    )
}