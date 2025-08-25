package com.example.v.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.v.models.Server
import com.example.v.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    servers: List<Server>,
    selectedServer: Server?,
    connectedServer: Server?,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onServerSelect: (Server) -> Unit,
    onServerFavorite: (Server) -> Unit,
    onBackClick: () -> Unit,
    onVPNPermissionRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val filteredServers = remember(servers, searchQuery, selectedFilter) {
        servers.filter { server ->
            val matchesSearch = server.name.contains(searchQuery, ignoreCase = true) ||
                    server.country.contains(searchQuery, ignoreCase = true) ||
                    server.city.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Favorites" -> false
                "Africa" -> server.country in listOf("South Africa")
                "Asia" -> server.country in listOf("Japan", "Singapore", "South Korea", "Hong Kong", "India", "Indonesia", "Malaysia", "Taiwan", "United Arab Emirates", "Israel")
                "Europe" -> server.country in listOf("United Kingdom", "Switzerland", "France", "Germany", "Spain", "Italy")
                "America" -> server.country in listOf("United States", "Canada", "Brazil", "Mexico")
                "Oceania" -> server.country in listOf("Australia")
                else -> true
            }
            matchesSearch && matchesFilter
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
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = getCardBackgroundColor(isDarkTheme),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title
            Text(
                text = "Servers",
                style = MaterialTheme.typography.headlineMedium,
                    color = getPrimaryTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )

                // Theme toggle button
                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = getCardBackgroundColor(isDarkTheme),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = getPrimaryTextColor(isDarkTheme),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

        // Quick Connect Button
        Button(
            onClick = {
                    val optimalServer = servers.first()
                    optimalServer?.let { onServerSelect(it) }
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                    containerColor = getOrangeColor()
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                    contentDescription = "Quick Connect",
                    tint = LightWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Connect to Fastest Server",
                    color = LightWhite,
                    fontWeight = FontWeight.SemiBold
            )
        }



        // Search Bar
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
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
                    focusedBorderColor = getOrangeColor(),
                    unfocusedBorderColor = getSecondaryTextColor(),
                    focusedTextColor = getPrimaryTextColor(isDarkTheme),
                    unfocusedTextColor = getPrimaryTextColor(isDarkTheme)
            ),
            shape = RoundedCornerShape(12.dp)
        )

            // Filter Buttons
        Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                val filters = listOf("All", "Favorites", "Africa", "Asia", "Europe", "America", "Oceania")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getOrangeColor(),
                            selectedLabelColor = LightWhite
                        )
                    )
                }
            }

            // Server List
        Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredServers) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = selectedServer?.id == server.id,
                        isConnected = connectedServer?.id == server.id,
                        isDarkTheme = isDarkTheme,
                        onServerSelect = onServerSelect,
                        onServerFavorite = onServerFavorite
                    )
                }
            }
        }
    }
}



@Composable
private fun ServerListItem(
    server: Server,
    isSelected: Boolean,
    isConnected: Boolean,
    isDarkTheme: Boolean,
    onServerSelect: (Server) -> Unit,
    onServerFavorite: (Server) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onServerSelect(server) }
            .background(
                color = when {
                    isConnected -> Color(0xFFE53E3E).copy(alpha = 0.15f) // Faded red for connected
                    isSelected -> Color(0xFFE53E3E).copy(alpha = 0.1f) // Faded red for selected
                    else -> getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag/Country icon
                    Text(
                        text = server.flag,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                    )

                    Column {
                        Text(
                            text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = getPrimaryTextColor(isDarkTheme),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${server.ping}ms Ping • ${server.load}% Load",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSecondaryTextColor(),
                        fontSize = 14.sp
                    )
                    
                    // Status badges
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                    }
                }
            }

            // Right side - Connection status and favorite
            Row(
                verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                // Connection indicator
                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFE53E3E), CircleShape)
                    )
                }
                
                // Favorite button
                IconButton(
                    onClick = { onServerFavorite(server) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = getSecondaryTextColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}