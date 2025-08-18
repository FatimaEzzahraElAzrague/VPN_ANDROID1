package com.example.v.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.models.Server
import kotlinx.coroutines.delay

@Composable
fun ConnectionDetailsPanel(
    server: Server,
    modifier: Modifier = Modifier
) {
    var sessionDuration by remember { mutableStateOf("00:00:00") }
    var localIP by remember { mutableStateOf("192.168.1.100") }
    var serverIP by remember { mutableStateOf("---") }

    LaunchedEffect(Unit) {
        // Simulate session timer
        var seconds = 0
        while (true) {
            delay(1000)
            seconds++
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            sessionDuration = String.format("%02d:%02d:%02d", hours, minutes, secs)
        }
    }

    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Connection Details",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connected",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Info
            ConnectionInfoItem(
                label = "Local IP Address",
                value = localIP,
                showCopy = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConnectionInfoItem(
                label = "Server IP Address",
                value = serverIP,
                showCopy = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConnectionInfoItem(
                label = "Session Duration",
                value = sessionDuration,
                showCopy = false
            )
        }
    }
}

@Composable
private fun ConnectionInfoItem(
    label: String,
    value: String,
    showCopy: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (showCopy) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}