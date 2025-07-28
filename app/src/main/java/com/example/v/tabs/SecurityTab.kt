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
                        checked = adBlockEnabled,
                        onCheckedChange = { adBlockEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "Malware & Phishing Block",
                        description = "Protect against malicious websites",
                        checked = malwareBlockEnabled,
                        onCheckedChange = { malwareBlockEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "Family Mode",
                        description = "Filter adult content and inappropriate websites",
                        checked = familyModeEnabled,
                        onCheckedChange = { familyModeEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "DNS Leak Protection",
                        description = "Prevent DNS queries from bypassing VPN",
                        checked = dnsLeakProtectionEnabled,
                        onCheckedChange = { dnsLeakProtectionEnabled = it }
                    )

                    ToggleSettingItem(
                        title = "Kill Switch",
                        description = "Block internet if VPN connection drops",
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

                    val apps = listOf(
                        "Chrome" to Icons.Default.Language,
                        "WhatsApp" to Icons.Default.Message,
                        "Instagram" to Icons.Default.CameraAlt,
                        "YouTube" to Icons.Default.PlayArrow,
                        "Gmail" to Icons.Default.Email,
                        "Spotify" to Icons.Default.MusicNote,
                        "Netflix" to Icons.Default.Movie,
                        "Banking App" to Icons.Default.AccountBalance
                    )

                    apps.forEach { (appName, icon) ->
                        val isSelected = selectedApps.contains(appName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(appName)
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    selectedApps = if (isSelected) {
                                        selectedApps - appName
                                    } else {
                                        selectedApps + appName
                                    }
                                }
                            )
                        }
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
        Column(modifier = Modifier.weight(1f)) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}