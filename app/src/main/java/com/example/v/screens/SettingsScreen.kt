package com.example.v.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Import theme colors
import com.example.v.ui.theme.*

// Define colors as non-composable functions
private val OrangeCrayola = Color(0xFFFF6B35)

private fun getSecondaryTextColor(): Color = Color.Gray
private fun getPrimaryTextColor(isDarkTheme: Boolean): Color =
    if (isDarkTheme) Color.White else Color.Black
private fun getCardBackgroundColor(isDarkTheme: Boolean): Color =
    if (isDarkTheme) Color(0xFF2D2D2D) else Color.White

// Define missing composable functions
@Composable
private fun TitleText(
    text: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = if (isDarkTheme) Color.White else Color.Black,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onThemeToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle theme",
            tint = if (isDarkTheme) Color.White else Color.Black
        )
    }
}

@Composable
private fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangeCrayola,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAutoConnectPage by remember { mutableStateOf(false) }
    var showKillSwitchPage by remember { mutableStateOf(false) }
    var showSubscriptionPage by remember { mutableStateOf(false) }
    var showSecurityPage by remember { mutableStateOf(false) }
    var showSpeedTestPage by remember { mutableStateOf(false) }
    var showSplitTunnelingPage by remember { mutableStateOf(false) }
    
    // Security states
    var adBlockEnabled by remember { mutableStateOf(true) }
    var malwareBlockEnabled by remember { mutableStateOf(true) }
    var familyModeEnabled by remember { mutableStateOf(false) }
    var splitTunnelingEnabled by remember { mutableStateOf(false) }
    var dnsLeakProtectionEnabled by remember { mutableStateOf(true) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var isSplitTunnelingExpanded by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

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

            // Top bar with theme toggle and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title
                TitleText(
                    text = "Settings",
                    isDarkTheme = isDarkTheme
                )

                // Theme toggle button
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            // Settings content
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Account section
                item {
                    Text(
                        text = "Account",
                            style = MaterialTheme.typography.titleMedium,
                        color = getSecondaryTextColor(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, top = 16.dp)
                    )
                }
                
                item {
                    SettingsRow(
                        title = "Subscription",
                        description = "Manage your subscription",
                        icon = Icons.Default.CreditCard,
                        onClick = { showSubscriptionPage = true },
                        isDarkTheme = isDarkTheme
                    )
                }
                
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = getSecondaryTextColor().copy(alpha = 0.3f)
                    )
                }

                // Connection section
                item {
                    Text(
                        text = "Connection",
                        style = MaterialTheme.typography.titleMedium,
                        color = getSecondaryTextColor(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, top = 16.dp)
                    )
                }
                
                item {
                    SettingsRow(
                        title = "Auto Connect",
                        description = "Automatically connect to VPN",
                        icon = Icons.Default.Wifi,
                        onClick = { showAutoConnectPage = true },
                        isDarkTheme = isDarkTheme
                    )
                }
                
                item {
                    SettingsRow(
                        title = "Kill Switch",
                        description = "Block internet when VPN disconnects",
                        icon = Icons.Default.Security,
                        onClick = { showKillSwitchPage = true },
                        isDarkTheme = isDarkTheme
                    )
                }
                
                item {
                    SettingsRow(
                        title = "Split Tunneling",
                        description = "Choose which apps use VPN",
                        icon = Icons.Default.Settings,
                        onClick = { showSplitTunnelingPage = true },
                        isDarkTheme = isDarkTheme
                    )
                }
                
                item {
                    HorizontalDivider(
                        color = getSecondaryTextColor().copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Security section
                item {
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.titleMedium,
                        color = getSecondaryTextColor(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, top = 16.dp)
                    )
                }
                
                item {
                    SettingsRow(
                        title = "Security Settings",
                        description = "Configure security features",
                        icon = Icons.Default.Shield,
                        onClick = { showSecurityPage = true },
                        isDarkTheme = isDarkTheme
                    )
                }
                
                item {
                    SettingsRow(
                        title = "Speed Test",
                        description = "Test your connection speed",
                        icon = Icons.Default.Speed,
                        onClick = { showSpeedTestPage = true },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }

    // Detail pages
    if (showAutoConnectPage) {
        AutoConnectPage(
            isDarkTheme = isDarkTheme,
            onBack = { showAutoConnectPage = false },
            onThemeToggle = onThemeToggle
        )
    }
    
    if (showKillSwitchPage) {
        KillSwitchPage(
            isDarkTheme = isDarkTheme,
            onBack = { showKillSwitchPage = false },
            onThemeToggle = onThemeToggle
        )
    }
    
    if (showSubscriptionPage) {
        SubscriptionPage(
            isDarkTheme = isDarkTheme,
            onBack = { showSubscriptionPage = false },
            onThemeToggle = onThemeToggle
        )
    }
    
    if (showSecurityPage) {
        SecurityPage(
            isDarkTheme = isDarkTheme,
            onBack = { showSecurityPage = false },
            onThemeToggle = onThemeToggle
        )
    }
    
    if (showSpeedTestPage) {
        SpeedTestPage(
            isDarkTheme = isDarkTheme,
            onBack = { showSpeedTestPage = false },
            onThemeToggle = onThemeToggle
        )
    }
    
    if (showSplitTunnelingPage) {
        SplitTunnelingPage(
            isDarkTheme = isDarkTheme,
            onBack = { showSplitTunnelingPage = false },
            onThemeToggle = onThemeToggle
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = getSecondaryTextColor()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Normal
                )
            }
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = if (isDarkTheme) Color.Gray else Color.DarkGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class SpeedTestResults(
    val downloadSpeed: Float,
    val uploadSpeed: Float,
    val ping: Int,
    val jitter: Int
)

@Composable
private fun AutoConnectPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    var autoConnectEnabled by remember { mutableStateOf(true) }
    var trustedNetworksPermission by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Always") }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Auto Connect",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Auto Connect Settings",
                                style = MaterialTheme.typography.titleLarge,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Auto Connect Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto Connect",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getPrimaryTextColor(isDarkTheme),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Automatically connect to VPN",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getSecondaryTextColor()
                                    )
                                }
                                Switch(
                                    checked = autoConnectEnabled,
                                    onCheckedChange = { autoConnectEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OrangeCrayola,
                                        checkedTrackColor = OrangeCrayola.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                if (autoConnectEnabled) {
                    item {
                        StyledCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Connection Rules",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getPrimaryTextColor(isDarkTheme),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Connection options
                                val options = listOf("Always", "On untrusted networks", "Never")
                                options.forEach { option ->
                                    RadioOption(
                                        text = option,
                                        selected = selectedOption == option,
                                        onSelect = { selectedOption = option },
                                        isDarkTheme = isDarkTheme
                                    )
                                    if (option != options.last()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        StyledCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Trusted Networks",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getPrimaryTextColor(isDarkTheme),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Auto Connect won't connect when on trusted networks",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = getSecondaryTextColor()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                SettingsRow(
                                    title = "Trusted networks",
                                    description = "Auto Connect won't connect to your VPN when this device is on a trusted network",
                                    icon = Icons.Default.Shield,
                                    onClick = {
                                        if (!trustedNetworksPermission) {
                                            trustedNetworksPermission = true
                                        }
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KillSwitchPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    var killSwitchEnabled by remember { mutableStateOf(false) }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Kill Switch",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Kill Switch Protection",
                                style = MaterialTheme.typography.titleLarge,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Block all internet traffic when VPN disconnects",
                                style = MaterialTheme.typography.bodyMedium,
                                color = getSecondaryTextColor()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kill Switch",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getPrimaryTextColor(isDarkTheme),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Protect your privacy when VPN drops",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getSecondaryTextColor()
                                    )
                                }
                                Switch(
                                    checked = killSwitchEnabled,
                                    onCheckedChange = { killSwitchEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OrangeCrayola,
                                        checkedTrackColor = OrangeCrayola.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "How it works",
                                style = MaterialTheme.typography.titleMedium,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            StepItem(
                                number = 1,
                                text = "When Kill Switch is enabled, all internet traffic is blocked",
                                isDarkTheme = isDarkTheme
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StepItem(
                                number = 2,
                                text = "If your VPN connection drops unexpectedly, your device stays protected",
                                isDarkTheme = isDarkTheme
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StepItem(
                                number = 3,
                                text = "Internet access is restored when VPN reconnects",
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Subscription",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Current subscription",
                                style = MaterialTheme.typography.titleLarge,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Yearly Multi Device",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getPrimaryTextColor(isDarkTheme),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Active until Dec 15, 2024",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getSecondaryTextColor()
                                    )
                                }
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = OrangeCrayola.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OrangeCrayola,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Account Details",
                                style = MaterialTheme.typography.titleMedium,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SettingsRow(
                                title = "Linked to",
                                description = "Not linked to Avast Account",
                                icon = Icons.Default.AccountCircle,
                                onClick = { },
                                isDarkTheme = isDarkTheme
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = getSecondaryTextColor().copy(alpha = 0.3f)
                            )
                            
                            SettingsRow(
                                title = "Activation code",
                                description = "VT52XC-5D7YJJ-4HCHHJ",
                                icon = Icons.Default.Key,
                                onClick = { },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    var adBlockEnabled by remember { mutableStateOf(true) }
    var malwareBlockEnabled by remember { mutableStateOf(true) }
    var familyModeEnabled by remember { mutableStateOf(false) }
    var dnsLeakProtectionEnabled by remember { mutableStateOf(true) }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Security Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Protection Features",
                                style = MaterialTheme.typography.titleLarge,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SecurityOption(
                                title = "Ad Blocker",
                                description = "Block ads and trackers",
                                isEnabled = adBlockEnabled,
                                onToggle = { adBlockEnabled = it },
                                isDarkTheme = isDarkTheme
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = getSecondaryTextColor().copy(alpha = 0.3f)
                            )
                            
                            SecurityOption(
                                title = "Malware Protection",
                                description = "Block malicious websites",
                                isEnabled = malwareBlockEnabled,
                                onToggle = { malwareBlockEnabled = it },
                                isDarkTheme = isDarkTheme
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = getSecondaryTextColor().copy(alpha = 0.3f)
                            )
                            
                            SecurityOption(
                                title = "Family Mode",
                                description = "Block inappropriate content",
                                isEnabled = familyModeEnabled,
                                onToggle = { familyModeEnabled = it },
                                isDarkTheme = isDarkTheme
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = getSecondaryTextColor().copy(alpha = 0.3f)
                            )
                            
                            SecurityOption(
                                title = "DNS Leak Protection",
                                description = "Prevent DNS leaks",
                                isEnabled = dnsLeakProtectionEnabled,
                                onToggle = { dnsLeakProtectionEnabled = it },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitTunnelingPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    var splitTunnelingEnabled by remember { mutableStateOf(false) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var isExpanded by remember { mutableStateOf(false) }

    val apps = listOf(
        "Chrome", "Firefox", "Safari", "Gmail", "WhatsApp", "Telegram",
        "Instagram", "Facebook", "Twitter", "YouTube", "Netflix", "Spotify"
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Split Tunneling",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Split Tunneling",
                                style = MaterialTheme.typography.titleLarge,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose which apps use VPN connection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = getSecondaryTextColor()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Enable Split Tunneling",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getPrimaryTextColor(isDarkTheme),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Only selected apps will use VPN",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getSecondaryTextColor()
                                    )
                                }
                                Switch(
                                    checked = splitTunnelingEnabled,
                                    onCheckedChange = { splitTunnelingEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OrangeCrayola,
                                        checkedTrackColor = OrangeCrayola.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                if (splitTunnelingEnabled) {
                    item {
                        StyledCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Select Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getPrimaryTextColor(isDarkTheme),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                apps.forEach { appName ->
                                    val isSelected = selectedApps.contains(appName)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedApps = if (isSelected) {
                                                    selectedApps - appName
                                                } else {
                                                    selectedApps + appName
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Apps,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isSelected) OrangeCrayola else getSecondaryTextColor()
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) getPrimaryTextColor(isDarkTheme) else getSecondaryTextColor(),
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                selectedApps = if (isSelected) {
                                                    selectedApps - appName
                                                } else {
                                                    selectedApps + appName
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = OrangeCrayola,
                                                uncheckedColor = getSecondaryTextColor()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedTestPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<SpeedTestResults?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Speed Test",
                    style = MaterialTheme.typography.titleLarge,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 200.dp) // Increased bottom padding to ensure speed test is touchable
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRunning) {
                            // Car Engine Animation
                            val infiniteTransition = rememberInfiniteTransition(label = "engine")
                            val rotationAngle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "rotation"
                            )
                            
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                OrangeCrayola.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Engine Block
                                Canvas(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .scale(scale)
                                ) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    val radius = size.width / 2 - 20f
                                    
                                    // Engine block (rectangular)
                                    drawRoundRect(
                                        color = getCardBackgroundColor(isDarkTheme),
                                        topLeft = Offset(center.x - radius * 0.8f, center.y - radius * 0.6f),
                                        size = androidx.compose.ui.geometry.Size(radius * 1.6f, radius * 1.2f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f)
                                    )
                                    
                                    // Pistons (cylinders)
                                    for (i in 0..3) {
                                        val pistonX = center.x - radius * 0.6f + (i * radius * 0.4f)
                                        val pistonY = center.y - radius * 0.4f
                                        
                                        // Cylinder
                                        drawCircle(
                                            color = getPrimaryTextColor(isDarkTheme).copy(alpha = 0.3f),
                                            radius = 20f,
                                            center = Offset(pistonX, pistonY)
                                        )
                                        
                                        // Piston (moving)
                                        val pistonOffset = (kotlin.math.sin(rotationAngle * 0.1f + i) * 15f).toFloat()
                                        drawCircle(
                                            color = OrangeCrayola,
                                            radius = 15f,
                                            center = Offset(pistonX, pistonY + pistonOffset)
                                        )
                                    }
                                    
                                    // Crankshaft
                                    drawLine(
                                        color = OrangeCrayola,
                                        start = Offset(center.x - radius * 0.8f, center.y + radius * 0.3f),
                                        end = Offset(center.x + radius * 0.8f, center.y + radius * 0.3f),
                                        strokeWidth = 8f
                                    )
                                    
                                    // Engine details
                                    drawCircle(
                                        color = getSecondaryTextColor(),
                                        radius = 8f,
                                        center = Offset(center.x - radius * 0.4f, center.y + radius * 0.3f)
                                    )
                                    drawCircle(
                                        color = getSecondaryTextColor(),
                                        radius = 8f,
                                        center = Offset(center.x + radius * 0.4f, center.y + radius * 0.3f)
                                    )
                                }
                                
                                // Digital Speed Display
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(
                                            color = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "85",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = OrangeCrayola,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mbps",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = getSecondaryTextColor()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Engine is running at full power!",
                                style = MaterialTheme.typography.titleMedium,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Testing your VPN connection speed...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = getSecondaryTextColor(),
                                textAlign = TextAlign.Center
                            )
                        } else if (results != null) {
                            // Results
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Test Results",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = getPrimaryTextColor(isDarkTheme),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = OrangeCrayola.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Text(
                                            text = "Excellent",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OrangeCrayola,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))

                                // Speed Metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    SpeedMetricCard(
                                        title = "Download",
                                        value = "${results!!.downloadSpeed}",
                                        unit = "Mbps",
                                        color = OrangeCrayola,
                                        icon = Icons.Default.Download,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SpeedMetricCard(
                                        title = "Upload",
                                        value = "${results!!.uploadSpeed}",
                                        unit = "Mbps",
                                        color = OrangeCrayola,
                                        icon = Icons.Default.Upload,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    SpeedMetricCard(
                                        title = "Ping",
                                        value = "${results!!.ping}",
                                        unit = "ms",
                                        color = OrangeCrayola,
                                        icon = Icons.Default.Timer,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SpeedMetricCard(
                                        title = "Jitter",
                                        value = "${results!!.jitter}",
                                        unit = "ms",
                                        color = OrangeCrayola,
                                        icon = Icons.Default.Tune,
                                        isDarkTheme = isDarkTheme,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else {
                            // Start Test
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Engine Start Button
                                Card(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clickable {
                                            isRunning = true
                                            coroutineScope.launch {
                                                delay(5000)
                                                results = SpeedTestResults(
                                                    downloadSpeed = 85.2f,
                                                    uploadSpeed = 42.1f,
                                                    ping = 25,
                                                    jitter = 5
                                                )
                                                isRunning = false
                                            }
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = OrangeCrayola.copy(alpha = 0.1f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = "Start Engine",
                                                tint = OrangeCrayola,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Start Engine",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = OrangeCrayola,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Test Your Connection Speed",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = getPrimaryTextColor(isDarkTheme),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Rev up your engine to test VPN performance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = getSecondaryTextColor(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    isDarkTheme: Boolean,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = OrangeCrayola,
                unselectedColor = if (enabled) getSecondaryTextColor() else getSecondaryTextColor().copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) getPrimaryTextColor(isDarkTheme) else getSecondaryTextColor().copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StepItem(
    number: Int,
    text: String,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(OrangeCrayola.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = OrangeCrayola,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = getPrimaryTextColor(isDarkTheme),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SecurityOption(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = getSecondaryTextColor()
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OrangeCrayola,
                checkedTrackColor = OrangeCrayola.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SpeedMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = getSecondaryTextColor()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.Medium
            )
        }
    }
}