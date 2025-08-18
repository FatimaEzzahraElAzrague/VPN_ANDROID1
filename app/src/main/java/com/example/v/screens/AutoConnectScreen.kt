package com.example.v.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.v.data.autoconnect.AutoConnectMode
import com.example.v.data.autoconnect.AutoConnectRepository
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.launch

@Composable
fun AutoConnectScreen(repo: AutoConnectRepository) {
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(AutoConnectMode.ANY_WIFI_OR_CELLULAR) }
    val isDarkTheme = isSystemInDarkTheme()
    
    // Debug: Print the theme state
    LaunchedEffect(isDarkTheme) {
        println("AutoConnectScreen: isDarkTheme = $isDarkTheme")
    }
    
    LaunchedEffect(Unit) {
        val current = repo.get()
        if (current != null) {
            enabled = current.enabled
            mode = current.mode
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF9F9F7)
            )
            .padding(16.dp)
    ) {
        // Auto Connect Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Auto Connect Toggle - text and switch aligned horizontally
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto Connect Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            scope.launch { repo.set(enabled = enabled, mode = mode) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFF6C36),
                            checkedTrackColor = Color(0xFFFF6C36).copy(alpha = 0.3f),
                            uncheckedThumbColor = if (isDarkTheme) Color(0xFF4A5568) else Color(0xFFCBD5E0),
                            uncheckedTrackColor = if (isDarkTheme) Color(0xFF2D3748) else Color(0xFFE2E8F0)
                        )
                    )
                }
            }
        }

        // Connection Rules Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Connection Rules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Choose when to automatically connect to VPN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF4A5161),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        AutoConnectMode.UNSECURED_WIFI_ONLY to "Unsecured Wi-Fi only",
                        AutoConnectMode.ANY_WIFI to "Any Wi-Fi",
                        AutoConnectMode.ANY_WIFI_OR_CELLULAR to "Any Wi-Fi or cellular"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                            RadioButton(
                                selected = mode == value,
                                onClick = {
                                    mode = value
                                    scope.launch { repo.set(enabled = enabled, mode = mode) }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFF6C36),
                                    unselectedColor = if (isDarkTheme) Color(0xFF4A5568) else Color(0xFFCBD5E0)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


