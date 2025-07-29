package com.example.v.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.models.Server

@Composable
private fun ServerItem(
    server: Server,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onServerClick: () -> Unit
) {
    val primaryTextColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF4A5161)
    val secondaryTextColor = Color(0xFF979EAE)
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF1F2838) else Color(0xFFFFFFFF)
    val orangeColor = Color(0xFFFF6C36)

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) orangeColor.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300),
        label = "background"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clickable { onServerClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedBackgroundColor)
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
                                color = primaryTextColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )

                            if (server.isOptimal == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            orangeColor.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "OPTIMAL",
                                        fontSize = 10.sp,
                                        color = orangeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (server.isPremium == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFFFD700).copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PREMIUM",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = server.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor,
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
                        tint = orangeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
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