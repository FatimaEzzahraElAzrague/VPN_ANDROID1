// In tabs/SpeedTestTab.kt
package com.example.v.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.v.components.SpeedTestGauge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SpeedTestTab(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<SpeedTestResults?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRunning) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Testing connection...",
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (results != null) {
            // Show the gauge component
            SpeedTestGauge(
                downloadSpeed = results!!.downloadSpeed,
                uploadSpeed = results!!.uploadSpeed,
                ping = results!!.ping.toFloat(),
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isRunning = true
                    results = null
                    coroutineScope.launch {
                        delay(3000) // Simulate test
                        results = SpeedTestResults(
                            downloadSpeed = (50..150).random().toFloat(),
                            uploadSpeed = (10..50).random().toFloat(),
                            ping = (10..100).random(),
                            jitter = (1..10).random()
                        )
                        isRunning = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Test Again")
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "Speed Test",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Measure your connection speed",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        isRunning = true
                        coroutineScope.launch {
                            delay(3000) // Simulate test
                            results = SpeedTestResults(
                                downloadSpeed = (50..150).random().toFloat(),
                                uploadSpeed = (10..50).random().toFloat(),
                                ping = (10..100).random(),
                                jitter = (1..10).random()
                            )
                            isRunning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Test")
                }
            }
        }
    }
}

data class SpeedTestResults(
    val downloadSpeed: Float,
    val uploadSpeed: Float,
    val ping: Int,
    val jitter: Int
)