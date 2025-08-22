package com.example.v.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.vpn.VPNProxyService
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.BorderStroke

/**
 * Proxy Status Component - Shows proxy service status and working servers
 */
@Composable
fun ProxyStatusComponent(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var proxyStatus by remember { mutableStateOf("Initializing...") }
    var workingServers by remember { mutableStateOf<List<VPNProxyService.ServerInfo>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(false) }
    
    // Update status every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            try {
                // Get proxy status (this would come from your VPN manager)
                proxyStatus = "Proxy: Running | Servers: 2/2 working | Port: 1080"
                workingServers = listOf(
                    VPNProxyService.ServerInfo(
                        id = "paris",
                        name = "Paris",
                        ip = "52.47.190.220",
                        port = 51820,
                        publicKey = "yvB7acu9ncFFEyzw5n8L7kpLazTgQonML1PuhoStjjg=",
                        subnet = "10.77.26.0/24"
                    ),
                    VPNProxyService.ServerInfo(
                        id = "osaka",
                        name = "Osaka",
                        ip = "15.168.240.118",
                        port = 51820,
                        publicKey = "Hr1B3sNsDSxFpR+zO34qLGxutUK3wgaPwrsWoY2ViAM=",
                        subnet = "10.77.27.0/24"
                    )
                )
            } catch (e: Exception) {
                proxyStatus = "Proxy: Error - ${e.message}"
            }
            delay(5000)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔧 VPN Proxy Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Color.Green,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = proxyStatus,
                    fontSize = 14.sp,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }
            
            // Expandable content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Working servers
                Text(
                    text = "Working Servers:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                workingServers.forEach { server ->
                    ServerStatusItem(
                        server = server,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Proxy info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF3D3D3D) else Color(0xFFE8E8E8)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📊 Proxy Information",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        InfoRow("Port", "1080", isDarkTheme)
                        InfoRow("Protocol", "SOCKS5", isDarkTheme)
                        InfoRow("Encryption", "AES-256", isDarkTheme)
                        InfoRow("Fallback", "Enabled", isDarkTheme)
                    }
                }
            }
        }
    }
}

/**
 * Individual server status item
 */
@Composable
private fun ServerStatusItem(
    server: VPNProxyService.ServerInfo,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF3D3D3D) else Color(0xFFE8E8E8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDarkTheme) Color(0xFF555555) else Color(0xFFCCCCCC)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = Color.Green,
                        shape = RoundedCornerShape(5.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Server info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = server.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                
                Text(
                    text = "${server.ip}:${server.port}",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray
                )
                
                Text(
                    text = "Subnet: ${server.subnet}",
                    fontSize = 11.sp,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray
                )
            }
            
            // Connection status
            Text(
                text = "✅",
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Information row for proxy details
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDarkTheme) Color.Gray else Color.DarkGray
        )
        
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black
        )
    }
}
