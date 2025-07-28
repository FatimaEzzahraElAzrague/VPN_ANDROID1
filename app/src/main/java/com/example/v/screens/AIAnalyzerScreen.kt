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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.components.TrafficChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// Data classes for dynamic functionality
data class TrafficDataPoint(
    val timestamp: Long,
    val bytesIn: Long,
    val bytesOut: Long,
    val connections: Int,
    val anomalyScore: Float
)

data class TrafficData(
    val timestamp: Long,
    val bytesIn: Long,
    val bytesOut: Long,
    val connections: Int,
    val anomalyScore: Float
)

data class Anomaly(
    val id: String,
    val type: AnomalyType,
    val severity: Severity,
    val description: String,
    val suggestion: String,
    val timestamp: Long,
    val confidence: Float
)

data class TrafficHealth(
    val status: HealthStatus,
    val score: Int,
    val description: String
)

enum class HealthStatus {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AnomalyType {
    SUSPICIOUS_TRAFFIC,
    MALWARE_DETECTED,
    DNS_LEAK,
    UNUSUAL_BANDWIDTH,
    TRACKING_ATTEMPT,
    PHISHING_BLOCKED
}

// Dark theme colors matching web app
object VPNTheme {
    val DarkBackground = Color(0xFF1A1B2E)
    val DarkSurface = Color(0xFF16213E)
    val CardBackground = Color(0xFF0F3460)
    val AccentOrange = Color(0xFFFF6B35)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB8BCC8)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFFF5722)
    val Critical = Color(0xFFF44336)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalyzerScreen() {
    var isAnalyzing by remember { mutableStateOf(false) }
    var isTraining by remember { mutableStateOf(false) }
    var trafficData by remember { mutableStateOf(generateInitialTrafficData()) }
    var anomalies by remember { mutableStateOf(listOf<Anomaly>()) }
    var modelAccuracy by remember { mutableStateOf(0.85f) }
    var federatedNodes by remember { mutableStateOf(3) }
    var lastUpdate by remember { mutableStateOf(System.currentTimeMillis()) }

    var trafficHealth by remember {
        mutableStateOf(
            TrafficHealth(
                status = HealthStatus.GOOD,
                score = 85,
                description = "Federated learning model is monitoring traffic patterns in real-time."
            )
        )
    }

    val scope = rememberCoroutineScope()

    // Real-time data collection effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Update every 5 seconds

            // Simulate new traffic data
            val newDataPoint = TrafficDataPoint(
                timestamp = System.currentTimeMillis(),
                bytesIn = Random.nextLong(1000, 50000),
                bytesOut = Random.nextLong(500, 25000),
                connections = Random.nextInt(10, 100),
                anomalyScore = Random.nextFloat()
            )

            trafficData = trafficData.takeLast(49) + newDataPoint

            // Run anomaly detection
            if (newDataPoint.anomalyScore > 0.7f) {
                val newAnomaly = generateDynamicAnomaly(newDataPoint)
                anomalies = (listOf(newAnomaly) + anomalies).take(10)

                // Update health status based on anomaly
                trafficHealth = calculateHealthStatus(anomalies, modelAccuracy)
            }

            lastUpdate = System.currentTimeMillis()
        }
    }

    // Dark gradient background matching web app
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VPNTheme.DarkBackground,
                        VPNTheme.DarkSurface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header with live status - matching web app style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Traffic Monitor",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VPNTheme.TextPrimary
                    )
                    Text(
                        text = "Federated Learning • Live",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VPNTheme.Success
                    )
                }

                // Live indicator matching web app
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = VPNTheme.Success.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = VPNTheme.Success,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = VPNTheme.Success
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Federated Learning Status Card - web app style
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = VPNTheme.CardBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Federated Learning Status",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = VPNTheme.TextPrimary
                        )

                        if (isTraining) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = VPNTheme.AccentOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Model Accuracy",
                                style = MaterialTheme.typography.bodySmall,
                                color = VPNTheme.TextSecondary
                            )
                            Text(
                                text = "${(modelAccuracy * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VPNTheme.AccentOrange
                            )
                        }

                        Column {
                            Text(
                                text = "Active Nodes",
                                style = MaterialTheme.typography.bodySmall,
                                color = VPNTheme.TextSecondary
                            )
                            Text(
                                text = federatedNodes.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VPNTheme.AccentOrange
                            )
                        }

                        Column {
                            Text(
                                text = "Last Update",
                                style = MaterialTheme.typography.bodySmall,
                                color = VPNTheme.TextSecondary
                            )
                            Text(
                                text = "${(System.currentTimeMillis() - lastUpdate) / 1000}s ago",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VPNTheme.AccentOrange
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Traffic Health - web app card style
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = VPNTheme.CardBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                color = getHealthColor(trafficHealth.status),
                                shape = RoundedCornerShape(35.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trafficHealth.score.toString(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Traffic Health",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VPNTheme.TextSecondary
                        )
                        Text(
                            text = trafficHealth.status.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = getHealthColor(trafficHealth.status)
                        )
                        Text(
                            text = trafficHealth.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VPNTheme.TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = getHealthIcon(trafficHealth.status),
                        contentDescription = null,
                        tint = getHealthColor(trafficHealth.status),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Traffic Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = VPNTheme.CardBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Real-time Traffic Analysis",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VPNTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TrafficChart(
                        trafficData = trafficData.map { dataPoint ->
                            TrafficData(
                                timestamp = dataPoint.timestamp,
                                bytesIn = dataPoint.bytesIn,
                                bytesOut = dataPoint.bytesOut,
                                connections = dataPoint.connections,
                                anomalyScore = dataPoint.anomalyScore
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Control Buttons - web app style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        isAnalyzing = true
                        scope.launch {
                            delay(2000)
                            isAnalyzing = false
                            // Force immediate analysis
                            val newAnomaly = generateDynamicAnomaly(trafficData.last())
                            anomalies = (listOf(newAnomaly) + anomalies).take(10)
                            trafficHealth = calculateHealthStatus(anomalies, modelAccuracy)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VPNTheme.AccentOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Force Scan")
                    }
                }

                OutlinedButton(
                    onClick = {
                        isTraining = true
                        scope.launch {
                            delay(5000) // Simulate federated training
                            isTraining = false
                            modelAccuracy = (modelAccuracy + Random.nextFloat() * 0.05f).coerceAtMost(0.98f)
                            federatedNodes = Random.nextInt(2, 8)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTraining,
                    colors = OutlinedButtonDefaults.outlinedButtonColors(
                        contentColor = VPNTheme.AccentOrange
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        VPNTheme.AccentOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isTraining) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = VPNTheme.AccentOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Training...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.ModelTraining,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retrain")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Anomalies List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Anomaly Detection",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = VPNTheme.TextPrimary
                )

                Surface(
                    color = VPNTheme.AccentOrange.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = VPNTheme.AccentOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${anomalies.size} detected",
                            style = MaterialTheme.typography.bodySmall,
                            color = VPNTheme.AccentOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (anomalies.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = VPNTheme.Success.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VPNTheme.Success,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Anomalies Detected",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = VPNTheme.Success
                        )
                        Text(
                            text = "Your network traffic appears normal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VPNTheme.TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(anomalies) { anomaly ->
                        AnomalyItem(anomaly = anomaly)
                    }
                }
            }
        }
    }
}

@Composable
fun AnomalyItem(anomaly: Anomaly) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = VPNTheme.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getAnomalyIcon(anomaly.type),
                        contentDescription = null,
                        tint = getSeverityColor(anomaly.severity),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = anomaly.type.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VPNTheme.TextPrimary
                    )
                }

                Surface(
                    color = getSeverityColor(anomaly.severity).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = anomaly.severity.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = getSeverityColor(anomaly.severity),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = anomaly.description,
                style = MaterialTheme.typography.bodyMedium,
                color = VPNTheme.TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 ${anomaly.suggestion}",
                style = MaterialTheme.typography.bodySmall,
                color = VPNTheme.TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(anomaly.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = VPNTheme.TextSecondary
                )

                Text(
                    text = "Confidence: ${(anomaly.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = getSeverityColor(anomaly.severity),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Dynamic data generation functions
fun generateInitialTrafficData(): List<TrafficDataPoint> {
    val currentTime = System.currentTimeMillis()
    return (0..49).map { i ->
        TrafficDataPoint(
            timestamp = currentTime - (49 - i) * 5000,
            bytesIn = Random.nextLong(1000, 30000),
            bytesOut = Random.nextLong(500, 15000),
            connections = Random.nextInt(10, 80),
            anomalyScore = Random.nextFloat() * 0.6f
        )
    }
}

fun generateDynamicAnomaly(dataPoint: TrafficDataPoint): Anomaly {
    val anomalyTypes = AnomalyType.values()
    val selectedType = anomalyTypes.random()
    val selectedSeverity = when {
        dataPoint.anomalyScore > 0.9f -> Severity.CRITICAL
        dataPoint.anomalyScore > 0.8f -> Severity.HIGH
        dataPoint.anomalyScore > 0.7f -> Severity.MEDIUM
        else -> Severity.LOW
    }

    val descriptions = mapOf(
        AnomalyType.SUSPICIOUS_TRAFFIC to "Unusual traffic pattern detected with ${dataPoint.connections} concurrent connections",
        AnomalyType.MALWARE_DETECTED to "Potential malware communication detected in network traffic",
        AnomalyType.DNS_LEAK to "DNS queries bypassing secure tunnel detected",
        AnomalyType.UNUSUAL_BANDWIDTH to "Bandwidth spike detected: ${dataPoint.bytesIn + dataPoint.bytesOut} bytes",
        AnomalyType.TRACKING_ATTEMPT to "Third-party tracking attempt blocked by federated AI",
        AnomalyType.PHISHING_BLOCKED to "Phishing attempt detected and blocked automatically"
    )

    val suggestions = mapOf(
        AnomalyType.SUSPICIOUS_TRAFFIC to "Monitor connection patterns and consider rate limiting",
        AnomalyType.MALWARE_DETECTED to "Run full system scan and update security definitions",
        AnomalyType.DNS_LEAK to "Check VPN configuration and DNS settings",
        AnomalyType.UNUSUAL_BANDWIDTH to "Investigate high bandwidth usage applications",
        AnomalyType.TRACKING_ATTEMPT to "Review privacy settings and ad blockers",
        AnomalyType.PHISHING_BLOCKED to "Verify email sources and update security training"
    )

    return Anomaly(
        id = UUID.randomUUID().toString(),
        type = selectedType,
        severity = selectedSeverity,
        description = descriptions[selectedType] ?: "Anomaly detected in network traffic",
        suggestion = suggestions[selectedType] ?: "Review network security settings",
        timestamp = dataPoint.timestamp,
        confidence = dataPoint.anomalyScore
    )
}

fun calculateHealthStatus(anomalies: List<Anomaly>, modelAccuracy: Float): TrafficHealth {
    val recentAnomalies = anomalies.filter {
        System.currentTimeMillis() - it.timestamp < 300000 // Last 5 minutes
    }

    val criticalCount = recentAnomalies.count { it.severity == Severity.CRITICAL }
    val highCount = recentAnomalies.count { it.severity == Severity.HIGH }
    val mediumCount = recentAnomalies.count { it.severity == Severity.MEDIUM }

    val (status, score, description) = when {
        criticalCount > 0 -> Triple(
            HealthStatus.CRITICAL,
            25,
            "Critical security threats detected! Immediate attention required."
        )
        highCount > 2 -> Triple(
            HealthStatus.POOR,
            45,
            "Multiple high-severity anomalies detected in recent traffic."
        )
        highCount > 0 || mediumCount > 3 -> Triple(
            HealthStatus.FAIR,
            65,
            "Some security concerns detected. Monitoring recommended."
        )
        mediumCount > 0 -> Triple(
            HealthStatus.GOOD,
            80,
            "Minor anomalies detected. Network appears mostly secure."
        )
        else -> Triple(
            HealthStatus.EXCELLENT,
            95,
            "No threats detected. Federated AI model running optimally."
        )
    }

    // Adjust score based on model accuracy
    val adjustedScore = (score * modelAccuracy).toInt()

    return TrafficHealth(
        status = status,
        score = adjustedScore,
        description = description
    )
}

// Helper functions with web app color scheme
fun getHealthColor(status: HealthStatus): Color {
    return when (status) {
        HealthStatus.EXCELLENT -> VPNTheme.Success
        HealthStatus.GOOD -> Color(0xFF8BC34A)
        HealthStatus.FAIR -> VPNTheme.Warning
        HealthStatus.POOR -> VPNTheme.Error
        HealthStatus.CRITICAL -> VPNTheme.Critical
    }
}

fun getHealthIcon(status: HealthStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        HealthStatus.EXCELLENT -> Icons.Default.CheckCircle
        HealthStatus.GOOD -> Icons.Default.Check
        HealthStatus.FAIR -> Icons.Default.Warning
        HealthStatus.POOR -> Icons.Default.Error
        HealthStatus.CRITICAL -> Icons.Default.Dangerous
    }
}

fun getSeverityColor(severity: Severity): Color {
    return when (severity) {
        Severity.LOW -> VPNTheme.Success
        Severity.MEDIUM -> VPNTheme.Warning
        Severity.HIGH -> VPNTheme.Error
        Severity.CRITICAL -> VPNTheme.Critical
    }
}

fun getAnomalyIcon(type: AnomalyType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        AnomalyType.SUSPICIOUS_TRAFFIC -> Icons.Default.Warning
        AnomalyType.MALWARE_DETECTED -> Icons.Default.BugReport
        AnomalyType.DNS_LEAK -> Icons.Default.VpnLock
        AnomalyType.UNUSUAL_BANDWIDTH -> Icons.Default.TrendingUp
        AnomalyType.TRACKING_ATTEMPT -> Icons.Default.Visibility
        AnomalyType.PHISHING_BLOCKED -> Icons.Default.Security
    }
}