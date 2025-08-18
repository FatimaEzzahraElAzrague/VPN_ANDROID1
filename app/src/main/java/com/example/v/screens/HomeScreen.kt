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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.v.R
import com.example.v.components.ServerLocationCard
import com.example.v.components.ConnectionDetailsPanel
import com.example.v.models.Server
import com.example.v.ui.theme.VPNTheme
import com.example.v.ui.theme.getGradientBackground
import com.example.v.ui.theme.ThemeToggleButton
import com.example.v.utils.IPChecker
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    vpnManager: com.example.v.vpn.VPNManager
) {
    // Debug logging for VPNManager
    LaunchedEffect(vpnManager) {
        println("🔍 DEBUG: HomeScreen - vpnManager received: $vpnManager")
        println("🔍 DEBUG: HomeScreen - currentServer: $currentServer")
        println("🔍 DEBUG: HomeScreen - vpnManager class: ${vpnManager.javaClass.simpleName}")
    }
    // State variables
    var isConnected by remember { mutableStateOf(false) }
    var selectedServer by remember { mutableStateOf(currentServer) }
    var sessionStartTime by remember { mutableStateOf<Long?>(null) }
    var sessionDuration by remember { mutableStateOf("00:00:00") }
    var currentIP by remember { mutableStateOf<String?>(null) }
    var originalIP by remember { mutableStateOf<String?>(null) }
    
    // Observe VPN connection state
    val vpnConnectionState by remember(vpnManager) {
        vpnManager.connectionState
    }.collectAsState()
    isConnected = vpnConnectionState == com.example.v.vpn.VPNConnectionState.CONNECTED
    
    // UI state
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Launcher for VPN permission dialog
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
        val granted = vpnManager.hasVpnPermission()
        println("🔍 DEBUG: VPN permission result received. granted=$granted")
        if (granted) {
            println("🔍 DEBUG: Permission granted via dialog. Connecting now...")
            vpnManager.connect(currentServer)
        } else {
            println("🔍 DEBUG: Permission denied by user.")
        }
    }
    
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
    
    // Check IP address changes
    LaunchedEffect(Unit) {
        // Get original IP when screen loads
        launch {
            val ip = IPChecker.getCurrentIP()
            originalIP = ip
            currentIP = ip
            println("🔍 DEBUG: Original IP: $ip")
        }
    }
    
    LaunchedEffect(isConnected) {
        if (isConnected) {
            // Delay a bit for VPN to establish, then check IP
            delay(3000)
            launch {
                val ip = IPChecker.getCurrentIP()
                currentIP = ip
                println("🔍 DEBUG: VPN IP: $ip (Original: $originalIP)")
                
                if (ip != null && originalIP != null && ip != originalIP) {
                    println("✅ VPN IP VERIFICATION PASSED: $originalIP → $ip")
                } else {
                    println("❌ VPN IP VERIFICATION FAILED: IP unchanged ($ip)")
                }
            }
        }
    }

    // Handle VPN connection
    fun handleConnect() {
        println("🔍 DEBUG: handleConnect function called")
        println("🔍 DEBUG: Current state: $vpnConnectionState")
        
        if (vpnConnectionState == com.example.v.vpn.VPNConnectionState.CONNECTED) {
            // Disconnect
            println("🔍 DEBUG: Disconnecting VPN")
            onDisconnect()
        } else if (vpnConnectionState == com.example.v.vpn.VPNConnectionState.CONNECTING) {
            println("🔍 DEBUG: VPN is already connecting...")
            return
        } else {
            // Debug logging
            println("🔍 DEBUG: handleConnect called")
            println("🔍 DEBUG: vpnManager = $vpnManager")
            println("🔍 DEBUG: currentServer = $currentServer")
            println("🔍 DEBUG: currentServer.id = ${currentServer.id}")
            println("🔍 DEBUG: currentServer.city = ${currentServer.city}")
            
            println("🔍 DEBUG: ===============================")
            println("🔍 DEBUG: CONNECT BUTTON CLICKED!")
            println("🔍 DEBUG: ===============================")
            
            // Always check VPN permission first - even if we think we have it
            val hasPermission = vpnManager.hasVpnPermission()
            println("🔍 DEBUG: hasVpnPermission = $hasPermission")
            
            if (hasPermission) {
                // Connect directly to current server
                println("🔍 DEBUG: ✅ Has permission, connecting to ${currentServer.city}")
                println("🔍 DEBUG: Calling vpnManager.connect() now...")
                vpnManager.connect(currentServer)
                println("🔍 DEBUG: vpnManager.connect() call completed")
            } else {
                // Request VPN permission
                println("🔍 DEBUG: ❌ No permission, requesting VPN permission")
                val permissionIntent = vpnManager.getVpnPermissionIntent()
                println("🔍 DEBUG: permissionIntent = $permissionIntent")
                
                if (permissionIntent != null) {
                    println("🔍 DEBUG: Launching VPN permission dialog via Activity Result API...")
                    vpnPermissionLauncher.launch(permissionIntent)
                } else {
                    println("🔍 DEBUG: ❌ permissionIntent is null - this should not happen!")
                    // Try to connect anyway - permission might already be granted
                    println("🔍 DEBUG: Attempting to connect anyway...")
                    vpnManager.connect(currentServer)
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
                        isConnecting = vpnConnectionState == com.example.v.vpn.VPNConnectionState.CONNECTING,
                        onClick = { 
                            println("🔍 DEBUG: Connect button clicked!")
                            println("🔍 DEBUG: Button state - isConnected: $isConnected")
                            println("🔍 DEBUG: Button state - vpnConnectionState: $vpnConnectionState")
                            println("🔍 DEBUG: About to call handleConnect()")
                            handleConnect() 
                            println("🔍 DEBUG: handleConnect() called successfully")
                        },
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
                            localTunnelIp = vpnManager.getLocalTunnelIpv4(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            

        }
    }
}

@Composable
private fun ConnectButton(
    isConnected: Boolean,
    isConnecting: Boolean = false,
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

    val buttonColor = when {
        isConnected -> Color(0xFF4CAF50)
        isConnecting -> Color(0xFFFFA726)
        else -> Color(0xFFFF6C36)
    }
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
            .clickable(enabled = !isConnecting) {
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
                contentDescription = when {
                    isConnected -> "Disconnect"
                    isConnecting -> "Connecting"
                    else -> "Connect"
                },
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    isConnected -> "DISCONNECT"
                    isConnecting -> "CONNECTING..."
                    else -> "CONNECT"
                },
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
    localTunnelIp: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
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
                    val ipText = localTunnelIp ?: "---"
                    Text(
                        text = ipText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                    TextButton(
                        onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(ipText)) },
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
                    val endpoint = server.wireGuardConfig?.serverEndpoint ?: "---"
                    Text(
                        text = endpoint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                    TextButton(
                        onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(endpoint)) },
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