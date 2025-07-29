package com.example.v.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.models.Server
import com.example.v.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    servers: List<Server>,
    selectedServer: Server?,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onServerSelect: (Server) -> Unit,
    onBackClick: () -> Unit
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
                TitleText(
                    text = "Server List",
                    isDarkTheme = isDarkTheme
                )

                // Theme toggle button
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            // Server list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(servers) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = selectedServer?.id == server.id,
                        isDarkTheme = isDarkTheme,
                        onServerSelect = onServerSelect
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
    isDarkTheme: Boolean,
    onServerSelect: (Server) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onServerSelect(server) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                getOrangeColor().copy(alpha = 0.1f) 
            else 
                getCardBackgroundColor(isDarkTheme).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag/Country icon placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isSelected) getOrangeColor() else getSecondaryTextColor(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = server.country.take(2).uppercase(),
                        color = if (isSelected) LightWhite else getPrimaryTextColor(isDarkTheme),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = server.country,
                        style = MaterialTheme.typography.titleMedium,
                        color = getPrimaryTextColor(isDarkTheme),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = server.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSecondaryTextColor(),
                        fontSize = 14.sp
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = getOrangeColor(),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Ping indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellular4Bar,
                        contentDescription = "Signal",
                        tint = getSecondaryTextColor(),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${server.ping}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = getSecondaryTextColor(),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
