package com.example.v.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.v.tabs.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Account", "Security", "Speed Test", "Statistics", "General")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Tab row
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> AccountTab()
            1 -> SecurityTab()
            2 -> SpeedTestTab()
            3 -> StatisticsTab()
            4 -> GeneralTab(onSignOut = onSignOut)
        }
    }
}