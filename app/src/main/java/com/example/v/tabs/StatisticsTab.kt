package com.example.v.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatisticsTab() {
    var selectedPeriod by remember { mutableStateOf("Weekly") }
    val periods = listOf("Daily", "Weekly", "Monthly")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Period Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Usage Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { period ->
                            FilterChip(
                                onClick = { selectedPeriod = period },
                                label = { Text(period) },
                                selected = selectedPeriod == period,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Data Usage Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Data Usage - $selectedPeriod",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Usage Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        drawUsageChart(selectedPeriod)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem("Download", Color(0xFF4CAF50))
                        LegendItem("Upload", Color(0xFF2196F3))
                    }
                }
            }
        }

        item {
            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Data",
                    value = "45.2 GB",
                    icon = Icons.Default.CloudDownload,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Sessions",
                    value = "127",
                    icon = Icons.Default.Timeline,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Avg Speed",
                    value = "78.5 Mbps",
                    icon = Icons.Default.Speed,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Uptime",
                    value = "98.7%",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Detailed Statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detailed Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val stats = listOf(
                        "Data Downloaded" to "32.4 GB",
                        "Data Uploaded" to "12.8 GB",
                        "Connection Time" to "156h 23m",
                        "Servers Used" to "18",
                        "Countries Connected" to "12",
                        "Average Session" to "2h 15m",
                        "Longest Session" to "8h 42m",
                        "Data Saved (Compression)" to "4.2 GB"
                    )

                    stats.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                        if (stats.last() != (label to value)) {
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Export Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Data Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Export your usage statistics and connection history",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export CSV")
                        }

                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Report")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun DrawScope.drawUsageChart(period: String) {
    val padding = 40f
    val chartWidth = size.width - 2 * padding
    val chartHeight = size.height - 2 * padding

    // Mock data based on period
    val dataPoints = when (period) {
        "Daily" -> listOf(2.1f, 3.4f, 1.8f, 4.2f, 3.7f, 2.9f, 3.1f)
        "Weekly" -> listOf(15.2f, 18.7f, 12.4f, 21.3f, 19.8f, 16.5f, 20.1f)
        else -> listOf(45.2f, 52.8f, 38.9f, 61.4f, 47.6f, 55.3f, 49.7f)
    }

    val maxValue = dataPoints.maxOrNull() ?: 1f
    val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

    // Draw grid lines
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    for (i in 0..4) {
        val y = padding + (chartHeight / 4) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Draw bars
    dataPoints.forEachIndexed { index, value ->
        val barHeight = (value / maxValue) * chartHeight
        val barWidth = chartWidth / dataPoints.size * 0.6f
        val x = padding + index * stepX - barWidth / 2
        val y = padding + chartHeight - barHeight

        drawRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
        )
    }
}