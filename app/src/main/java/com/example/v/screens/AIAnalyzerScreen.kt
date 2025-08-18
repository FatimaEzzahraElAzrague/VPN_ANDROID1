package com.example.v.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.components.TrafficChart
import com.example.v.ui.theme.*
import com.example.v.utils.AIVPNTrafficAnalyzer
import com.example.v.utils.AnalysisResult
import com.example.v.utils.ThreatLevel
import com.example.v.utils.TrafficData as AnalyzerTrafficData
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
    val context = LocalContext.current
    val aiAnalyzer = remember { AIVPNTrafficAnalyzer(context) }
    
    var isTracking by remember { mutableStateOf(false) }
    var showAnalysis by remember { mutableStateOf(false) }
    
    // Collect AI analysis data
    val isMonitoring by aiAnalyzer.isMonitoring.collectAsState()
    val analysisResults by aiAnalyzer.analysisResults.collectAsState()
    val trafficData by aiAnalyzer.trafficData.collectAsState()
    
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

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            aiAnalyzer.cleanup()
        }
    }

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
                    text = "AI VPN Analyzer",
                    isDarkTheme = isDarkTheme
                )

                // Theme toggle button
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Model Status
            AICard(
                isDarkTheme = isDarkTheme,
                isModelLoaded = true, // We'll assume it's loaded
                isMonitoring = isMonitoring
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Start Tracking Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isTracking) {
                    PrimaryButton(
                        onClick = {
                            isTracking = true
                            aiAnalyzer.startMonitoring()
                            
                            // Show analysis after a short delay
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(3000)
                                showAnalysis = true
                            }
                        },
                        text = "Start AI Analysis",
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(if (isTracking) pulseScale else 1f)
                    )
                } else {
                    // Analysis in progress
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = OrangeCrayola,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "AI is analyzing VPN traffic...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = getPrimaryTextColor(isDarkTheme),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Text(
                            text = "Monitoring network patterns and detecting anomalies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getSecondaryTextColor(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // AI Analysis Results
            if (showAnalysis && analysisResults != null) {
                AIAnalysisResultsCard(
                    isDarkTheme = isDarkTheme,
                    analysisResult = analysisResults!!,
                    trafficData = trafficData
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Status Overview
            StatusOverviewCard(
                isDarkTheme = isDarkTheme,
                federatedLearningStatus = "Active",
                currentTrafficHealth = when (analysisResults?.threatLevel) {
                    ThreatLevel.LOW -> HealthStatus.EXCELLENT
                    ThreatLevel.MEDIUM -> HealthStatus.GOOD
                    ThreatLevel.HIGH -> HealthStatus.WARNING
                    ThreatLevel.CRITICAL -> HealthStatus.CRITICAL
                    else -> HealthStatus.GOOD
                },
                isMonitoring = isMonitoring,
                onMonitoringToggle = {
                    if (isMonitoring) {
                        aiAnalyzer.stopMonitoring()
                    } else {
                        aiAnalyzer.startMonitoring()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Traffic Chart
            TrafficChartCard(
                isDarkTheme = isDarkTheme,
                trafficData = convertToTrafficData(trafficData)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Anomalies
            AnomaliesCard(
                isDarkTheme = isDarkTheme,
                anomalies = generateAnomaliesFromAnalysis(analysisResults)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AICard(
    isDarkTheme: Boolean,
    isModelLoaded: Boolean,
    isMonitoring: Boolean
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Model",
                    tint = OrangeCrayola,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Random Forest VPN IDS Model",
                        style = MaterialTheme.typography.titleMedium,
                        color = getPrimaryTextColor(isDarkTheme),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "AI-powered traffic analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSecondaryTextColor()
                    )
                }
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = when {
                                isModelLoaded && isMonitoring -> Color(0xFF4CAF50) // Green
                                isModelLoaded -> Color(0xFFFFC107) // Yellow
                                else -> Color(0xFFF44336) // Red
                            },
                            shape = CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    title = "Model Status",
                    value = if (isModelLoaded) "Loaded" else "Loading...",
                    icon = Icons.Default.Storage,
                    isDarkTheme = isDarkTheme
                )
                
                StatusItem(
                    title = "Analysis Status",
                    value = if (isMonitoring) "Active" else "Inactive",
                    icon = Icons.Default.Radar,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun AIAnalysisResultsCard(
    isDarkTheme: Boolean,
    analysisResult: AnalysisResult,
    trafficData: List<AnalyzerTrafficData>
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "AI Analysis",
                    tint = OrangeCrayola,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "AI Analysis Results",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Threat Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Threat Level:",
                    style = MaterialTheme.typography.titleMedium,
                    color = getSecondaryTextColor()
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (analysisResult.threatLevel) {
                            ThreatLevel.LOW -> Color(0xFF4CAF50)
                            ThreatLevel.MEDIUM -> Color(0xFFFFC107)
                            ThreatLevel.HIGH -> Color(0xFFFF9800)
                            ThreatLevel.CRITICAL -> Color(0xFFF44336)
                            else -> Color(0xFF9E9E9E)
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = analysisResult.threatLevel.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Anomaly Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anomaly Score:",
                    style = MaterialTheme.typography.titleMedium,
                    color = getSecondaryTextColor()
                )
                
                Text(
                    text = "${(analysisResult.anomalyScore * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Traffic Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Traffic Type:",
                    style = MaterialTheme.typography.titleMedium,
                    color = getSecondaryTextColor()
                )
                
                Text(
                    text = analysisResult.trafficType,
                    style = MaterialTheme.typography.titleMedium,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Recommendations
            Text(
                text = "Security Recommendations:",
                style = MaterialTheme.typography.titleMedium,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            analysisResult.recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Recommendation",
                        tint = OrangeCrayola,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSecondaryTextColor(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
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

// Helper functions for AI analysis
private fun convertToTrafficData(trafficData: List<AnalyzerTrafficData>): List<TrafficData> {
    return trafficData.map { data ->
        TrafficData(
            timestamp = data.timestamp,
            bytesIn = data.bytesTransferred,
            bytesOut = data.bytesTransferred / 2, // Simulate outbound traffic
            anomalyScore = data.averageLatency / 100.0 // Use latency as anomaly indicator
        )
    }
}

private fun generateAnomaliesFromAnalysis(analysisResult: AnalysisResult?): List<Anomaly> {
    if (analysisResult == null) return emptyList()
    
    val anomalies = mutableListOf<Anomaly>()
    
    // Add anomaly based on threat level
    if (analysisResult.threatLevel == ThreatLevel.HIGH || analysisResult.threatLevel == ThreatLevel.CRITICAL) {
        anomalies.add(
            Anomaly(
                id = "threat_${System.currentTimeMillis()}",
                type = AnomalyType.TRAFFIC_SPIKE,
                severity = when (analysisResult.threatLevel) {
                    ThreatLevel.HIGH -> Severity.HIGH
                    ThreatLevel.CRITICAL -> Severity.HIGH
                    else -> Severity.MEDIUM
                },
                description = "High threat level detected in VPN traffic",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    // Add anomaly based on anomaly score
    if (analysisResult.anomalyScore > 0.7) {
        anomalies.add(
            Anomaly(
                id = "anomaly_${System.currentTimeMillis()}",
                type = AnomalyType.BANDWIDTH_ABUSE,
                severity = Severity.MEDIUM,
                description = "Unusual traffic patterns detected",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    // Add anomaly if not VPN traffic
    if (!analysisResult.isVPNTraffic) {
        anomalies.add(
            Anomaly(
                id = "vpn_${System.currentTimeMillis()}",
                type = AnomalyType.CONNECTION_DROP,
                severity = Severity.HIGH,
                description = "VPN connection appears to be inactive",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    return anomalies
}