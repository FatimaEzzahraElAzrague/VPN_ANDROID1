// MainActivity.kt
package com.example.v

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.v.navigation.AuthNavigation
import com.example.v.navigation.VPNNavigation
import com.example.v.services.GoogleSignInService
import com.example.v.ui.theme.VPNTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInService: GoogleSignInService
    private var onGoogleSignInSuccess: (() -> Unit)? = null
    private var navigateToSignIn: (() -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleGoogleSignInResult(result.data)
        } else {
            // Ensure UI unblocks
            navigateToSignIn?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInService = GoogleSignInService(this)

        setContent {
            VPNApp(
                onGoogleSignInRequest = {
                    val signInIntent = googleSignInService.getSignInIntent()
                    googleSignInLauncher.launch(signInIntent)
                },
                onGoogleSignInSuccess = { success ->
                    onGoogleSignInSuccess = success
                },
                exposeNavigateToSignIn = { navFn ->
                    navigateToSignIn = navFn
                }
            )
        }
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        // Launch coroutine to handle the result
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val response = googleSignInService.handleSignInResult(data)
                when {
                    // Existing active user -> proceed to app
                    response.accessToken != null -> onGoogleSignInSuccess?.invoke()
                    // New Google signup -> send user to sign-in screen to continue
                    response.requiresVerification == true -> navigateToSignIn?.invoke()
                    else -> navigateToSignIn?.invoke()
                }
            } catch (e: Exception) {
                // On error, stay in auth flow
                navigateToSignIn?.invoke()
            }
        }
    }
}

@Composable
fun VPNApp(
    onGoogleSignInRequest: () -> Unit = {},
    onGoogleSignInSuccess: (() -> Unit) -> Unit = {},
    exposeNavigateToSignIn: ((() -> Unit) -> Unit) = {}
) {
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
                onAuthSuccess = { isAuthenticated = true },
                onGoogleSignInRequest = onGoogleSignInRequest,
                exposeNavigateToSignIn = exposeNavigateToSignIn
            )
        }
    }

    // Set the success callback
    LaunchedEffect(Unit) {
        onGoogleSignInSuccess { isAuthenticated = true }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VPNApp()
}