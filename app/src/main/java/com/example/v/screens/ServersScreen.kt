package com.example.v.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.v.data.ServersData
import com.example.v.models.Server
import com.example.v.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    servers: List<Server> = ServersData.servers,
    selectedServer: Server? = null,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onServerSelect: (Server) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }
    
    val regions = listOf("All", "US", "Europe", "Asia Pacific", "Canada", "Middle East", "Africa", "South America", "Mexico", "Israel")
    
    val filteredServers = remember(servers, searchQuery, selectedRegion) {
        servers.filter { server ->
            val matchesSearch = server.country.contains(searchQuery, ignoreCase = true) ||
                    server.city.contains(searchQuery, ignoreCase = true)
            
            val matchesRegion = when (selectedRegion) {
                "All" -> true
                "US" -> server.id.startsWith("us-")
                "Europe" -> server.id.startsWith("eu-")
                "Asia Pacific" -> server.id.startsWith("ap-")
                "Canada" -> server.id.startsWith("ca-")
                "Middle East" -> server.id.startsWith("me-")
                "Africa" -> server.id.startsWith("af-")
                "South America" -> server.id.startsWith("sa-")
                "Mexico" -> server.id.startsWith("mx-")
                "Israel" -> server.id.startsWith("il-")
                else -> true
            }
            
            matchesSearch && matchesRegion
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
            // Status bar spacer
            Spacer(modifier = Modifier.height(40.dp))

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme)
                    )
                }

                // Title
                TitleText(
                    text = "Servers",
                    isDarkTheme = isDarkTheme
                )

                // Theme toggle
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = {
                    Text(
                        text = "Search servers...",
                        color = getSecondaryTextColor()
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = getSecondaryTextColor()
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeCrayola,
                    unfocusedBorderColor = getSecondaryTextColor(),
                    focusedLabelColor = OrangeCrayola,
                    unfocusedLabelColor = getSecondaryTextColor(),
                    cursorColor = OrangeCrayola
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Quick Connect Button
            PrimaryButton(
                onClick = {
                    val optimalServer = ServersData.getOptimalServer()
                    if (optimalServer != null) {
                        onServerSelect(optimalServer)
                    }
                },
                text = "Quick Connect to Fastest Server",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                icon = Icons.Default.FlashOn
            )

            // Region filter
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(regions) { region ->
                    FilterChip(
                        onClick = { selectedRegion = region },
                        label = { Text(region) },
                        selected = selectedRegion == region,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeCrayola,
                            selectedLabelColor = LightWhite
                        )
                    )
                }
            }

            // Server count
            Text(
                text = "${filteredServers.size} servers available",
                style = MaterialTheme.typography.bodyMedium,
                color = getSecondaryTextColor(),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Server list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredServers) { server ->
                    ServerItem(
                        server = server,
                        isSelected = selectedServer?.id == server.id,
                        isDarkTheme = isDarkTheme,
                        onServerClick = { onServerSelect(server) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerItem(
    server: Server,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onServerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onServerClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Country flag
                Text(
                    text = server.flag,
                    fontSize = 32.sp,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = server.country,
                            style = MaterialTheme.typography.titleMedium,
                            color = getPrimaryTextColor(isDarkTheme),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )

                        if (server.isOptimal) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = OrangeCrayola.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Optimal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangeCrayola,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (server.isPremium) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Premium",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = server.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSecondaryTextColor(),
                        fontSize = 14.sp
                    )
                }
            }

            // Server stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Ping indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SignalStrengthIndicator(
                        strength = getPingStrength(server.ping),
                        color = getPingColor(server.ping)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${server.ping}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = getPingColor(server.ping),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Load indicator
                Text(
                    text = "Load: ${server.load}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = getLoadColor(server.load),
                    fontSize = 11.sp
                )
            }

            // Selection indicator
            if (isSelected) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = OrangeCrayola,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SignalStrengthIndicator(
    strength: Int,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((6 + index * 3).dp)
                    .background(
                        color = if (index < strength) color else color.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

private fun getPingStrength(ping: Int): Int {
    return when {
        ping < 50 -> 3
        ping < 100 -> 2
        else -> 1
    }
}

private fun getPingColor(ping: Int): Color {
    return when {
        ping < 50 -> Color(0xFF4CAF50) // Green
        ping < 100 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }
}

private fun getLoadColor(load: Int): Color {
    return when {
        load < 30 -> Color(0xFF4CAF50) // Green
        load < 70 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }
}