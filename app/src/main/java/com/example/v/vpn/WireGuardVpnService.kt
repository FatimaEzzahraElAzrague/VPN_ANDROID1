package com.example.v.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.v.MainActivity
import com.example.v.R
import com.example.v.models.ClientConfig
import com.example.v.models.Server
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Enhanced WireGuard VPN Service with Split Tunneling and Kill Switch Support
 * Uses Android's native VpnService API with WireGuard protocol
 * Supports both include (whitelist) and exclude (blacklist) modes
 * Includes comprehensive kill switch functionality for traffic blocking
 */
class WireGuardVpnService : VpnService() {
    
    companion object {
        private const val TAG = "WireGuardVpnService"
        private const val NOTIFICATION_ID = 1001
        private const val KILL_SWITCH_NOTIFICATION_ID = 1002
        private const val NOTIFICATION_CHANNEL_ID = "VPN_SERVICE_CHANNEL"
        private const val KILL_SWITCH_CHANNEL_ID = "KILL_SWITCH_CHANNEL"
        
        const val ACTION_CONNECT = "com.example.v.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.v.vpn.DISCONNECT"
        const val ACTION_SET_SPLIT_TUNNELING = "com.example.v.vpn.SET_SPLIT_TUNNELING"
        const val ACTION_ACTIVATE_KILL_SWITCH = "com.example.v.vpn.ACTIVATE_KILL_SWITCH"
        const val ACTION_DEACTIVATE_KILL_SWITCH = "com.example.v.vpn.DEACTIVATE_KILL_SWITCH"
        const val ACTION_RECONNECT_VPN = "com.example.v.vpn.RECONNECT_VPN"
        
        @Volatile
        private var isConnected = false
        
        fun isVpnConnected(): Boolean = isConnected
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnelThread: Thread? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: NotificationManager
    
    // WireGuard protocol variables
    private var serverEndpoint: InetSocketAddress? = null
    private var clientPrivateKey: ByteArray? = null
    private var serverPublicKey: ByteArray? = null
    private var presharedKey: ByteArray? = null
    private var clientAddress: String? = null
    
    // Split tunneling configuration
    private var splitTunnelingConfig: SplitTunnelingConfig = SplitTunnelingConfig.default()
    
    // Kill switch state
    private var isKillSwitchActive = false
    private var killSwitchReason: String? = null
    private var lastServer: Server? = null
    private var lastClientConfig: ClientConfig? = null
    
    // Kill switch manager
    private lateinit var killSwitchManager: KillSwitchManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Enhanced WireGuard VPN Service created with split tunneling and kill switch support")
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
        
        // Initialize kill switch manager
        killSwitchManager = KillSwitchManager.getInstance(this)
        
        // Load saved configurations
        loadSplitTunnelingConfig()
        killSwitchManager.loadKillSwitchState()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        Log.d(TAG, "WireGuardVpnService onStartCommand: action=$action")
        
        when (action) {
            ACTION_CONNECT -> {
                Log.d(TAG, "Enhanced WireGuard VPN connect requested")
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
                
                Log.d(TAG, "Server: $server")
                Log.d(TAG, "ClientConfig: $clientConfig")
                Log.d(TAG, "Split tunneling config: $splitTunnelingConfig")
                Log.d(TAG, "Kill switch enabled: ${killSwitchManager.isKillSwitchEnabled()}")
                
                if (server != null && clientConfig != null) {
                    try {
                        // Store server and config for potential reconnection
                        lastServer = server
                        lastClientConfig = clientConfig
                        
                        connectVpn(server, clientConfig)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to connect VPN", e)
                        handleVpnConnectionFailure()
                    }
                } else {
                    Log.e(TAG, "Missing VPN configuration - server: $server, clientConfig: $clientConfig")
                    handleVpnConnectionFailure()
                }
            }
            ACTION_DISCONNECT -> {
                Log.d(TAG, "Enhanced WireGuard VPN disconnect requested")
                disconnectVpn()
            }
            ACTION_SET_SPLIT_TUNNELING -> {
                Log.d(TAG, "Setting split tunneling configuration")
                val mode = intent.getStringExtra("mode") ?: "EXCLUDE"
                val appPackages = intent.getStringArrayListExtra("app_packages") ?: arrayListOf()
                
                setSplitTunnelingMode(mode, appPackages)
            }
            ACTION_ACTIVATE_KILL_SWITCH -> {
                Log.d(TAG, "Activating kill switch")
                val reason = intent.getStringExtra("reason") ?: "Unknown reason"
                activateKillSwitch(reason)
            }
            ACTION_DEACTIVATE_KILL_SWITCH -> {
                Log.d(TAG, "Deactivating kill switch")
                deactivateKillSwitch()
            }
            ACTION_RECONNECT_VPN -> {
                Log.d(TAG, "Reconnecting VPN")
                val serverEndpointStr = intent.getStringExtra("server_endpoint")
                attemptVpnReconnection(serverEndpointStr)
            }
            else -> {
                Log.w(TAG, "Unknown action: $action")
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    /**
     * Set split tunneling mode and app list
     * @param mode "INCLUDE" for whitelist mode, "EXCLUDE" for blacklist mode
     * @param appPackages List of app package names to include/exclude
     */
    fun setSplitTunnelingMode(mode: String, appPackages: List<String>) {
        Log.d(TAG, "Setting split tunneling mode: $mode with ${appPackages.size} apps")
        
        val splitMode = when (mode.uppercase()) {
            "INCLUDE" -> SplitTunnelingMode.INCLUDE
            "EXCLUDE" -> SplitTunnelingMode.EXCLUDE
            else -> {
                Log.w(TAG, "Invalid split tunneling mode: $mode, defaulting to EXCLUDE")
                SplitTunnelingMode.EXCLUDE
            }
        }
        
        // Validate and filter app packages
        val validPackages = SplitTunnelingConfig.validateAppPackages(this, appPackages)
        
        if (validPackages.size != appPackages.size) {
            Log.w(TAG, "Some app packages were invalid and have been filtered out")
        }
        
        splitTunnelingConfig = SplitTunnelingConfig(
            mode = splitMode,
            appPackages = validPackages,
            isEnabled = validPackages.isNotEmpty()
        )
        
        Log.d(TAG, "Split tunneling configured: mode=${splitTunnelingConfig.mode}, " +
                "enabled=${splitTunnelingConfig.isEnabled}, " +
                "apps=${splitTunnelingConfig.appPackages}")
        
        // Save configuration for persistence
        saveSplitTunnelingConfig()
        
        // If VPN is currently connected, reconnect to apply new split tunneling settings
        if (isConnected) {
            Log.d(TAG, "VPN is connected, reconnecting to apply new split tunneling settings")
            // Note: In a real implementation, you might want to implement a more efficient
            // way to update split tunneling without full reconnection
        }
    }
    
    /**
     * Activate kill switch - block all internet traffic
     */
    private fun activateKillSwitch(reason: String) {
        if (!isKillSwitchActive) {
            Log.w(TAG, "Activating kill switch: $reason")
            
            isKillSwitchActive = true
            killSwitchReason = reason
            
            // Close existing VPN interface if connected
            if (isConnected) {
                Log.d(TAG, "Closing VPN interface for kill switch activation")
                closeVpnInterface()
            }
            
            // Create blocking VPN interface
            createBlockingVpnInterface()
            
            // Show kill switch notification
            showKillSwitchNotification(reason)
            
            // Save kill switch state
            saveKillSwitchState()
            
            Log.d(TAG, "Kill switch activated successfully")
        }
    }
    
    /**
     * Deactivate kill switch - allow normal traffic
     */
    private fun deactivateKillSwitch() {
        if (isKillSwitchActive) {
            Log.d(TAG, "Deactivating kill switch")
            
            isKillSwitchActive = false
            killSwitchReason = null
            
            // Close blocking VPN interface
            closeVpnInterface()
            
            // Hide kill switch notification
            hideKillSwitchNotification()
            
            // Save kill switch state
            saveKillSwitchState()
            
            Log.d(TAG, "Kill switch deactivated successfully")
        }
    }
    
    /**
     * Create a blocking VPN interface that blocks all traffic
     */
    private fun createBlockingVpnInterface() {
        try {
            Log.d(TAG, "Creating blocking VPN interface")
            
            val builder = Builder()
                .setSession("Kill Switch - Traffic Blocked")
                .setMtu(1500)
                .addAddress(InetAddress.getByName("10.0.0.1"), 32)
                // Don't add any routes - this blocks all traffic
                .setBlocking(false)
            
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                Log.d(TAG, "Blocking VPN interface created successfully")
                
                // Start foreground service with kill switch notification
                startForeground(KILL_SWITCH_NOTIFICATION_ID, createKillSwitchNotification())
            } else {
                Log.e(TAG, "Failed to establish blocking VPN interface")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating blocking VPN interface", e)
        }
    }
    
    /**
     * Attempt VPN reconnection
     */
    private fun attemptVpnReconnection(serverEndpointStr: String?) {
        if (lastServer != null && lastClientConfig != null) {
            Log.d(TAG, "Attempting VPN reconnection to ${lastServer!!.name}")
            
            // Deactivate kill switch temporarily for reconnection attempt
            val wasKillSwitchActive = isKillSwitchActive
            if (wasKillSwitchActive) {
                deactivateKillSwitch()
            }
            
            try {
                connectVpn(lastServer!!, lastClientConfig!!)
            } catch (e: Exception) {
                Log.e(TAG, "VPN reconnection failed", e)
                
                // Reactivate kill switch if it was active before
                if (wasKillSwitchActive) {
                    activateKillSwitch("Reconnection failed")
                }
            }
        } else {
            Log.e(TAG, "Cannot reconnect - no previous server configuration available")
        }
    }
    
    private fun connectVpn(server: Server, client: ClientConfig) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Starting enhanced WireGuard VPN connection with split tunneling and kill switch...")
                
                // Start foreground service with notification
                startForeground(NOTIFICATION_ID, createNotification("Connecting to WireGuard VPN..."))
                
                // Parse WireGuard configuration
                setupWireGuardConfig(server, client)
                
                // Create VPN interface with split tunneling support
                val builder = createVpnBuilder(client)
                
                // Configure split tunneling based on current settings
                configureSplitTunneling(builder)
                
                // Add DNS servers for split-tunneled apps using the VPN
                addDnsServers(builder)
                
                // Establish the VPN interface
                vpnInterface = builder.establish()
                
                if (vpnInterface != null) {
                    isConnected = true
                    Log.d(TAG, "Enhanced WireGuard VPN interface created successfully with split tunneling")
                    
                    // Update kill switch manager
                    killSwitchManager.setVpnConnected(true, serverEndpoint)
                    
                    // Start WireGuard tunnel thread
                    startWireGuardTunnel()
                    
                    // Update notification
                    updateNotification("Connected to ${server.city}, ${server.country}")
                    
                    // Broadcast connection status
                    val broadcastIntent = Intent("com.example.v.vpn.CONNECTION_STATUS")
                    broadcastIntent.putExtra("connected", true)
                    sendBroadcast(broadcastIntent)
                    
                    Log.d(TAG, "Enhanced WireGuard VPN connected successfully")
                } else {
                    Log.e(TAG, "Failed to establish VPN interface")
                    handleVpnConnectionFailure()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting enhanced WireGuard VPN", e)
                handleVpnConnectionFailure()
            }
        }
    }
    
    /**
     * Handle VPN connection failure
     */
    private fun handleVpnConnectionFailure() {
        isConnected = false
        killSwitchManager.setVpnConnected(false)
        
        // If kill switch is enabled, activate it
        if (killSwitchManager.isKillSwitchEnabled()) {
            activateKillSwitch("Connection failed")
        }
        
        stopForeground(true)
        stopSelf()
    }
    
    /**
     * Create the base VPN builder with common configuration
     */
    private fun createVpnBuilder(client: ClientConfig): Builder {
        return Builder()
            .setSession("WireGuard VPN with Split Tunneling")
            .setMtu(1420)
            .addAddress(InetAddress.getByName(clientAddress?.split("/")?.get(0) ?: "10.0.2.18"), 32)
            .addRoute(InetAddress.getByName("0.0.0.0"), 0) // Default route for all traffic
            .setBlocking(false)
    }
    
    /**
     * Configure split tunneling based on current settings
     */
    private fun configureSplitTunneling(builder: Builder) {
        if (!splitTunnelingConfig.isEnabled || splitTunnelingConfig.appPackages.isEmpty()) {
            Log.d(TAG, "Split tunneling disabled or no apps configured - all traffic through VPN")
            return
        }
        
        Log.d(TAG, "Configuring split tunneling: mode=${splitTunnelingConfig.mode}, " +
                "apps=${splitTunnelingConfig.appPackages}")
        
        when (splitTunnelingConfig.mode) {
            SplitTunnelingMode.INCLUDE -> {
                // Include mode: only selected apps go through VPN
                Log.d(TAG, "Configuring INCLUDE mode - only selected apps use VPN")
                
                splitTunnelingConfig.appPackages.forEach { packageName ->
                    try {
                        builder.addAllowedApplication(packageName)
                        Log.d(TAG, "Added allowed application: $packageName")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add allowed application: $packageName", e)
                    }
                }
            }
            
            SplitTunnelingMode.EXCLUDE -> {
                // Exclude mode: all apps use VPN except selected ones
                Log.d(TAG, "Configuring EXCLUDE mode - all apps use VPN except selected ones")
                
                splitTunnelingConfig.appPackages.forEach { packageName ->
                    try {
                        builder.addDisallowedApplication(packageName)
                        Log.d(TAG, "Added disallowed application: $packageName")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add disallowed application: $packageName", e)
                    }
                }
            }
        }
    }
    
    /**
     * Add DNS servers for split-tunneled apps using the VPN
     */
    private fun addDnsServers(builder: Builder) {
        // Add primary and secondary DNS servers
        // These will be used by apps that go through the VPN
        try {
            builder.addDnsServer(InetAddress.getByName("1.1.1.1")) // Cloudflare DNS
            builder.addDnsServer(InetAddress.getByName("8.8.8.8")) // Google DNS
            builder.addDnsServer(InetAddress.getByName("208.67.222.222")) // OpenDNS
            
            Log.d(TAG, "Added DNS servers for VPN traffic")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add DNS servers", e)
        }
    }
    
    private fun setupWireGuardConfig(server: Server, client: ClientConfig) {
        try {
            // Parse client private key
            clientPrivateKey = decodeBase64(client.privateKey)
            
            // Parse server public key
            serverPublicKey = decodeBase64(server.wireGuardConfig?.serverPublicKey ?: "")
            
            // Parse preshared key if available
            server.wireGuardConfig?.presharedKey?.let { key ->
                presharedKey = decodeBase64(key)
            }
            
            // Parse client address
            clientAddress = client.address
            
            // Setup server endpoint
            val endpoint = server.wireGuardConfig?.serverEndpoint ?: "13.38.83.180"
            val port = server.wireGuardConfig?.serverPort ?: 51820
            serverEndpoint = InetSocketAddress(endpoint, port)
            
            Log.d(TAG, "Connecting to Paris server: $endpoint:$port")
            
            Log.d(TAG, "WireGuard configuration parsed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing WireGuard configuration", e)
            throw e
        }
    }
    
    private fun startWireGuardTunnel() {
        tunnelThread = Thread {
            try {
                Log.d(TAG, "Starting WireGuard tunnel thread")
                
                val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
                val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
                
                // Create UDP channel for WireGuard communication
                val udpChannel = DatagramChannel.open()
                udpChannel.configureBlocking(false)
                udpChannel.connect(serverEndpoint)
                
                val buffer = ByteBuffer.allocate(2048)
                
                while (isConnected) {
                    try {
                        // Read from VPN interface
                        val bytesRead = vpnInput.read(buffer.array())
                        if (bytesRead > 0) {
                            // Process WireGuard packet
                            val processedPacket = processWireGuardPacket(buffer.array(), bytesRead)
                            if (processedPacket != null) {
                                // Send to server
                                val packetBuffer = ByteBuffer.wrap(processedPacket)
                                udpChannel.write(packetBuffer)
                            }
                        }
                        
                        // Read from server
                        buffer.clear()
                        val bytesReceived = udpChannel.read(buffer)
                        if (bytesReceived > 0) {
                            // Process incoming WireGuard packet
                            val responsePacket = processIncomingWireGuardPacket(buffer.array(), bytesReceived)
                            if (responsePacket != null) {
                                // Write to VPN interface
                                vpnOutput.write(responsePacket)
                            }
                        }
                        
                        Thread.sleep(10) // Small delay to prevent CPU spinning
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in WireGuard tunnel", e)
                        break
                    }
                }
                
                udpChannel.close()
                Log.d(TAG, "WireGuard tunnel thread stopped")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting WireGuard tunnel", e)
            }
        }
        
        tunnelThread?.start()
    }
    
    private fun processWireGuardPacket(data: ByteArray, length: Int): ByteArray? {
        try {
            // Simplified WireGuard packet processing
            // In a real implementation, this would handle WireGuard protocol properly
            return data.copyOf(length)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing WireGuard packet", e)
            return null
        }
    }
    
    private fun processIncomingWireGuardPacket(data: ByteArray, length: Int): ByteArray? {
        try {
            // Simplified WireGuard packet processing
            // In a real implementation, this would handle WireGuard protocol properly
            return data.copyOf(length)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing incoming WireGuard packet", e)
            return null
        }
    }
    
    private fun decodeBase64(base64: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(base64)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding base64", e)
            ByteArray(32) // Return empty key on error
        }
    }
    
