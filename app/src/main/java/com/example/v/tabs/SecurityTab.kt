package com.example.v.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.derivedStateOf
import com.example.v.components.AppIcon
import com.example.v.models.InstalledApp
import com.example.v.utils.AppUtils

@Composable
fun SecurityTab() {
    var adBlockEnabled by remember { mutableStateOf(true) }
    var malwareBlockEnabled by remember { mutableStateOf(true) }
    var familyModeEnabled by remember { mutableStateOf(false) }
    var splitTunnelingEnabled by remember { mutableStateOf(false) }
    var dnsLeakProtectionEnabled by remember { mutableStateOf(true) }
    var killSwitchEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Protection Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Protection Features",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ToggleSettingItem(
                        title = "Ad & Tracker Block",
                        description = "Block advertisements and tracking scripts",
                        icon = Icons.Default.Block,
                        checked = adBlockEnabled,
                        onCheckedChange = { adBlockEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "Malware & Phishing Block",
                        description = "Protect against malicious websites",
                        icon = Icons.Default.Security,
                        checked = malwareBlockEnabled,
                        onCheckedChange = { malwareBlockEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "Family Mode",
                        description = "Filter adult content and inappropriate websites",
                        icon = Icons.Default.FamilyRestroom,
                        checked = familyModeEnabled,
                        onCheckedChange = { familyModeEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "DNS Leak Protection",
                        description = "Prevent DNS queries from bypassing VPN",
                        icon = Icons.Default.NetworkCheck,
                        checked = dnsLeakProtectionEnabled,
                        onCheckedChange = { dnsLeakProtectionEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "Kill Switch",
                        description = "Block internet if VPN connection drops",
                        icon = Icons.Default.PowerSettingsNew,
                        checked = killSwitchEnabled,
                        onCheckedChange = { killSwitchEnabled = it }
                    )
                }
            }
        }

        item {
            // Split Tunneling
            SplitTunnelingCard(
                enabled = splitTunnelingEnabled,
                onEnabledChange = { splitTunnelingEnabled = it }
            )
        }
    }
}

@Composable
fun SplitTunnelingCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var showSystemApps by remember { mutableStateOf(false) }
    var isLoadingApps by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val installedApps by remember {
        derivedStateOf {
            try {
                isLoadingApps = true
                val apps = AppUtils.getFilteredApps(context, showSystemApps)
                isLoadingApps = false
                apps
            } catch (e: Exception) {
                isLoadingApps = false
                emptyList()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Split Tunneling",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Split Tunneling",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Choose which apps use VPN",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        onEnabledChange(it)
                        if (it) isExpanded = true
                    }
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Apps (${selectedApps.size} selected)")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Add filter toggle for system apps
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show System Apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Switch(
                            checked = showSystemApps,
                            onCheckedChange = { showSystemApps = it },
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    // App count
                    Text(
                        text = "${installedApps.size} apps found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isLoadingApps) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else if (installedApps.isEmpty()) {
                        Text(
                            text = "No apps found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        installedApps.take(10).forEach { app ->
                            val isSelected = selectedApps.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AppIcon(
                                        drawable = app.appIcon,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (app.isSystemApp) {
                                            Text(
                                                text = "System App",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.5f
                                                ),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        selectedApps = if (isSelected) {
                                            selectedApps - app.packageName
                                        } else {
                                            selectedApps + app.packageName
                                        }
                                    }
                                )
                            }
                        }

                        if (installedApps.size > 10) {
                            Text(
                                text = "... and ${installedApps.size - 10} more apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ToggleSettingItem(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}