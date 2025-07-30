package com.example.v.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConnectButton(
    isConnected: Boolean,
    isConnecting: Boolean = false,
    daysLeft: Int = 45,
    selectedRegion: String = "France, Paris",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ghostShieldBlue = Color(0xFF4285F4)
    val ghostShieldGreen = Color(0xFF34A853)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Premium badge (only shown in web version)

        // Main button container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isConnected) ghostShieldGreen else ghostShieldBlue)
                    .clickable { onClick() }
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isConnected) "DISCONNECT" else "TAP TO CONNECT",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!isConnected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Server location",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Optional location selector
        if (!isConnected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Optional Location",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* Handle location change */ }
            )
        }
    }
}