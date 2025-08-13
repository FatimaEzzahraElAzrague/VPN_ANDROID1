package com.example.v.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.v.MainActivity
import com.example.v.R
import com.example.v.models.ClientConfig
import com.example.v.models.Server
import com.example.v.wireguard.RealWireGuardBackend
import com.example.v.wireguard.RealWireGuardBackend.TunnelState
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.*

/**
 * Simple WireGuard VPN Service that actually works
 * Focuses on core VPN functionality without complex features
 */
class WireGuardVpnService : VpnService() {
    
    companion object {
        private const val TAG = "WireGuardVpnService"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "VPN_SERVICE_CHANNEL"
        
        const val ACTION_CONNECT = "com.example.v.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.v.vpn.DISCONNECT"
        const val ACTION_REFRESH_KILL_SWITCH = "com.example.v.vpn.KILL_SWITCH_REFRESH"
        
        @Volatile
        private var isConnected = false
        
        fun isVpnConnected(): Boolean = isConnected
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: NotificationManager
    // Note: We don't manage vpnInterface because GoBackend handles TUN internally
    
    // Current connection info
    private var currentServer: Server? = null
    private var currentClientConfig: ClientConfig? = null
    
    // WireGuard components
    private var realBackend: RealWireGuardBackend? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WireGuard VPN Service created")
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
        
        // Initialize real WireGuard backend
        realBackend = RealWireGuardBackend(this)
        Log.d(TAG, "🔍 DEBUG: Real WireGuard backend available: ${realBackend?.isAvailable()}")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        Log.d(TAG, "🔍 DEBUG: ===============================")
        Log.d(TAG, "🔍 DEBUG: WireGuard VPN service onStartCommand called")
        Log.d(TAG, "🔍 DEBUG: ===============================")
        Log.d(TAG, "🔍 DEBUG: Action received: $action")
        Log.d(TAG, "🔍 DEBUG: StartId: $startId")
        Log.d(TAG, "🔍 DEBUG: Flags: $flags")
        
        when (action) {
            ACTION_CONNECT -> {
                Log.d(TAG, "🔍 DEBUG: Processing CONNECT action...")
                val server = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("server", Server::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("server")
                }
                val clientConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("client_config", ClientConfig::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("client_config")
                }
                
                Log.d(TAG, "🔍 DEBUG: Server extracted: $server")
                Log.d(TAG, "🔍 DEBUG: Server name: ${server?.name}")
                Log.d(TAG, "🔍 DEBUG: Server city: ${server?.city}")
                Log.d(TAG, "🔍 DEBUG: ClientConfig extracted: $clientConfig")
                Log.d(TAG, "🔍 DEBUG: Client private key: ${clientConfig?.privateKey?.take(10)}...")
                
                if (server != null && clientConfig != null) {
                    try {
                        currentServer = server
                        currentClientConfig = clientConfig
                        connectVpn(server, clientConfig)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to connect VPN", e)
                        handleVpnConnectionFailure()
                    }
                } else {
                    Log.e(TAG, "Missing VPN configuration")
                    handleVpnConnectionFailure()
                }
            }
            ACTION_DISCONNECT -> {
                Log.d(TAG, "WireGuard VPN disconnect requested")
                disconnectVpn()
            }
            ACTION_REFRESH_KILL_SWITCH -> {
                Log.d(TAG, "Kill switch refresh requested - but kill switch disabled with GoBackend")
                // Kill switch functionality disabled when using GoBackend to avoid conflicts
            }
            else -> {
                Log.w(TAG, "Unknown action: $action")
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun connectVpn(server: Server, client: ClientConfig) {
        Log.d(TAG, "🔍 DEBUG: ===============================")
        Log.d(TAG, "🔍 DEBUG: connectVpn() called")
        Log.d(TAG, "🔍 DEBUG: ===============================")
        Log.d(TAG, "🔍 DEBUG: Server: ${server.name}")
        Log.d(TAG, "🔍 DEBUG: Client IP: ${client.address}")
        
        serviceScope.launch {
            try {
                Log.d(TAG, "🔍 DEBUG: Starting WireGuard VPN connection in coroutine...")
                // GoBackend handles TUN interface internally - no manual TUN management needed
                Log.d(TAG, "🔍 DEBUG: GoBackend will manage TUN interface internally")
                
                // Start foreground service with notification
                Log.d(TAG, "🔍 DEBUG: Starting foreground service...")
                startForeground(NOTIFICATION_ID, createNotification("Connecting to WireGuard VPN..."))
                
                // Use official GoBackend - it handles TUN interface internally
                val backend = realBackend
                if (backend?.isAvailable() == true) {
                    Log.d(TAG, "🔍 DEBUG: Using REAL WireGuard GoBackend (manages its own TUN)")
                    
                    // CRITICAL: Let GoBackend handle everything, don't create our own VPN interface
                    val success = backend.setState(server, client, TunnelState.UP)
                    if (success) {
                        isConnected = true
                        
                        Log.d(TAG, "✅ WireGuard GoBackend tunnel established!")
                        Log.d(TAG, "🔍 DEBUG: GoBackend is handling all routing internally")

                        // Give it a brief moment to establish routing
                        delay(2000)
                        
                        // Test IP change
                        testIPChange()

                        // Update notification
                        updateNotification("Connected to ${server.city}, ${server.country} (Real WireGuard)")

                        // Broadcast connection status
                        val broadcastIntent = Intent("com.example.v.vpn.CONNECTION_STATUS")
                        broadcastIntent.putExtra("connected", true)
                        sendBroadcast(broadcastIntent)

                        Log.d(TAG, "✅ REAL WireGuard tunnel established successfully!")
                        return@launch
                    } else {
                        Log.e(TAG, "❌ Real WireGuard backend failed to start")
                        handleVpnConnectionFailure()
                    }
                } else {
                    Log.e(TAG, "❌ Real WireGuard backend not available")
                    handleVpnConnectionFailure()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting WireGuard VPN", e)
                handleVpnConnectionFailure()
            }
        }
    }
    
    private fun handleVpnConnectionFailure() {
        isConnected = false
        stopForeground(true)
        stopSelf()
    }
    
    // REMOVED: createVpnBuilder method - GoBackend handles everything internally
    // This was causing conflicts with the real WireGuard tunnel
    
    private fun disconnectVpn() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Disconnecting WireGuard VPN...")
                
                updateNotification("Disconnecting from VPN...")
                
                // Stop WireGuard tunnel - GoBackend handles TUN cleanup internally
                realBackend?.disconnect()
                
                isConnected = false
                
                // Kill switch is disabled when using GoBackend to avoid conflicts
                Log.d(TAG, "🔍 DEBUG: GoBackend handles all TUN management - no kill switch needed")

                // Broadcast disconnection status
                val broadcastIntent = Intent("com.example.v.vpn.CONNECTION_STATUS")
                broadcastIntent.putExtra("connected", false)
                sendBroadcast(broadcastIntent)
                
                Log.d(TAG, "WireGuard VPN disconnected")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                stopForeground(true)
                }
                stopSelf()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting WireGuard VPN", e)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                stopForeground(true)
                }
                stopSelf()
            }
        }
    }
    
    // TUN interface management removed - GoBackend handles this internally
    // Kill switch functionality disabled to avoid conflicts with GoBackend
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vpnChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "WireGuard VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for WireGuard VPN service status"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(vpnChannel)
        }
    }
    
    private fun createNotification(message: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("WireGuard VPN")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_vpn_key)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun updateNotification(message: String) {
        notificationManager.notify(NOTIFICATION_ID, createNotification(message))
    }
    
    private fun testIPChange() {
        serviceScope.launch {
            var attempt = 0
            var success = false
            while (attempt < 3 && !success) {
                try {
                    Log.d(TAG, "🔍 DEBUG: Testing IP change (attempt ${attempt + 1})...")
                    val url = URL("https://api.ipify.org")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000

                    val responseCode = connection.responseCode
                    if (responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val currentIP = reader.readText().trim()
                        reader.close()
                        connection.disconnect()
                        Log.d(TAG, "✅ VPN IP TEST: Current IP is $currentIP")
                        updateNotification("Connected - IP: $currentIP")
                        success = true
                        break
                    } else {
                        Log.w(TAG, "❌ IP test failed with response code: $responseCode")
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error testing IP change", e)
                }
                attempt++
                delay(1500)
            }

            if (!success) {
                Log.e(TAG, "❌ VPN appears not to pass traffic; disconnecting to restore internet")
                withContext(Dispatchers.Main) {
                    handleVpnConnectionFailure()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isConnected = false
        // GoBackend handles TUN cleanup automatically
        serviceScope.cancel()
        Log.d(TAG, "WireGuard VPN Service destroyed")
    }
}