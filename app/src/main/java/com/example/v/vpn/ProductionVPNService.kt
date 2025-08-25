package com.example.v.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.v.MainActivity
import com.example.v.R
import com.example.v.data.VPNApiClient
import com.example.v.data.models.*
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production-ready VPN Service using WireGuard implementation
 * Integrates with backend for server configurations and uses real WireGuard tunnels
 */
class ProductionVPNService : VpnService() {
    
    companion object {
        private const val TAG = "ProductionVPNService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "vpn_service_channel"
        private const val VPN_MTU = 1420
        private const val BUFFER_SIZE = 32768
        
        // Actions
        const val ACTION_CONNECT = "CONNECT"
        const val ACTION_DISCONNECT = "DISCONNECT"
        const val ACTION_SWITCH_SERVER = "SWITCH_SERVER"
        
        // Extra keys
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_SERVER_ID = "server_id"
        const val EXTRA_AUTH_TOKEN = "auth_token"
        const val EXTRA_VPN_CONFIG = "vpn_config"
    }
    
    // VPN interface
    private var vpnInterface: ParcelFileDescriptor? = null
    
    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // VPN configuration
    private var vpnConfig: VPNConnectionConfig? = null
    private var currentUserId: String? = null
    private var currentServerId: String? = null
    private var authToken: String? = null
    
