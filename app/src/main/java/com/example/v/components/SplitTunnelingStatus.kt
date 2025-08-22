package com.example.v.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import com.example.v.services.SplitTunnelingService
import com.example.v.vpn.SplitTunnelingMode
import com.example.v.ui.theme.*

/**
 * Split Tunneling Status Component
 * Shows current split tunneling configuration and status
 */
@Composable
fun SplitTunnelingStatus(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val splitTunnelingService = remember { SplitTunnelingService.getInstance(context) }
    
    // Observe split tunneling state
    val isEnabled by splitTunnelingService.isEnabled.collectAsState()
    val mode by splitTunnelingService.mode.collectAsState()
    val selectedApps by splitTunnelingService.selectedApps.collectAsState()
    val config by splitTunnelingService.config.collectAsState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = "Split Tunneling",
                    tint = if (isEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Split Tunneling",
                    style = MaterialTheme.typography.titleMedium,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status text
            Text(
                text = if (isEnabled) {
                    when (mode) {
                        SplitTunnelingMode.INCLUDE -> "Only ${selectedApps.size} selected apps use VPN"
                        SplitTunnelingMode.EXCLUDE -> "${selectedApps.size} apps bypass VPN"
                    }
                } else {
                    "All apps use VPN"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = getSecondaryTextColor()
            )
            
            if (isEnabled && selectedApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // App count
                Text(
                    text = "${selectedApps.size} apps configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = getSecondaryTextColor(),
                    fontSize = 12.sp
                )
                
                // Mode indicator
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (mode) {
                            SplitTunnelingMode.INCLUDE -> Icons.Default.CheckCircle
                            SplitTunnelingMode.EXCLUDE -> Icons.Default.RemoveCircle
                        },
                        contentDescription = null,
                        tint = when (mode) {
                            SplitTunnelingMode.INCLUDE -> Color(0xFF4CAF50)
                            SplitTunnelingMode.EXCLUDE -> Color(0xFFFF9800)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (mode) {
                            SplitTunnelingMode.INCLUDE -> "Include Mode"
                            SplitTunnelingMode.EXCLUDE -> "Exclude Mode"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = getSecondaryTextColor(),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
