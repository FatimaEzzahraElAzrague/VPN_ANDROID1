package com.example.v.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.v.models.Server
import com.example.v.models.mockServers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    selectedServer: Server?,
    onServerSelect: (Server) -> Unit
) {
    var favoriteServers by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Select Server",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Quick Connect button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Select fastest server (lowest ping)
                    val fastestServer = mockServers.minByOrNull { it.ping }
                    fastestServer?.let { onServerSelect(it) }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Quick Connect",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quick Connect",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Server list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockServers) { server ->
                ServerItem(
                    server = server,
                    isSelected = server.id == selectedServer?.id,
                    isFavorite = favoriteServers.contains(server.id),
                    onSelect = { onServerSelect(server) },
                    onToggleFavorite = {
                        favoriteServers = if (favoriteServers.contains(server.id)) {
                            favoriteServers - server.id
                        } else {
                            favoriteServers + server.id
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ServerItem(
    server: Server,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag
            Text(
                text = server.flagEmoji,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            // Country and city
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = server.country,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = server.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }

            // Server stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Ping indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pingColor = when {
                        server.ping < 30 -> Color(0xFF4CAF50)
                        server.ping < 100 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = pingColor,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${server.ping}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }

                // Server load bar
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = server.load / 100f,
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp),
                        color = when {
                            server.load < 50 -> Color(0xFF4CAF50)
                            server.load < 80 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${server.load}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Favorite toggle
            IconButton(
                onClick = onToggleFavorite
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Connect button or status
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Connected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                OutlinedButton(
                    onClick = onSelect,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Connect",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}