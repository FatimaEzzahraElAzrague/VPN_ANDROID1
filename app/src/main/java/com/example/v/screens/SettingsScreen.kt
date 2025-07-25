package com.example.v.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.v.viewmodels.*
import com.example.v.components.ErrorHandler
import com.example.v.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onSignOut: () -> Unit,
    onEditAccount: () -> Unit,
    onShowSubscription: () -> Unit,
    onLanguageSettings: () -> Unit,
    onNotificationSettings: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
    accountViewModel: AccountViewModel = viewModel(),
    securityViewModel: SecurityViewModel = viewModel(),
    splitTunnelingViewModel: SplitTunnelingViewModel = viewModel(),
    speedTestViewModel: SpeedTestViewModel = viewModel(),
    statisticsViewModel: StatisticsViewModel = viewModel()
) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    // Error handling
    ErrorHandler(
        error = settingsUiState.error,
        onDismiss = { settingsViewModel.clearError() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (settingsUiState.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AccountSection(
                        viewModel = accountViewModel,
                        onEditAccount = onEditAccount,
                        onShowSubscription = onShowSubscription,
                        onSignOut = onSignOut
                    )
                }

                item {
                    SecurityFeaturesSection(
                        viewModel = securityViewModel
                    )
                }

                item {
                    SplitTunnelingSection(
                        viewModel = splitTunnelingViewModel
                    )
                }

                item {
                    SpeedTestSection(
                        viewModel = speedTestViewModel
                    )
                }

                item {
                    StatisticsSection(
                        viewModel = statisticsViewModel
                    )
                }

                item {
                    GeneralSettingsSection(
                        onLanguageSettings = onLanguageSettings,
                        onNotificationSettings = onNotificationSettings
                    )
                }

                item {
                    AboutSection(
                        onPrivacyPolicy = onPrivacyPolicy,
                        onTermsOfService = onTermsOfService
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSection(
    viewModel: AccountViewModel,
    onEditAccount: () -> Unit,
    onShowSubscription: () -> Unit,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsCard(title = "Account") {
        Column {
            SettingsItem(
                title = "Edit Account",
                icon = Icons.Default.Person,
                onClick = onEditAccount
            )

            SubscriptionStatusItem(
                status = uiState.subscriptionStatus,
                onClick = onShowSubscription
            )

            SettingsItem(
                title = "Sign Out",
                icon = Icons.Default.ExitToApp,
                isLoading = uiState.isSigningOut,
                onClick = onSignOut,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun SecurityFeaturesSection(
    viewModel: SecurityViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsCard(title = "Security Features") {
        Column {
            SecurityToggleItem(
                title = "Ad/Tracker Block",
                description = "Block ads and tracking elements",
                checked = uiState.adBlockEnabled,
                onCheckedChange = { viewModel.toggleAdBlock(it) }
            )

            SecurityToggleItem(
                title = "Malware/Phishing Block",
                description = "Protect against malicious websites",
                checked = uiState.malwareBlockEnabled,
                onCheckedChange = { viewModel.toggleMalwareBlock(it) }
            )

            SecurityToggleItem(
                title = "Family Mode",
                description = "Filter inappropriate content",
                checked = uiState.familyModeEnabled,
                onCheckedChange = { viewModel.toggleFamilyMode(it) }
            )

            SecurityToggleItem(
                title = "DNS Leak Protection",
                description = "Prevent DNS queries from leaking",
                checked = uiState.dnsLeakProtectionEnabled,
                onCheckedChange = { viewModel.toggleDnsLeakProtection(it) }
            )

            SecurityToggleItem(
                title = "Kill Switch",
                description = "Block internet if VPN disconnects",
                checked = uiState.killSwitchEnabled,
                onCheckedChange = { viewModel.toggleKillSwitch(it) }
            )
        }
    }
}

@Composable
private fun SplitTunnelingSection(
    viewModel: SplitTunnelingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }

    SettingsCard(title = "Split Tunneling") {
        Column {
            SettingsToggleItem(
                title = "Split Tunneling",
                description = "Allow selected apps to bypass VPN",
                checked = uiState.isEnabled,
                onCheckedChange = { viewModel.toggleSplitTunneling(it) }
            )

            AnimatedVisibility(visible = uiState.isEnabled) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select Apps (${uiState.selectedApps.size} selected)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess
                                    else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle apps list"
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    when {
                                        uiState.isLoadingApps -> {
                                            LoadingIndicator()
                                        }
                                        uiState.error != null -> {
                                            Text(
                                                text = "Error loading apps: ${uiState.error}",
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                        uiState.installedApps.isEmpty() -> {
                                            Text(
                                                text = "No apps found",
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                        else -> {
                                            LazyColumn(
                                                modifier = Modifier.heightIn(max = 300.dp)
                                            ) {
                                                items(uiState.installedApps) { app ->
                                                    AppSelectionItem(
                                                        app = app,
                                                        isSelected = app.packageName in uiState.selectedApps,
                                                        onSelectionChange = { selected ->
                                                            viewModel.toggleAppSelection(app.packageName, selected)
                                                        }
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
        }
    }
}

@Composable
private fun SpeedTestSection(
    viewModel: SpeedTestViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsCard(title = "Speed Test") {
        Column {
            if (uiState.results != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedTestResultItem("Download", "${uiState.results.downloadSpeed} Mbps")
                    SpeedTestResultItem("Upload", "${uiState.results.uploadSpeed} Mbps")
                    SpeedTestResultItem("Ping", "${uiState.results.ping} ms")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isRunning) {
                LinearProgressIndicator(
                    progress = uiState.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { viewModel.startSpeedTest() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isRunning
            ) {
                if (uiState.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testing...")
                } else {
                    Text("Start Speed Test")
                }
            }
        }
    }
}

@Composable
private fun StatisticsSection(
    viewModel: StatisticsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    SettingsCard(title = "Statistics") {
        Column {
            StatisticItem("Data Used Today", "${uiState.dailyUsage.used} / ${uiState.dailyUsage.limit}")
            StatisticItem("Data Used This Month", "${uiState.monthlyUsage.used} / ${uiState.monthlyUsage.limit}")

            Spacer(modifier = Modifier.height(8.dp))

            StatisticItem("Current Download", "${uiState.connectionStats.downloadSpeed} Mbps")
            StatisticItem("Current Upload", "${uiState.connectionStats.uploadSpeed} Mbps")
            StatisticItem("Connection Time", uiState.connectionStats.connectionTime)
        }
    }
}

@Composable
private fun GeneralSettingsSection(
    onLanguageSettings: () -> Unit,
    onNotificationSettings: () -> Unit
) {
    SettingsCard(title = "General Settings") {
        Column {
            SettingsActionItem(
                title = "Language",
                value = "English",
                onClick = onLanguageSettings
            )

            SettingsActionItem(
                title = "Notifications",
                value = "Enabled",
                onClick = onNotificationSettings
            )

            SettingsActionItem(
                title = "Reset App",
                value = "Reset to defaults",
                onClick = { /* Handle reset */ }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "1.2.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit
) {
    SettingsCard(title = "About") {
        Column {
            SettingsActionItem(
                title = "Privacy Policy",
                onClick = onPrivacyPolicy
            )

            SettingsActionItem(
                title = "Terms of Service",
                onClick = onTermsOfService
            )
        }
    }
}

// Reusable Components

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    tint = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SubscriptionStatusItem(
    status: SubscriptionStatus,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subscription Status",
                style = MaterialTheme.typography.bodyLarge
            )

            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        text = status.displayName,
                        fontSize = 12.sp
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (status == SubscriptionStatus.Premium)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AppSelectionItem(
    app: AppInfo,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChange
        )

        app.icon?.let { icon ->
            Icon(
                painter = rememberDrawablePainter(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = app.name,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SecurityToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsToggleItem(
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
private fun SpeedTestResultItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                value?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}