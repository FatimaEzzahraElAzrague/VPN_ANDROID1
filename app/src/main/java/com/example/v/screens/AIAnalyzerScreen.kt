package com.example.v.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.components.TrafficChart
import com.example.v.ui.theme.VPNTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalyzerScreen() {
    var isDarkTheme by remember { mutableStateOf(true) }
    var federatedLearningStatus by remember { mutableStateOf("Active") }
    var currentTrafficHealth by remember { mutableStateOf(HealthStatus.GOOD) }
    var trafficData by remember { mutableStateOf(listOf<TrafficData>()) }
    var anomalies by remember { mutableStateOf(listOf<Anomaly>()) }
    var isMonitoring by remember { mutableStateOf(false) }

    // Simulate real-time data collection
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            delay(2000) // Update every 2 seconds

            // Generate new traffic data point
            val newDataPoint = TrafficData(
                timestamp = System.currentTimeMillis(),
                bytesIn = Random.nextLong(1000000, 5000000),
                bytesOut = Random.nextLong(500000, 2000000),
                anomalyScore = Random.nextDouble(0.0, 1.0)
            )

            trafficData = (trafficData + newDataPoint).takeLast(20) // Keep last 20 points

            // Simulate anomaly detection
            if (newDataPoint.anomalyScore > 0.8) {
                val anomaly = Anomaly(
                    id = System.currentTimeMillis().toString(),
                    type = AnomalyType.values().random(),
                    severity = Severity.values().random(),
                    description = "Unusual traffic pattern detected",
                    timestamp = System.currentTimeMillis()
                )
                anomalies = (anomalies + anomaly).takeLast(5) // Keep last 5 anomalies
            }

            // Update health status based on recent data
            val recentAnomalyScore = trafficData.takeLast(5).map { it.anomalyScore }.average()
            currentTrafficHealth = when {
                recentAnomalyScore < 0.3 -> HealthStatus.EXCELLENT
                recentAnomalyScore < 0.6 -> HealthStatus.GOOD
                recentAnomalyScore < 0.8 -> HealthStatus.WARNING
                else -> HealthStatus.CRITICAL
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(VPNTheme.DarkBackground, VPNTheme.DarkSurface)
                    } else {
                        listOf(VPNTheme.LightBackground, VPNTheme.LightSurface)
                    }
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "AI Network Monitor",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDarkTheme) VPNTheme.DarkTextPrimary else VPNTheme.LightTextPrimary
                )
            }

            // Federated Learning Status
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) VPNTheme.DarkCardBackground else VPNTheme.LightCardBackground
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Status",
                            tint = VPNTheme.Success,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Federated Learning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) VPNTheme.DarkTextPrimary else VPNTheme.LightTextPrimary
                            )
                            Text(
                                text = "Status: $federatedLearningStatus",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) VPNTheme.DarkTextSecondary else VPNTheme.LightTextSecondary
                            )
                        }
                    }
                }
            }

            // Current Traffic Health
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) VPNTheme.DarkCardBackground else VPNTheme.LightCardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = "Health Status",
                                tint = when (currentTrafficHealth) {
                                    HealthStatus.EXCELLENT -> VPNTheme.Success
                                    HealthStatus.GOOD -> VPNTheme.Success
                                    HealthStatus.WARNING -> VPNTheme.Warning
                                    HealthStatus.CRITICAL -> VPNTheme.Error
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Current Traffic Health",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) VPNTheme.DarkTextPrimary else VPNTheme.LightTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentTrafficHealth.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = when (currentTrafficHealth) {
                                HealthStatus.EXCELLENT -> VPNTheme.Success
                                HealthStatus.GOOD -> VPNTheme.Success
                                HealthStatus.WARNING -> VPNTheme.Warning
                                HealthStatus.CRITICAL -> VPNTheme.Error
                            }
                        )
                    }
                }
            }

            // Traffic Chart
            item {
                TrafficChart(
                    trafficData = trafficData,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Control Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isMonitoring = !isMonitoring },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMonitoring) VPNTheme.Error else VPNTheme.Success
                        )
                    ) {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isMonitoring) "Stop Monitoring" else "Start Monitoring"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isMonitoring) "Stop" else "Start")
                    }

                    OutlinedButton(
                        onClick = {
                            trafficData = emptyList()
                            anomalies = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDarkTheme) VPNTheme.DarkTextPrimary else VPNTheme.LightTextPrimary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset")
                    }
                }
            }

            // Anomalies List
            if (anomalies.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Anomalies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) VPNTheme.DarkTextPrimary else VPNTheme.LightTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(anomalies) { anomaly ->
                    AnomalyCard(anomaly = anomaly, isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun AnomalyCard(anomaly: Anomaly, isDarkTheme: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) VPNTheme.DarkCardBackground else VPNTheme.LightCardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (anomaly.type) {
                    AnomalyType.TRAFFIC_SPIKE -> Icons.Default.TrendingUp
                    AnomalyType.SUSPICIOUS_ACTIVITY -> Icons.Default.Security
                    AnomalyType.PERFORMANCE_ISSUE -> Icons.Default.Speed
                    AnomalyType.NETWORK_ERROR -> Icons.Default.Error
                },
                contentDescription = anomaly.type.name,
                tint = when (anomaly.severity) {
                    Severity.LOW -> VPNTheme.Success
                    Severity.MEDIUM -> VPNTheme.Warning
                    Severity.HIGH -> VPNTheme.Error
                    Severity.CRITICAL -> VPNTheme.Error
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anomaly.type.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) VPNTheme.DarkTextPrimary else VPNTheme.LightTextPrimary
                )
                Text(
                    text = anomaly.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) VPNTheme.DarkTextSecondary else VPNTheme.LightTextSecondary
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (anomaly.severity) {
                        Severity.LOW -> VPNTheme.Success.copy(alpha = 0.2f)
                        Severity.MEDIUM -> VPNTheme.Warning.copy(alpha = 0.2f)
                        Severity.HIGH -> VPNTheme.Error.copy(alpha = 0.2f)
                        Severity.CRITICAL -> VPNTheme.Error.copy(alpha = 0.3f)
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = anomaly.severity.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (anomaly.severity) {
                        Severity.LOW -> VPNTheme.Success
                        Severity.MEDIUM -> VPNTheme.Warning
                        Severity.HIGH -> VPNTheme.Error
                        Severity.CRITICAL -> VPNTheme.Error
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Data classes and enums
data class TrafficData(
    val timestamp: Long,
    val bytesIn: Long,
    val bytesOut: Long,
    val anomalyScore: Double
)

enum class HealthStatus(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    WARNING("Warning"),
    CRITICAL("Critical")
}

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AnomalyType(val displayName: String) {
    TRAFFIC_SPIKE("Traffic Spike"),
    SUSPICIOUS_ACTIVITY("Suspicious Activity"),
    PERFORMANCE_ISSUE("Performance Issue"),
    NETWORK_ERROR("Network Error")
}

data class Anomaly(
    val id: String,
    val type: AnomalyType,
    val severity: Severity,
    val description: String,
    val timestamp: Long
)