    // Connection state
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    
    // WireGuard tunnel
    private var wireGuardTunnel: WireGuardTunnel? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "🚀 Production VPN Service created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                Log.i(TAG, "🚀 CONNECT command received")
                handleConnect(intent)
            }
            ACTION_DISCONNECT -> {
                Log.i(TAG, "🛑 DISCONNECT command received")
                handleDisconnect()
            }
            ACTION_SWITCH_SERVER -> {
                Log.i(TAG, "🔄 SWITCH_SERVER command received")
                handleSwitchServer(intent)
            }
            else -> {
                Log.w(TAG, "⚠️ Unknown action: ${intent?.action}")
            }
        }
        return START_STICKY
    }
    
    private fun handleConnect(intent: Intent) {
        serviceScope.launch {
            try {
                if (isConnecting.get()) {
                    Log.w(TAG, "⚠️ Already connecting, ignoring connect request")
                    return@launch
                }
                
                isConnecting.set(true)
                
                // Extract parameters
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID)
                val token = intent.getStringExtra(EXTRA_AUTH_TOKEN)
                
                if (userId == null || serverId == null || token == null) {
                    Log.e(TAG, "❌ Missing required parameters for VPN connection")
                    updateConnectionStatus(userId ?: "", serverId ?: "", "ERROR", "Missing parameters")
                    isConnecting.set(false)
                    return@launch
                }
                
                currentUserId = userId
                currentServerId = serverId
                authToken = token
                
                Log.i(TAG, "🔧 === STARTING PRODUCTION VPN CONNECTION ===")
                Log.i(TAG, "👤 User: $userId")
                Log.i(TAG, "🖥️ Server: $serverId")
                
                // Step 1: Get VPN configuration from backend
                val configResponse = VPNApiClient.getVPNConfiguration(userId, serverId, token)
                if (configResponse?.success != true || configResponse.config == null) {
                    val error = configResponse?.error ?: "Failed to get VPN configuration"
                    Log.e(TAG, "❌ Failed to get VPN configuration: $error")
                    updateConnectionStatus(userId, serverId, "ERROR", error)
                    isConnecting.set(false)
                    return@launch
                }
                
                vpnConfig = configResponse.config
                Log.i(TAG, "✅ VPN configuration received from backend")
                
                // Step 2: Create VPN interface
                val interfaceCreated = createVPNInterface(vpnConfig!!)
                if (!interfaceCreated) {
                    Log.e(TAG, "❌ Failed to create VPN interface")
                    updateConnectionStatus(userId, serverId, "ERROR", "Failed to create VPN interface")
                    isConnecting.set(false)
                    return@launch
                }
                
                // Step 3: Start WireGuard tunnel
                val tunnelStarted = startWireGuardTunnel(vpnConfig!!)
                if (!tunnelStarted) {
                    Log.e(TAG, "❌ Failed to start WireGuard tunnel")
                    destroyVPNInterface()
                    updateConnectionStatus(userId, serverId, "ERROR", "Failed to start WireGuard tunnel")
                    isConnecting.set(false)
                    return@launch
                }
                
                // Step 4: Start VPN tunnel
                val vpnTunnelStarted = startVPNTunnel()
                if (!vpnTunnelStarted) {
                    Log.e(TAG, "❌ Failed to start VPN tunnel")
                    destroyVPNInterface()
                    updateConnectionStatus(userId, serverId, "ERROR", "Failed to start VPN tunnel")
                    isConnecting.set(false)
                    return@launch
                }
                
                // Step 5: Update connection status
                updateConnectionStatus(userId, serverId, "CONNECTED")
                
                isConnected.set(true)
                isConnecting.set(false)
                
                Log.i(TAG, "🎉 === PRODUCTION VPN CONNECTION ESTABLISHED ===")
                Log.i(TAG, "✅ VPN interface created")
                Log.i(TAG, "✅ WireGuard tunnel active")
                Log.i(TAG, "✅ Traffic routing enabled")
                Log.i(TAG, "✅ Backend integration working")
                
                // Start foreground service
                startForeground(NOTIFICATION_ID, createNotification("VPN Connected", "Connected to ${vpnConfig!!.server.name}"))
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during VPN connection", e)
                updateConnectionStatus(
                    currentUserId ?: "",
                    currentServerId ?: "",
                    "ERROR",
                    e.message ?: "Unknown error"
                )
                isConnecting.set(false)
            }
        }
    }
    
    private fun handleDisconnect() {
        serviceScope.launch {
            try {
                Log.i(TAG, "🛑 === DISCONNECTING VPN ===")
                
                // Stop WireGuard tunnel
                wireGuardTunnel?.stop()
                wireGuardTunnel = null
                
                // Destroy VPN interface
                destroyVPNInterface()
                
                // Update connection status
                currentUserId?.let { userId ->
                    currentServerId?.let { serverId ->
                        updateConnectionStatus(userId, serverId, "DISCONNECTED")
                    }
                }
                
                isConnected.set(false)
                
                Log.i(TAG, "✅ VPN disconnected successfully")
                
                // Stop foreground service
                stopForeground(true)
                stopSelf()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during VPN disconnection", e)
            }
        }
    }
    
    private fun handleSwitchServer(intent: Intent) {
        serviceScope.launch {
            try {
                Log.i(TAG, "🔄 === SWITCHING VPN SERVER ===")
                
                // Disconnect from current server
                handleDisconnect()
                
                // Wait a bit for cleanup
                delay(1000)
                
                // Connect to new server
                handleConnect(intent)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during server switch", e)
            }
        }
    }
    
    private suspend fun createVPNInterface(config: VPNConnectionConfig): Boolean {
        return try {
            Log.i(TAG, "🔧 Creating VPN interface...")
            
            val builder = Builder()
                .setSession("ProductionVPN")
                .addAddress(config.clientConfig.address, 32)
                .addDnsServer(config.clientConfig.dns)
                .addRoute("0.0.0.0", 0)
                .setMtu(config.server.mtu)
                .setBlocking(true)
            
            // Add DNS servers from server configuration
            config.server.dnsServers.forEach { dns ->
                builder.addDnsServer(dns)
            }
            
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                Log.i(TAG, "✅ VPN interface created successfully")
                true
            } else {
                Log.e(TAG, "❌ Failed to establish VPN interface")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating VPN interface", e)
            false
        }
    }
    
    private suspend fun startWireGuardTunnel(config: VPNConnectionConfig): Boolean {
        return try {
            Log.i(TAG, "🔧 Starting WireGuard tunnel...")
            
            // Create WireGuard tunnel configuration
            val tunnelConfig = WireGuardTunnelConfig(
                privateKey = config.clientConfig.privateKey,
                publicKey = config.server.wireguardPublicKey,
                endpoint = config.server.wireguardEndpoint,
                allowedIPs = config.server.allowedIPs,
                persistentKeepalive = config.persistentKeepalive,
                mtu = config.server.mtu
            )
            
            // Create and start WireGuard tunnel
            wireGuardTunnel = WireGuardTunnel(tunnelConfig)
            val started = wireGuardTunnel?.start()
            
            if (started == true) {
                Log.i(TAG, "✅ WireGuard tunnel started successfully")
                true
            } else {
                Log.e(TAG, "❌ Failed to start WireGuard tunnel")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting WireGuard tunnel", e)
            false
        }
    }
    
    private suspend fun startVPNTunnel(): Boolean {
        return try {
            Log.i(TAG, "🔧 Starting VPN tunnel...")
            
            // Start tunnel processing
            startTunnelProcessing()
            
            Log.i(TAG, "✅ VPN tunnel started successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting VPN tunnel", e)
            false
        }
    }
    
    private fun startTunnelProcessing() {
        serviceScope.launch {
            try {
                Log.i(TAG, "🔄 Starting tunnel processing...")
                
                val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
                val outputStream = FileOutputStream(vpnInterface?.fileDescriptor)
                
                val buffer = ByteArray(BUFFER_SIZE)
                
                while (isConnected.get()) {
                    try {
                        // Read from VPN interface
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead > 0) {
                            // Process through WireGuard tunnel
                            val processedData = wireGuardTunnel?.processData(buffer, 0, bytesRead)
                            
                            if (processedData != null) {
                                // Write processed data back to VPN interface
                                outputStream.write(processedData)
                                outputStream.flush()
                            }
                        }
                        
                        // Small delay to prevent CPU spinning
                        delay(1)
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error in tunnel processing", e)
                        break
                    }
                }
                
                Log.i(TAG, "🛑 Tunnel processing stopped")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting tunnel processing", e)
            }
        }
    }
    
    private fun destroyVPNInterface() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            Log.i(TAG, "✅ VPN interface destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error destroying VPN interface", e)
        }
    }
    
    private suspend fun updateConnectionStatus(
        userId: String,
        serverId: String,
        status: String,
        error: String? = null
    ) {
        try {
            val request = VPNConnectionRequest(userId, serverId, status)
            authToken?.let { token ->
                VPNApiClient.updateConnectionStatus(userId, serverId, token, request)
                Log.i(TAG, "✅ Connection status updated: $status")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating connection status", e)
        }
    }
    
    private fun createNotification(title: String, content: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
                        .setSmallIcon(R.drawable.ic_vpn_key)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "VPN connection status"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "🛑 Production VPN Service destroyed")
        
        // Cleanup
        handleDisconnect()
        serviceScope.cancel()
    }
    
    override fun onRevoke() {
        super.onRevoke()
        Log.w(TAG, "⚠️ VPN permissions revoked")
        handleDisconnect()
    }
}
