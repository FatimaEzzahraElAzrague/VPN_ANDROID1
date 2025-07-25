package com.example.v

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.v.models.Server
import com.example.v.screens.HomeScreen
import com.example.v.screens.ServerListScreen
import com.example.v.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VPNNavigation(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    var isConnected by remember { mutableStateOf(false) }
    var selectedServer by remember { mutableStateOf<Server?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listOf(
                    BottomNavItem("home", "Home", Icons.Default.Home),
                    BottomNavItem("servers", "Servers", Icons.Default.List),
                    BottomNavItem("settings", "Settings", Icons.Default.Settings)
                ).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    isConnected = isConnected,
                    selectedServer = selectedServer,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    onConnectToggle = { isConnected = !isConnected },
                    onServerClick = { navController.navigate("servers") }
                )
            }
            composable("servers") {
                ServerListScreen(
                    selectedServer = selectedServer,
                    onServerSelect = { server ->
                        selectedServer = server
                        navController.popBackStack()
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBackPressed = { navController.popBackStack() },
                    onSignOut = onSignOut,
                    onEditAccount = { navController.navigate("editAccount") },
                    onShowSubscription = { navController.navigate("subscription") },
                    onLanguageSettings = { navController.navigate("language") },
                    onNotificationSettings = { navController.navigate("notifications") },
                    onPrivacyPolicy = { navController.navigate("privacyPolicy") },
                    onTermsOfService = { navController.navigate("termsOfService") }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)