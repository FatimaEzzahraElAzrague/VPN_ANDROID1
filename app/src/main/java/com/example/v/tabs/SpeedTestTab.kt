package com.example.v.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.v.components.SpeedTestGauge
import kotlinx.coroutines.launch

@Composable
fun SpeedTestTab() {
    var isRunning by remember { mutableStateOf(false) }
    var hasResults by remember { mutableStateOf(false) }
    var downloadSpeed by remember { mutableStateOf(0f) }
    var uploadSpeed by remember { mutableStateOf(0f) }
    var ping by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Speed Test Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Internet Speed Test",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Test your connection speed through the VPN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Speed Gauges
        if (hasResults || isRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Speed Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SpeedTestGauge(
                            currentSpeed = downloadSpeed,
                            maxSpeed = 100f,
                            label = "Download",
                            unit = "Mbps",
                            color = Color(0xFF4CAF50)
                        )

                        SpeedTestGauge(
                            currentSpeed = uploadSpeed,
                            maxSpeed = 50f,
                            label = "Upload",
                            unit = "Mbps",
                            color = Color(0xFF2196F3)
                        )

                        SpeedTestGauge(
                            currentSpeed = ping,
                            maxSpeed = 200f,
                            label = "Ping",
                            unit = "ms",
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }

        // Test Button
        Button(
            onClick = {
                isRunning = true
                hasResults = false
                downloadSpeed = 0f
                uploadSpeed = 0f
                ping = 0f

                // Simulate speed test
                kotlinx.coroutines.GlobalScope.launch {
                    // Simulate download test
                    for (i in 1..85) {
                        downloadSpeed = i.toFloat()
                        kotlinx.coroutines.delay(30)
                    }

                    // Simulate upload test
                    for (i in 1..42) {
                        uploadSpeed = i.toFloat()
                        kotlinx.coroutines.delay(30)
                    }

                    // Simulate ping test
                    for (i in 1..15) {
                        ping = i.toFloat()
                        kotlinx.coroutines.delay(30)
                    }

                    isRunning = false
                    hasResults = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing Connection...")
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (hasResults) "Run Test Again" else "Start Speed Test")
            }
        }

        // Test History
        if (hasResults) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Test History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Mock history entries
                    val historyEntries = listOf(
                        Triple("Just now", "85.2 / 42.1 Mbps", "15 ms"),
                        Triple("2 hours ago", "78.5 / 38.9 Mbps", "18 ms"),
                        Triple("Yesterday", "92.1 / 45.3 Mbps", "12 ms"),
                        Triple("2 days ago", "81.7 / 41.2 Mbps", "16 ms")
                    )

                    historyEntries.forEach { (time, speeds, pingTime) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = speeds,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Ping: $pingTime",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        if (historyEntries.last() != Triple(time, speeds, pingTime)) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Speed Test Tips",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Close other apps using internet\n• Connect to nearest server for best results\n• Test multiple times for accuracy\n• VPN may reduce speed by 10-20%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}