// screens/HomeScreen.kt
package com.example.v.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.v.components.ConnectionButton
import com.example.v.components.ServerLocationCard
import com.example.v.models.Server
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.v.screens.VPNMapScreen

@Composable
fun HomeScreen(
    currentServer: Server,
    onServerChange: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var isConnected by remember { mutableStateOf(false) }

    // Default user location (create a proper Server instance)
    val userLocation = remember {
        Server(
            id = "user",
            country = "Your Location",
            city = "",
            flag = "",
            ping = 0,
            load = 0,
            isOptimal = false,
            isPremium = false,
            latitude = 34.02,
            longitude = -6.84
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map as background
        android.util.Log.d("HomeScreen", "Creating VPNMapScreen with user: ${userLocation.latitude},${userLocation.longitude} server: ${currentServer.latitude},${currentServer.longitude}")
        VPNMapScreen(
            userLat = userLocation.latitude,
            userLng = userLocation.longitude,
            server = currentServer,
            modifier = Modifier.fillMaxSize()
        )

        // UI overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            ConnectionButton(
                isConnected = isConnected,
                onToggle = { isConnected = !isConnected }
            )

            Spacer(modifier = Modifier.height(32.dp))

            ServerLocationCard(
                server = currentServer,
                onClick = onServerChange
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}