package com.example.v.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.components.TrafficChart
import com.example.v.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalyzerScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var isTracking by remember { mutableStateOf(false) }
    var showAnalysis by remember { mutableStateOf(false) }
    
    // Animation for tracking button
    val infiniteTransition = rememberInfiniteTransition(label = "tracking")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getGradientBackground(isDarkTheme))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Status bar spacer
            Spacer(modifier = Modifier.height(40.dp))

            // Top bar with title and theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title
                TitleText(
                    text = "AI Analyzer",
                    isDarkTheme = isDarkTheme
                )

                // Theme toggle button
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Tracking Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isTracking) {
                    PrimaryButton(
                        onClick = {
                            isTracking = true
                            // Simulate tracking delay
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(3000)
                                showAnalysis = true
                            }
                        },
                        text = "Start Tracking",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        icon = Icons.Default.Analytics
                    )
                } else {
                    // Animated tracking indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(pulseScale)
                                .background(
                                    OrangeCrayola.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Tracking",
                                tint = OrangeCrayola,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Analyzing traffic...",
                            style = MaterialTheme.typography.titleMedium,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Analysis Results
            if (showAnalysis) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Traffic Health Card
                StyledCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Traffic Health",
                            style = MaterialTheme.typography.titleLarge,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = getSecondaryTextColor()
                                )
                                Text(
                                    text = "Healthy",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "Threats",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = getSecondaryTextColor()
                                )
                                Text(
                                    text = "0 Detected",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getPrimaryTextColor(isDarkTheme),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Analysis Card
                StyledCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "AI Analysis",
                            style = MaterialTheme.typography.titleLarge,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Your connection is secure and optimized. No suspicious activity detected. All traffic is properly encrypted and routed through secure servers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getSecondaryTextColor()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatusOverviewCard(
    isDarkTheme: Boolean,
    federatedLearningStatus: String,
    currentTrafficHealth: HealthStatus,
    isMonitoring: Boolean,
    onMonitoringToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network Status",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )

                Switch(
                    checked = isMonitoring,
                    onCheckedChange = { onMonitoringToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OrangeCrayola,
                        checkedTrackColor = OrangeCrayola.copy(alpha = 0.3f),
                        uncheckedThumbColor = getSecondaryTextColor(),
                        uncheckedTrackColor = getSecondaryTextColor().copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    title = "Federated Learning",
                    value = federatedLearningStatus,
                    icon = Icons.Default.Psychology,
                    isDarkTheme = isDarkTheme
                )

                StatusItem(
                    title = "Traffic Health",
                    value = currentTrafficHealth.name,
                    icon = Icons.Default.HealthAndSafety,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun StatusItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkTheme: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = OrangeCrayola,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = getSecondaryTextColor(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = getPrimaryTextColor(isDarkTheme),
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun TrafficChartCard(
    isDarkTheme: Boolean,
    trafficData: List<TrafficData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Traffic Analysis",
                style = MaterialTheme.typography.titleLarge,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrafficChart(
                trafficData = trafficData,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun AnomaliesCard(
    isDarkTheme: Boolean,
    anomalies: List<Anomaly>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Recent Anomalies",
                style = MaterialTheme.typography.titleLarge,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (anomalies.isEmpty()) {
                Text(
                    text = "No anomalies detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getSecondaryTextColor(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                anomalies.forEach { anomaly ->
                    AnomalyItem(
                        anomaly = anomaly,
                        isDarkTheme = isDarkTheme
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AnomalyItem(
    anomaly: Anomaly,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (anomaly.severity) {
                Severity.LOW -> Icons.Default.Info
                Severity.MEDIUM -> Icons.Default.Warning
                Severity.HIGH -> Icons.Default.Error
            },
            contentDescription = "Severity",
            tint = when (anomaly.severity) {
                Severity.LOW -> Color(0xFF4CAF50)
                Severity.MEDIUM -> Color(0xFFFFC107)
                Severity.HIGH -> Color(0xFFF44336)
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = anomaly.description,
                style = MaterialTheme.typography.bodyMedium,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${anomaly.type.name} - ${anomaly.severity.name}",
                style = MaterialTheme.typography.bodySmall,
                color = getSecondaryTextColor()
            )
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

data class Anomaly(
    val id: String,
    val type: AnomalyType,
    val severity: Severity,
    val description: String,
    val timestamp: Long
)

enum class HealthStatus {
    EXCELLENT, GOOD, WARNING, CRITICAL
}

enum class AnomalyType {
    TRAFFIC_SPIKE, CONNECTION_DROP, LATENCY_INCREASE, BANDWIDTH_ABUSE
}

enum class Severity {
    LOW, MEDIUM, HIGH
}