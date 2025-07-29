package com.example.v.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.models.Server
import com.example.v.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Theme Colors - moved to Colors.kt
// val LightCharcoal = Color(0xFF4A5161)
// val LightCadetGray = Color(0xFF979EAE)
// val LightSeasalt = Color(0xFFF9F9F7)
// val LightOrangeCrayola = Color(0xFFFF6C36)
// val LightWhite = Color(0xFFFFFFFF)

// val DarkBlack = Color(0xFF090909)
// val DarkOxfordBlue = Color(0xFF182132)
// val DarkGunmetal = Color(0xFF2B3440)
// val DarkOrangeCrayola = Color(0xFFFF6C36)
// val DarkGunmetalSecondary = Color(0xFF1F2838)

// val OrangeCrayola = Color(0xFFFF6C36)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isConnected: Boolean,
    selectedServer: Server?,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onConnectToggle: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    var isConnecting by remember { mutableStateOf(false) }
    var connectionDuration by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    // Animation for connecting state
    val infiniteTransition = rememberInfiniteTransition(label = "connecting")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Connection duration timer
    LaunchedEffect(isConnected) {
        if (isConnected) {
            val startTime = System.currentTimeMillis()
            while (isConnected) {
                connectionDuration = System.currentTimeMillis() - startTime
                delay(1000)
            }
            connectionDuration = 0L
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                // Theme toggle button
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )

                // App title
                TitleText(
                    text = "SecureLine VPN",
                    isDarkTheme = isDarkTheme
                )

                // Spacer to balance the layout
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Main connection circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                // Store colors in variables to avoid @Composable calls in Canvas
                val circleColor = getCircleColor(isDarkTheme)
                val orangeColor = getOrangeColor()
                
                // Background circles for depth
                repeat(3) { index ->
                    Canvas(
                        modifier = Modifier.size(300.dp - (index * 40).dp)
                    ) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.minDimension / 2 - 10.dp.toPx()

                        drawCircle(
                            color = circleColor.copy(alpha = 0.3f - (index * 0.1f)),
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Main connection circle
                Canvas(
                    modifier = Modifier
                        .size(250.dp)
                        .then(
                            if (isConnecting) Modifier.rotate(rotationAngle) else Modifier
                        )
                ) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2 - 15.dp.toPx()

                    // Outer ring
                    drawCircle(
                        color = circleColor,
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Progress ring
                    if (isConnected || isConnecting) {
                        val sweepAngle = if (isConnecting) 90f else 360f
                        drawArc(
                            color = orangeColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(
                                width = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                // Center content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            isConnecting -> "CONNECTING"
                            isConnected -> "CONNECTED"
                            else -> "TAP TO CONNECT"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = when {
                            isConnected -> orangeColor
                            isConnecting -> orangeColor
                            else -> getPrimaryTextColor(isDarkTheme)
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    if (isConnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatDuration(connectionDuration),
                            style = MaterialTheme.typography.titleMedium,
                            color = getSecondaryTextColor(),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }
                }

                // Connect/Disconnect button
                if (!isConnecting) {
                    FloatingActionButton(
                        onClick = {
                            if (isConnected) {
                                onConnectToggle()
                            } else {
                                isConnecting = true
                                scope.launch {
                                    delay(3000)
                                    isConnecting = false
                                    onConnectToggle()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(if (isConnected) 56.dp else 0.dp)
                            .offset(x = 80.dp, y = 80.dp),
                        containerColor = LightWhite,
                        contentColor = LightCharcoal
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isConnected) "Disconnect" else "Connect",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Tap area for connection when disconnected
                if (!isConnected && !isConnecting) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .clickable {
                                isConnecting = true
                                scope.launch {
                                    delay(3000)
                                    isConnecting = false
                                    onConnectToggle()
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Server location section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp) // Increased padding to avoid nav bar
            ) {
                Text(
                    text = "Server location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getSecondaryTextColor(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Server selection card
                StyledCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServerClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isDarkTheme) OrangeCrayola.copy(alpha = 0.2f) else getOrangeColor().copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Lightning",
                                    tint = if (isDarkTheme) OrangeCrayola else getOrangeColor(),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = selectedServer?.let {
                                    "${it.country}, ${it.city}"
                                } ?: "Optimal Location",
                                style = MaterialTheme.typography.titleMedium,
                                color = getPrimaryTextColor(isDarkTheme),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropUp,
                            contentDescription = "Expand",
                            tint = if (isDarkTheme) OrangeCrayola else getPrimaryTextColor(isDarkTheme),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}