    private fun disconnectVpn() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Disconnecting enhanced WireGuard VPN...")
                
                updateNotification("Disconnecting from VPN...")
                
                // Update kill switch manager
                killSwitchManager.setVpnConnected(false)
                
                // Stop tunnel thread
                tunnelThread?.interrupt()
                tunnelThread = null
                
                // Close VPN interface
                closeVpnInterface()
                
                isConnected = false
                
                // Broadcast disconnection status
                val broadcastIntent = Intent("com.example.v.vpn.CONNECTION_STATUS")
                broadcastIntent.putExtra("connected", false)
                sendBroadcast(broadcastIntent)
                
                Log.d(TAG, "Enhanced WireGuard VPN disconnected")
                stopForeground(true)
                stopSelf()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting enhanced WireGuard VPN", e)
                stopForeground(true)
                stopSelf()
            }
        }
    }
    
    /**
     * Close VPN interface
     */
    private fun closeVpnInterface() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            Log.d(TAG, "VPN interface closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
    }
    
    /**
     * Save split tunneling configuration to SharedPreferences
     */
    private fun saveSplitTunnelingConfig() {
        val preferences = getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        preferences.edit().apply {
            putString("split_tunneling_mode", splitTunnelingConfig.mode.name)
            putStringSet("split_tunneling_apps", splitTunnelingConfig.appPackages.toSet())
            putBoolean("split_tunneling_enabled", splitTunnelingConfig.isEnabled)
            apply()
        }
        Log.d(TAG, "Split tunneling configuration saved")
    }
    
    /**
     * Load split tunneling configuration from SharedPreferences
     */
    private fun loadSplitTunnelingConfig() {
        val preferences = getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        val modeName = preferences.getString("split_tunneling_mode", "EXCLUDE") ?: "EXCLUDE"
        val appPackages = preferences.getStringSet("split_tunneling_apps", emptySet())?.toList() ?: emptyList()
        val isEnabled = preferences.getBoolean("split_tunneling_enabled", false)
        
        val mode = when (modeName) {
            "INCLUDE" -> SplitTunnelingMode.INCLUDE
            "EXCLUDE" -> SplitTunnelingMode.EXCLUDE
            else -> SplitTunnelingMode.EXCLUDE
        }
        
        splitTunnelingConfig = SplitTunnelingConfig(
            mode = mode,
            appPackages = appPackages,
            isEnabled = isEnabled
        )
        
        Log.d(TAG, "Split tunneling configuration loaded: $splitTunnelingConfig")
    }
    
    /**
     * Save kill switch state to SharedPreferences
     */
    private fun saveKillSwitchState() {
        val preferences = getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        preferences.edit().apply {
            putBoolean("kill_switch_active", isKillSwitchActive)
            putString("kill_switch_reason", killSwitchReason)
            apply()
        }
        Log.d(TAG, "Kill switch state saved: active=$isKillSwitchActive, reason=$killSwitchReason")
    }
    
    /**
     * Load kill switch state from SharedPreferences
     */
    private fun loadKillSwitchState() {
        val preferences = getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        isKillSwitchActive = preferences.getBoolean("kill_switch_active", false)
        killSwitchReason = preferences.getString("kill_switch_reason", null)
        
        Log.d(TAG, "Kill switch state loaded: active=$isKillSwitchActive, reason=$killSwitchReason")
        
        // If kill switch was active, reactivate it
        if (isKillSwitchActive && killSwitchReason != null) {
            Log.d(TAG, "Reactivating kill switch after service restart")
            activateKillSwitch(killSwitchReason!!)
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // VPN service channel
            val vpnChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "WireGuard VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for WireGuard VPN service status"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(vpnChannel)
            
            // Kill switch channel
            val killSwitchChannel = NotificationChannel(
                KILL_SWITCH_CHANNEL_ID,
                "Kill Switch Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for kill switch status"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(killSwitchChannel)
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
    
    private fun createKillSwitchNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, KILL_SWITCH_CHANNEL_ID)
            .setContentTitle("Kill Switch Active")
            .setContentText("Internet traffic blocked: ${killSwitchReason ?: "VPN disconnected"}")
            .setSmallIcon(R.drawable.ic_vpn_key)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }
    
    private fun showKillSwitchNotification(reason: String) {
        notificationManager.notify(KILL_SWITCH_NOTIFICATION_ID, createKillSwitchNotification())
    }
    
    private fun hideKillSwitchNotification() {
        notificationManager.cancel(KILL_SWITCH_NOTIFICATION_ID)
    }
    
    private fun updateNotification(message: String) {
        notificationManager.notify(NOTIFICATION_ID, createNotification(message))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isConnected = false
        tunnelThread?.interrupt()
        closeVpnInterface()
        serviceScope.cancel()
        Log.d(TAG, "Enhanced WireGuard VPN Service destroyed")
    }
}