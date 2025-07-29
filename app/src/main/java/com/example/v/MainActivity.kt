// MainActivity.kt
package com.example.v

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.v.navigation.AuthNavigation
import com.example.v.ui.theme.VPNTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VPNApp()
        }
    }
}

@Composable
fun VPNApp() {
    var isDarkTheme by remember { mutableStateOf<Boolean?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }
    val systemDarkTheme = isSystemInDarkTheme()
    val actualDarkTheme = isDarkTheme ?: systemDarkTheme

    VPNTheme(darkTheme = actualDarkTheme) {
        if (isAuthenticated) {
            VPNNavigation(
                isDarkTheme = actualDarkTheme,
                onThemeToggle = {
                    isDarkTheme = if (isDarkTheme == null) !systemDarkTheme else !isDarkTheme!!
                },
                onSignOut = { isAuthenticated = false }
            )
        } else {
            AuthNavigation(
                isDarkTheme = actualDarkTheme,
                onThemeToggle = {
                    isDarkTheme = if (isDarkTheme == null) !systemDarkTheme else !isDarkTheme!!
                },
                onAuthSuccess = { isAuthenticated = true }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VPNApp()
}