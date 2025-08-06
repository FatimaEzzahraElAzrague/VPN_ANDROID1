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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.v.components.SpeedTestGauge
import com.example.v.components.AppIcon
import com.example.v.models.InstalledApp
import com.example.v.utils.AppUtils
import androidx.compose.ui.platform.LocalContext

// Import theme colors
import com.example.v.ui.theme.*

// Define SpeedTestResults data class
data class SpeedTestResults(
    val downloadSpeed: Float,
    val uploadSpeed: Float,
    val ping: Int,
    val jitter: Int
)

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
            containerColor = Color(0xFFFF6B35),
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

@Composable
private fun StyledCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        content = { content() }
    )
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

@Composable
private fun SpeedTestPage(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<SpeedTestResults?>(null) }
    var realTimeResults by remember { mutableStateOf(SpeedTestResults(0f, 0f, 0, 0)) }
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isRunning) {
                    // Show real-time speed test
                    SpeedTestGauge(
                        downloadSpeed = realTimeResults.downloadSpeed,
                        uploadSpeed = realTimeResults.uploadSpeed,
                        ping = realTimeResults.ping.toFloat(),
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        isRealTime = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Testing Your Connection in Real-Time...",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Measuring download, upload, and ping speeds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else if (results != null) {
                    // Show results with enhanced car engine gauges
                    SpeedTestGauge(
                        downloadSpeed = results!!.downloadSpeed,
                        uploadSpeed = results!!.uploadSpeed,
                        ping = results!!.ping.toFloat(),
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )

                    // Fine line separator
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 24.dp),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    // Performance analysis without card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Connection Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getPrimaryTextColor(isDarkTheme),
                                    fontWeight = FontWeight.Bold
                                )

                                val connectionQuality = when {
                                    results!!.downloadSpeed > 100f -> "Excellent"
                                    results!!.downloadSpeed > 50f -> "Good"
                                    results!!.downloadSpeed > 25f -> "Fair"
                                    else -> "Poor"
                                }

                                val qualityColor = when (connectionQuality) {
                                    "Excellent" -> Color(0xFF4CAF50)
                                    "Good" -> Color(0xFF8BC34A)
                                    "Fair" -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = qualityColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = connectionQuality,
                                        color = qualityColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Server location info with car dashboard styling
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Server",
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Test Server: New York, USA",
                                    color = getSecondaryTextColor(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Time",
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Test completed at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}",
                                    color = getSecondaryTextColor(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Run another test button with automotive styling
                            PrimaryButton(
                                onClick = {
                                    isRunning = true
                                    results = null
                                    realTimeResults = SpeedTestResults(0f, 0f, 0, 0)
                                    coroutineScope.launch {
                                        // Simulate real-time speed test
                                        for (i in 1..30) {
                                            delay(100) // Update every 100ms
                                            realTimeResults = SpeedTestResults(
                                                downloadSpeed = (i * 3f + (0..15).random().toFloat()),
                                                uploadSpeed = (i * 1f + (0..8).random().toFloat()),
                                                ping = (15 + (0..25).random()),
                                                jitter = (1..6).random()
                                            )
                                        }
                                        // Final results
                                        results = SpeedTestResults(
                                            downloadSpeed = (80..150).random().toFloat(),
                                            uploadSpeed = (20..50).random().toFloat(),
                                            ping = (10..50).random(),
                                            jitter = (1..10).random()
                                        )
                                        isRunning = false
                                    }
                                },
                                text = "Run Another Test",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                else {
                    // Initial state - no test run yet with automotive styling
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))

                        // Large speedometer icon
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = size.minDimension / 3f

                                // Draw speedometer background
                                drawCircle(
                                    color = Color(0xFFFF6B35).copy(alpha = 0.1f),
                                    radius = radius + 20f,
                                    center = center
                                )

                                drawArc(
                                    color = Color(0xFFFF6B35).copy(alpha = 0.3f),
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx()),
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed Test",
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Speed Test Ready",
                            style = MaterialTheme.typography.titleLarge,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Test your connection with our automotive-grade speed measurement",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getSecondaryTextColor(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        PrimaryButton(
                            onClick = {
                                isRunning = true
                                realTimeResults = SpeedTestResults(0f, 0f, 0, 0)
                                coroutineScope.launch {
                                    // Simulate real-time speed test
                                    for (i in 1..30) {
                                        delay(100) // Update every 100ms
                                        realTimeResults = SpeedTestResults(
                                            downloadSpeed = (i * 3f + (0..15).random().toFloat()),
                                            uploadSpeed = (i * 1f + (0..8).random().toFloat()),
                                            ping = (15 + (0..25).random()),
                                            jitter = (1..6).random()
                                        )
                                    }
                                    // Final results
                                    results = SpeedTestResults(
                                        downloadSpeed = (80..150).random().toFloat(),
                                        uploadSpeed = (20..50).random().toFloat(),
                                        ping = (10..50).random(),
                                        jitter = (1..10).random()
                                    )
                                    isRunning = false
                                }
                            },
                            text = "Start Speed Test",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp)
                        )
                    }
                }
            }
        }
    }
}

// Keep all other existing functions...
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
                                        checkedThumbColor = Color(0xFFFF6B35),
                                        checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
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
                                        checkedThumbColor = Color(0xFFFF6B35),
                                        checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
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
                                        containerColor = Color(0xFFFF6B35).copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFFF6B35),
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
    var showSystemApps by remember { mutableStateOf(false) }
    var isLoadingApps by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val installedApps by remember {
        derivedStateOf {
            try {
                val apps = AppUtils.getFilteredApps(context, showSystemApps)
                apps
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

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
                                        checkedThumbColor = Color(0xFFFF6B35),
                                        checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                if (splitTunnelingEnabled) {
                    item {
                                Text(
                                    text = "Select Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                                )
                    }

                    item {
                                // Add filter toggle for system apps
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Show System Apps",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getSecondaryTextColor(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = showSystemApps,
                                        onCheckedChange = { showSystemApps = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFFF6B35),
                                            checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
                                        )
                                    )
                        }
                                }

                    item {
                                // App count
                                Text(
                                    text = "${installedApps.size} apps found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = getSecondaryTextColor(),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                    }

                                    items(
                                        items = installedApps,
                                        key = { it.packageName }
                                    ) { app ->
                                        val isSelected = selectedApps.contains(app.packageName)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedApps = if (isSelected) {
                                                        selectedApps - app.packageName
                                                } else {
                                                        selectedApps + app.packageName
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                            AppIcon(
                                                drawable = app.appIcon,
                                            tint = if (isSelected) Color(0xFFFF6B35) else getSecondaryTextColor()
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                    text = app.appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) getPrimaryTextColor(isDarkTheme) else getSecondaryTextColor(),
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                                if (app.isSystemApp) {
                                                    Text(
                                                        text = "System App",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = getSecondaryTextColor(),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                selectedApps = if (isSelected) {
                                                        selectedApps - app.packageName
                                                } else {
                                                        selectedApps + app.packageName
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFFF6B35),
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
                selectedColor = Color(0xFFFF6B35),
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
                        colors = listOf(Color(0xFFFF6B35).copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B35),
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
                checkedThumbColor = Color(0xFFFF6B35),
                checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
            )
        )
    }
}