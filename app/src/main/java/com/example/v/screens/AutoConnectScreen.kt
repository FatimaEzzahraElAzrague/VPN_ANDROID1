package com.example.v.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.v.data.autoconnect.AutoConnectMode
import com.example.v.data.autoconnect.AutoConnectRepository
import kotlinx.coroutines.launch

@Composable
fun AutoConnectScreen(repo: AutoConnectRepository) {
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(AutoConnectMode.ANY_WIFI_OR_CELLULAR) }

    LaunchedEffect(Unit) {
        val current = repo.get()
        if (current != null) {
            enabled = current.enabled
            mode = current.mode
        }
    }

    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Auto Connect", style = MaterialTheme.typography.titleMedium)
            Switch(checked = enabled, onCheckedChange = {
                enabled = it
                scope.launch { repo.set(enabled = enabled, mode = mode) }
            })
        }

        Spacer(Modifier.height(16.dp))

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                AutoConnectMode.UNSECURED_WIFI_ONLY to "Unsecured Wi‑Fi only",
                AutoConnectMode.ANY_WIFI to "Any Wi‑Fi",
                AutoConnectMode.ANY_WIFI_OR_CELLULAR to "Any Wi‑Fi or cellular"
            ).forEach { (value, label) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label)
                    RadioButton(
                        selected = mode == value,
                        onClick = if (enabled) {
                            {
                                mode = value
                                scope.launch { repo.set(enabled = enabled, mode = mode) }
                            }
                        } else null
                    )
                }
            }
        }
    }
}


