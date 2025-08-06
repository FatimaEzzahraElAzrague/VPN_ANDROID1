package com.example.v.screens

import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.v.R
import com.example.v.components.ServerLocationCard
import com.example.v.components.ConnectionDetailsPanel
import com.example.v.models.Server
import com.example.v.ui.theme.VPNTheme
import com.example.v.ui.theme.getGradientBackground
import com.example.v.ui.theme.ThemeToggleButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentServer: Server,
    connectedServer: Server?,
    onServerChange: () -> Unit,
    onNavigate: (String) -> Unit,
    onDisconnect: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    vpnManager: com.example.v.vpn.VPNManager? = null
) {
    var isConnected by remember { mutableStateOf(connectedServer != null) }
    var selectedServer by remember { mutableStateOf(currentServer) }
    var sessionStartTime by remember { mutableStateOf<Long?>(null) }
    var sessionDuration by remember { mutableStateOf("00:00:00") }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Update connection state when connectedServer changes
    LaunchedEffect(connectedServer) {
        isConnected = connectedServer != null
    }

    // Session duration timer
    LaunchedEffect(isConnected) {
        if (isConnected) {
            sessionStartTime = System.currentTimeMillis()
            while (isConnected) {
                sessionStartTime?.let { startTime ->
                    val elapsed = System.currentTimeMillis() - startTime
                    val hours = (elapsed / (1000 * 60 * 60)).toInt()
                    val minutes = ((elapsed % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                    val seconds = ((elapsed % (1000 * 60)) / 1000).toInt()
                    sessionDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000)
            }
        } else {
            sessionStartTime = null
            sessionDuration = "00:00:00"
        }
    }

    // Handle VPN connection
    fun handleConnect() {
        if (isConnected) {
            // Disconnect
            onDisconnect()
        } else {
            // Check VPN permission first
            if (vpnManager?.hasVpnPermission() == true) {
                // Connect directly to current server
                vpnManager.connect(currentServer)
            } else {
                // Request VPN permission
                val permissionIntent = vpnManager?.getVpnPermissionIntent()
                if (permissionIntent != null) {
                    context.startActivity(permissionIntent)
                } else {
                    showPermissionDialog = true
                }
            }
        }
    }

    VPNTheme {
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

                // Top bar with theme toggle only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Theme toggle button
                    ThemeToggleButton(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle
                    )
                }

                // Main Content Layer with Scroll
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Status Text
                Text(
                    text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                        color = if (isConnected) Color(0xFF4CAF50) else if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Central Connect Button
                    ConnectButton(
                    isConnected = isConnected,
                    onClick = { handleConnect() },
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(60.dp))

                    // Server Selection Card (clickable)
                    ServerSelectionCard(
                        server = connectedServer ?: selectedServer,
                        isConnected = connectedServer != null,
                        onClick = { onNavigate("servers") },
                        modifier = Modifier.fillMaxWidth(),
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connection Details Card (only show when connected)
                    if (isConnected && connectedServer != null) {
                        ConnectionDetailsCard(
                            server = connectedServer,
                            sessionDuration = sessionDuration,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // VPN Permission Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("VPN Permission Required") },
                text = { Text("This app needs VPN permission to establish secure connections. Please grant the permission in the next dialog.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionDialog = false
                            val permissionIntent = vpnManager?.getVpnPermissionIntent()
                            if (permissionIntent != null) {
                                context.startActivity(permissionIntent)
                            }
                        }
                    ) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ConnectButton(
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val buttonColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF6C36)
    val shadowColor = buttonColor.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 8.dp else 12.dp,
                shape = CircleShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(CircleShape)
            .background(buttonColor)
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Use the power_ic vector asset
            Icon(
                painter = painterResource(id = R.drawable.power_ic),
                contentDescription = if (isConnected) "Disconnect" else "Connect",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isConnected) "DISCONNECT" else "CONNECT",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun ServerSelectionCard(
    server: Server,
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = if (isConnected)
                    Color(0xFFE53E3E).copy(alpha = 0.15f)
                else if (isDarkTheme)
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                else
                    Color.White,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = server.flag,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${server.country}, ${server.city}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) MaterialTheme.colorScheme.onSurface else Color.Black
                    )
                    Text(
                        text = "Ping: ${server.ping}ms • Load: ${server.load}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
            // Connection status indicator
            if (isConnected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE53E3E), CircleShape)
                    )
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53E3E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select region",
                tint = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ConnectionDetailsCard(
    server: Server,
    sessionDuration: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Connection Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Local IP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local IP Address",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "192.168.1.103",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { /* TODO: Copy IP */ },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF6C36)
                        )
                    ) {
                        Text(
                            text = "Copy",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

                    // Server IP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Server IP Address",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = server.wireGuardConfig?.serverEndpoint ?: "---",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { /* TODO: Copy IP */ },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFFF6C36)
                                )
                            ) {
                                Text(
                                    text = "Copy",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

        Spacer(modifier = Modifier.height(4.dp))

            // Session Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session Duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
        Text(
                    text = sessionDuration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}