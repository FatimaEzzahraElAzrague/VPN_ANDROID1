// MainActivity.kt
package com.example.v

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.v.navigation.AuthNavigation
import com.example.v.autoconnect.AutoConnectManager
import com.example.v.data.autoconnect.AutoConnectRepository
import com.example.v.vpn.VPNManager
import com.example.v.navigation.VPNNavigation
import com.example.v.services.GoogleSignInService
import com.example.v.ui.theme.VPNTheme
import com.example.v.auth.AuthManager
import kotlinx.coroutines.launch
import com.example.v.data.ServersData
import android.util.Log

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInService: GoogleSignInService
    private var onGoogleSignInSuccess: (() -> Unit)? = null
    private var navigateToSignIn: (() -> Unit)? = null
    private lateinit var vpnManager: VPNManager

    // VPN permission launcher
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.i("MainActivity", "✅ VPN permission granted")
            // VPN permission granted, can now connect
            vpnManager.setVPNPermissionCallback { granted ->
                if (granted) {
                    Log.i("MainActivity", "✅ VPN permission confirmed")
                } else {
                    Log.w("MainActivity", "⚠️ VPN permission denied")
                }
            }
        } else {
            Log.w("MainActivity", "⚠️ VPN permission denied or cancelled")
        }
    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AuthManager
        AuthManager.initialize(this)
        
        // Initialize VPN Manager
        vpnManager = VPNManager.getInstance(this)
        vpnManager.initialize()
        
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
                },
                onVPNPermissionRequest = {
                    requestVPNPermission()
                }
            )
        }

        // Start Auto-Connect monitoring once app launches
        val repo = AutoConnectRepository(applicationContext)
        
        // Get the first available server (since we removed getOptimalServer)
        val currentServer = ServersData.servers.firstOrNull() ?: ServersData.servers[0]

        val autoConnectManager = AutoConnectManager(
            context = applicationContext,
            repo = repo,
            startVpnTunnel = {
                // Use optimal server when auto-connecting
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        // Check VPN permission first
                        val intent = vpnManager.prepareVPNPermission()
                        if (intent != null) {
                            vpnPermissionLauncher.launch(intent)
                        } else {
                            // Permission already granted, connect
                            vpnManager.connectToVPN(currentServer.id)
                        }
                    }
                }
            }
        )
        autoConnectManager.start()
    }

    /**
     * Request VPN permission
     */
    private fun requestVPNPermission() {
        val intent = vpnManager.prepareVPNPermission()
        if (intent != null) {
            Log.i("MainActivity", "🔐 Requesting VPN permission...")
            vpnPermissionLauncher.launch(intent)
        } else {
            Log.i("MainActivity", "✅ VPN permission already granted")
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VPNApp(
    onGoogleSignInRequest: () -> Unit = {},
    onGoogleSignInSuccess: (() -> Unit) -> Unit = {},
    exposeNavigateToSignIn: ((() -> Unit) -> Unit) = {},
    onVPNPermissionRequest: () -> Unit = {}
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
                onSignOut = { isAuthenticated = false },
                onVPNPermissionRequest = onVPNPermissionRequest
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VPNApp()
}