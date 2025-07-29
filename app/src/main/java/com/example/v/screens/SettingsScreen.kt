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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.v.tabs.*
import com.example.v.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

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
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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

            // Settings Content with Navigation
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Account Section
                item {
                    SettingsSection(
                        title = "ACCOUNT",
                        isDarkTheme = isDarkTheme
                    ) {
                        SettingsRow(
                            title = "Subscription",
                            description = "Yearly Multi-Device subscription",
                            isDarkTheme = isDarkTheme,
                            onClick = { showSubscriptionPage = true }
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f)
                        )
                        SettingsRow(
                            title = "Sign Out",
                            description = "Sign out of your account",
                            isDarkTheme = isDarkTheme,
                            onClick = onSignOut
                        )
                    }
                }

                // Connection Rules Section
                item {
                    SettingsSection(
                        title = "CONNECTION RULES",
                        isDarkTheme = isDarkTheme
                    ) {
                        SettingsRow(
                            title = "Auto Connect",
                            description = "Choose when your VPN automatically connects or disconnects",
                            isDarkTheme = isDarkTheme,
                            status = "On",
                            statusColor = OrangeCrayola,
                            onClick = { showAutoConnectPage = true }
                        )
                        Divider(
                            color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        SettingsRow(
                            title = "Split Tunneling",
                            description = "Only selected apps will use the VPN",
                            isDarkTheme = isDarkTheme,
                            status = if (splitTunnelingEnabled) "On" else "Off",
                            statusColor = if (splitTunnelingEnabled) OrangeCrayola else getSecondaryTextColor(),
                            onClick = { showSplitTunnelingPage = true }
                        )
                        Divider(
                            color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        SettingsRow(
                            title = "Kill Switch",
                            description = "Protects you by blocking all internet traffic if your VPN connection unexpectedly drops",
                            isDarkTheme = isDarkTheme,
                            status = "Off",
                            statusColor = getSecondaryTextColor(),
                            onClick = { showKillSwitchPage = true }
                        )
                    }
                }

                // Security Section
                item {
                    SettingsSection(
                        title = "SECURITY",
                        isDarkTheme = isDarkTheme
                    ) {
                        SettingsRow(
                            title = "Security Settings",
                            description = "Configure ad blocking, malware protection, and more",
                            isDarkTheme = isDarkTheme,
                            onClick = { showSecurityPage = true }
                        )
                    }
                }

                // Speed Test Section
                item {
                    SettingsSection(
                        title = "SPEED TEST",
                        isDarkTheme = isDarkTheme
                    ) {
                        SettingsRow(
                            title = "Connection Speed",
                            description = "Test your download and upload speeds",
                            isDarkTheme = isDarkTheme,
                            onClick = { showSpeedTestPage = true }
                        )
                    }
                }
            }
        }
    }

    // Detail Pages
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
private fun SettingsSection(
    title: String,
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = getSecondaryTextColor(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun SettingsRow(
    title: String,
    description: String,
    isDarkTheme: Boolean,
    status: String? = null,
    statusColor: Color = getSecondaryTextColor(),
    onClick: () -> Unit
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = getPrimaryTextColor(isDarkTheme),
                fontWeight = FontWeight.Normal
            )
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getSecondaryTextColor(),
                    lineHeight = 20.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (status != null) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = getSecondaryTextColor(),
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
    var selectedOption by remember { mutableStateOf(1) } // 0: Unsecured, 1: Any Wi-Fi, 2: Any Wi-Fi or cellular
    var trustedNetworksPermission by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                        tint = if (isDarkTheme) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Auto Connect",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Auto Connect Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (autoConnectEnabled) "On" else "Off",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (autoConnectEnabled) OrangeCrayola else if (isDarkTheme) Color.Gray else Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = autoConnectEnabled,
                            onCheckedChange = { autoConnectEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OrangeCrayola,
                                checkedTrackColor = OrangeCrayola.copy(alpha = 0.5f),
                                uncheckedThumbColor = if (isDarkTheme) Color.Gray else Color.DarkGray,
                                uncheckedTrackColor = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Connection Rules (only show if auto connect is enabled)
                if (autoConnectEnabled) {
                    item {
                        Column {
                            Text(
                                text = "Automatically connect to my VPN when I connect to:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.Gray else Color.DarkGray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Radio Options
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                RadioOption(
                                    text = "Unsecured Wi-Fi",
                                    selected = selectedOption == 0,
                                    onSelect = { selectedOption = 0 },
                                    isDarkTheme = isDarkTheme,
                                    enabled = autoConnectEnabled
                                )
                                RadioOption(
                                    text = "Any Wi-Fi",
                                    selected = selectedOption == 1,
                                    onSelect = { selectedOption = 1 },
                                    isDarkTheme = isDarkTheme,
                                    enabled = autoConnectEnabled
                                )
                                RadioOption(
                                    text = "Any Wi-Fi or cellular data",
                                    selected = selectedOption == 2,
                                    onSelect = { selectedOption = 2 },
                                    isDarkTheme = isDarkTheme,
                                    enabled = autoConnectEnabled
                                )
                            }

                            if (selectedOption == 2) {
                                Text(
                                    text = "This option might drain your battery faster",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = getSecondaryTextColor(),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    // Trusted Networks
                    item {
                        SettingsRow(
                            title = "Trusted networks",
                            description = "Auto Connect won't connect to your VPN when this device is on a trusted network",
                            isDarkTheme = isDarkTheme,
                            status = if (trustedNetworksPermission) "Allowed" else "Allow",
                            statusColor = if (trustedNetworksPermission) OrangeCrayola else getSecondaryTextColor(),
                            onClick = {
                                if (!trustedNetworksPermission) {
                                    trustedNetworksPermission = true
                                }
                            }
                        )
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                        tint = if (isDarkTheme) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Kill Switch",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Kill Switch Description
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kill Switch",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isDarkTheme) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Protects your data from leaking by blocking all internet traffic if your VPN connection unexpectedly drops",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.Gray else Color.DarkGray
                            )
                        }
                        Text(
                            text = "Off",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // How to turn on Kill Switch
                item {
                    Column {
                        Text(
                            text = "How to turn on Kill Switch",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StepItem(
                                number = 1,
                                text = "Tap Open Android Settings, then tap the settings icon",
                                isDarkTheme = isDarkTheme
                            )
                            StepItem(
                                number = 2,
                                text = "Turn on Always-on VPN",
                                isDarkTheme = isDarkTheme
                            )
                            StepItem(
                                number = 3,
                                text = "Then turn on Block connection without VPN",
                                isDarkTheme = isDarkTheme
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            onClick = { },
                            text = "OPEN ANDROID SETTINGS",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Please Note
                item {
                    Column {
                        Text(
                            text = "PLEASE NOTE",
                            style = MaterialTheme.typography.titleMedium,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "Kill Switch may interfere with some of our other VPN features. Here's what to expect",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getSecondaryTextColor(),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "• If you connect to a trusted network, your VPN will turn off, and Kill Switch will block your internet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getSecondaryTextColor(),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "• If you use Split Tunneling, Kill Switch will block any app not selected for Split Tunneling from using the internet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getSecondaryTextColor()
                        )
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
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Valid until
                item {
                    Column {
                        Text(
                            text = "Valid until",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "July 16, 2026",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f)
                )

                // Current subscription
                item {
                    Column {
                        Text(
                            text = "Current subscription",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Yearly Multi Device subscription",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f)
                )

                // Linked to
                item {
                    Column {
                        Text(
                            text = "Linked to",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Not linked to Avast Account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                        TextButton(
                            onClick = { },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = OrangeCrayola
                            )
                        ) {
                            Text(
                                text = "LINK TO AVAST ACCOUNT",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Divider(
                    color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Activation code
                item {
                    Column {
                        Text(
                            text = "Activation code",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "VT52XC-5D7YJJ-4HCHHJ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TextButton(
                                onClick = { },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = OrangeCrayola
                                )
                            ) {
                                Text(
                                    text = "COPY",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = { },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = OrangeCrayola
                                )
                            ) {
                                Text(
                                    text = "SHARE",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Divider(
                    color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Manage subscription
                item {
                    Column {
                        Text(
                            text = "Manage subscription",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cancel or change your subscription",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                        TextButton(
                            onClick = { },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = OrangeCrayola
                            )
                        ) {
                            Text(
                                text = "MANAGE",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Divider(
                    color = getSecondaryTextColor().copy(alpha = 0.3f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Remove Device
                item {
                    TextButton(
                        onClick = { },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFE57373)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "REMOVE DEVICE FROM MY SUBSCRIPTION",
                            fontWeight = FontWeight.Bold
                        )
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
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                    SecurityOption(
                        title = "Ad & Tracker Block",
                        description = "Block advertisements and tracking scripts",
                        isEnabled = adBlockEnabled,
                        onToggle = { adBlockEnabled = it },
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    Divider(
                        color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                item {
                    SecurityOption(
                        title = "Malware & Phishing Block",
                        description = "Protect against malicious websites",
                        isEnabled = malwareBlockEnabled,
                        onToggle = { malwareBlockEnabled = it },
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    Divider(
                        color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                item {
                    SecurityOption(
                        title = "Family Mode",
                        description = "Filter adult content and inappropriate websites",
                        isEnabled = familyModeEnabled,
                        onToggle = { familyModeEnabled = it },
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    Divider(
                        color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                item {
                    SecurityOption(
                        title = "DNS Leak Protection",
                        description = "Prevent DNS queries from leaking",
                        isEnabled = dnsLeakProtectionEnabled,
                        onToggle = { dnsLeakProtectionEnabled = it },
                        isDarkTheme = isDarkTheme
                    )
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

    val availableApps = listOf(
        "Chrome" to Icons.Default.Language,
        "WhatsApp" to Icons.Default.Message,
        "Instagram" to Icons.Default.CameraAlt,
        "YouTube" to Icons.Default.PlayArrow,
        "Gmail" to Icons.Default.Email,
        "Spotify" to Icons.Default.MusicNote,
        "Netflix" to Icons.Default.Movie,
        "Banking App" to Icons.Default.AccountBalance,
        "Facebook" to Icons.Default.Person,
        "Twitter" to Icons.Default.Chat,
        "TikTok" to Icons.Default.VideoLibrary,
        "Telegram" to Icons.Default.Send
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                    color = if (isDarkTheme) Color.White else Color.Black,
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
                // Split Tunneling Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Split Tunneling",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDarkTheme) Color.White else Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Only selected apps will use the VPN",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.Gray else Color.DarkGray
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

                if (splitTunnelingEnabled) {
                    item {
                        Divider(
                            color = getSecondaryTextColor().copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        Text(
                            text = "Select apps to use VPN:",
                            style = MaterialTheme.typography.titleMedium,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(availableApps) { (appName, icon) ->
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
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
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
                            }
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
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme)
                        listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
                    else
                        listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                        tint = if (isDarkTheme) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Speed Test",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRunning) {
                            // Speedometer Animation
                            val infiniteTransition = rememberInfiniteTransition(label = "needle")
                            val needleAngle by infiniteTransition.animateFloat(
                                initialValue = 135f,
                                targetValue = 405f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(3000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "needle"
                            )

                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF2196F3).copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Speedometer Gauge
                                Canvas(
                                    modifier = Modifier.size(240.dp)
                                ) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    val radius = size.width / 2 - 20f

                                    // Draw speedometer scale
                                    drawArc(
                                        color = Color(0xFF2196F3),
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                        style = Stroke(width = 8f)
                                    )

                                    // Draw speed numbers
                                    for (i in 0..16) {
                                        val angle = 135f + (i * 270f / 16)
                                        val numberRadius = radius - 30f
                                        val x = center.x + (numberRadius * kotlin.math.cos(Math.toRadians(angle.toDouble()))).toFloat()
                                        val y = center.y + (numberRadius * kotlin.math.sin(Math.toRadians(angle.toDouble()))).toFloat()

                                        drawIntoCanvas { canvas ->
                                            canvas.nativeCanvas.drawText(
                                                (i * 10).toString(),
                                                x - 10f,
                                                y + 5f,
                                                android.graphics.Paint().apply {
                                                    color = android.graphics.Color.WHITE
                                                    textSize = 14f
                                                    textAlign = android.graphics.Paint.Align.CENTER
                                                }
                                            )
                                        }
                                    }

                                    // Draw needle
                                    val needleLength = radius - 40f
                                    val needleEndX = center.x + (needleLength * kotlin.math.cos(Math.toRadians(needleAngle.toDouble()))).toFloat()
                                    val needleEndY = center.y + (needleLength * kotlin.math.sin(Math.toRadians(needleAngle.toDouble()))).toFloat()

                                    drawLine(
                                        color = Color.White,
                                        start = center,
                                        end = Offset(needleEndX, needleEndY),
                                        strokeWidth = 4f
                                    )
                                }

                                // Digital Speed Display
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF2196F3).copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            ),
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
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mbps",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Testing Connection Speed",
                                style = MaterialTheme.typography.titleMedium,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Speedometer is measuring your VPN performance...",
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
                                // Speedometer Start Button
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
                                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
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
                                                contentDescription = "Start Speedometer",
                                                tint = Color(0xFF2196F3),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Start Test",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFF2196F3),
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
                                    text = "Use the speedometer to measure your VPN performance",
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