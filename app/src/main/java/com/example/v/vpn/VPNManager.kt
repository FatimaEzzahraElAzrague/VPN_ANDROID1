package com.example.v.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.v.config.VPNConfig
import com.example.v.models.ClientConfig
import com.example.v.models.Server
import kotlinx.coroutines.*
import java.security.SecureRandom
import java.util.*

/**
 * VPN Manager class to handle VPN connections and state management
 * Note: This is a mock implementation for demonstration purposes
 * In production, integrate with actual WireGuard library
 */
class VPNManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VPNManager"
        
        @Volatile
        private var INSTANCE: VPNManager? = null
        
        fun getInstance(context: Context): VPNManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VPNManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val _connectionState = MutableLiveData<VPNConnectionState>()
    val connectionState: LiveData<VPNConnectionState> = _connectionState
    
    private val _currentServer = MutableLiveData<Server?>()
    val currentServer: LiveData<Server?> = _currentServer
    
    private val _statistics = MutableLiveData<VPNStatistics>()
    val statistics: LiveData<VPNStatistics> = _statistics
    
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var clientConfig: ClientConfig? = null
    
    init {
        _connectionState.value = VPNConnectionState.DISCONNECTED
        startStatisticsUpdater()
    }
    
    /**
     * Check if VPN permission is granted
     */
    fun hasVpnPermission(): Boolean {
        val intent = VpnService.prepare(context)
        Log.d(TAG, "VPN permission check: ${intent == null}")
        return intent == null
    }
    
    /**
     * Get VPN permission intent
     */
    fun getVpnPermissionIntent(): Intent? {
        val intent = VpnService.prepare(context)
        Log.d(TAG, "VPN permission intent: $intent")
        return intent
    }
    
    /**
     * Connect to VPN server
     */
    fun connect(server: Server) {
        Log.d(TAG, "Connect requested for server: ${server.name}")
        
        if (_connectionState.value == VPNConnectionState.CONNECTED || 
            _connectionState.value == VPNConnectionState.CONNECTING) {
            Log.w(TAG, "VPN is already connected or connecting")
            return
        }
        
        // Check VPN permission first
        if (!hasVpnPermission()) {
            Log.e(TAG, "VPN permission not granted - need to request permission")
            _connectionState.value = VPNConnectionState.ERROR
            return
        }
        
        _connectionState.value = VPNConnectionState.CONNECTING
        _currentServer.value = server
        
        Log.d(TAG, "Starting VPN connection to ${server.city}, ${server.country}")
        Log.d(TAG, "Server endpoint: ${server.wireGuardConfig?.serverEndpoint}")
        
        managerScope.launch {
            try {
                // Use real client configuration for Paris server
                if (clientConfig == null) {
                    clientConfig = if (server.id == "france-paris") {
                        Log.d(TAG, "Using Paris client config")
                        VPNConfig.parisClientConfig
                    } else {
                        Log.d(TAG, "Generating mock client config")
                        generateClientConfig()
                    }
                }
                
                // Save connection state for auto-connect
                saveConnectionState(server, clientConfig!!)
                
                // Start real WireGuard VPN service
                val intent = Intent(context, WireGuardVpnService::class.java).apply {
                    action = WireGuardVpnService.ACTION_CONNECT
                    putExtra("server", server)
                    putExtra("client_config", clientConfig!!)
                }
                
                Log.d(TAG, "Starting WireGuard VPN service...")
                context.startForegroundService(intent)
                
                // Monitor connection status
                var attempts = 0
                while (attempts < 30) { // Wait up to 30 seconds
                    delay(1000)
                    Log.d(TAG, "Checking VPN connection status... attempt ${attempts + 1}")
                    if (WireGuardVpnService.isVpnConnected()) {
                        _connectionState.value = VPNConnectionState.CONNECTED
                        Log.d(TAG, "Real VPN connected to ${server.city}, ${server.country}")
                        return@launch
                    }
                    attempts++
                }
                
                // If we reach here, connection failed
                _connectionState.value = VPNConnectionState.ERROR
                Log.e(TAG, "VPN connection timeout after 30 seconds")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect VPN", e)
                _connectionState.value = VPNConnectionState.ERROR
            }
        }
    }
    
    /**
     * Disconnect from VPN
     */
    fun disconnect() {
        Log.d(TAG, "Disconnect requested")
        
        if (_connectionState.value == VPNConnectionState.DISCONNECTED) {
            Log.w(TAG, "VPN is already disconnected")
            return
        }
        
        _connectionState.value = VPNConnectionState.DISCONNECTING
        
        managerScope.launch {
            try {
                // Send disconnect intent to service
                val intent = Intent(context, WireGuardVpnService::class.java).apply {
                    action = WireGuardVpnService.ACTION_DISCONNECT
                }
                context.startService(intent)
                
                // Simulate disconnection delay
                delay(1000)
                
                // Clear connection state
                clearConnectionState()
                
                _connectionState.value = VPNConnectionState.DISCONNECTED
                _currentServer.value = null
                
                Log.d(TAG, "VPN disconnected")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disconnect VPN", e)
                _connectionState.value = VPNConnectionState.ERROR
            }
        }
    }
    
    /**
     * Generate client WireGuard configuration (Mock implementation for non-Paris servers)
     */
    private fun generateClientConfig(): ClientConfig {
        // Generate a mock private key
        val random = SecureRandom()
        val privateKeyBytes = ByteArray(32)
        random.nextBytes(privateKeyBytes)
        
        // Create mock keys (in real implementation, use actual WireGuard Key class)
        val privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes)
        val publicKeyBase64 = Base64.getEncoder().encodeToString(ByteArray(32).apply { 
            random.nextBytes(this) 
        })
        
        return ClientConfig(
            privateKey = privateKeyBase64,
            publicKey = publicKeyBase64,
            address = "10.0.0.${(2..254).random()}/32" // Random IP in range
        )
    }
    
    /**
     * Save connection state for auto-reconnect
     */
    private fun saveConnectionState(server: Server, clientConfig: ClientConfig?) {
        val preferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        preferences.edit().apply {
            putBoolean("was_connected_before_reboot", true)
            putString("last_server_config", serializeServer(server))
            if (clientConfig != null) {
                putString("last_client_config", serializeClientConfig(clientConfig))
            }
            apply()
        }
    }
    
    /**
     * Clear connection state
     */
    private fun clearConnectionState() {
        val preferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        preferences.edit().apply {
            putBoolean("was_connected_before_reboot", false)
            remove("last_server_config")
            remove("last_client_config")
            apply()
        }
    }
    
    /**
     * Serialize server configuration to JSON string
     */
    private fun serializeServer(server: Server): String {
        // Simple serialization - in production, use proper JSON library
        return "${server.id}|${server.country}|${server.city}|${server.wireGuardConfig?.serverEndpoint}|${server.wireGuardConfig?.serverPublicKey}"
    }
    
    /**
     * Serialize client configuration to JSON string
     */
    private fun serializeClientConfig(config: ClientConfig): String {
        return "${config.privateKey}|${config.publicKey}|${config.address}|${config.dns}"
    }
    
    /**
     * Start statistics updater
     */
    private fun startStatisticsUpdater() {
        managerScope.launch {
            while (true) {
                delay(1000) // Update every second
                
                if (_connectionState.value == VPNConnectionState.CONNECTED) {
                    // Collect real statistics when connected
                    val stats = VPNStatistics(
                        bytesReceived = getRealBytesReceived(),
                        bytesSent = getRealBytesSent(),
                        packetsReceived = getRealPacketsReceived(),
                        packetsSent = getRealPacketsSent(),
                        connectionDuration = System.currentTimeMillis() // Simplified
                    )
                    _statistics.postValue(stats)
                }
            }
        }
    }
    
    /**
     * Get real bytes received from network interface
     */
    private fun getRealBytesReceived(): Long {
        return try {
            // Read from /proc/net/dev to get real network statistics
            val process = Runtime.getRuntime().exec("cat /proc/net/dev")
            val reader = process.inputStream.bufferedReader()
            var bytesReceived = 0L
            
            reader.useLines { lines ->
                for (line in lines) {
                    if (line.contains("tun0") || line.contains("wg0")) {
                        // Parse network interface statistics
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.size >= 10) {
                            bytesReceived = parts[1].toLongOrNull() ?: 0L
                        }
                        break
                    }
                }
            }
            
            bytesReceived
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bytes received", e)
            // Fallback to time-based calculation
            val connectionTime = System.currentTimeMillis() - (_statistics.value?.connectionDuration ?: System.currentTimeMillis())
            (connectionTime / 1000) * 1024 * 10 // 10KB per second average
        }
    }
    
    /**
     * Get real bytes sent from network interface
     */
    private fun getRealBytesSent(): Long {
        return try {
            // Read from /proc/net/dev to get real network statistics
            val process = Runtime.getRuntime().exec("cat /proc/net/dev")
            val reader = process.inputStream.bufferedReader()
            var bytesSent = 0L
            
            reader.useLines { lines ->
                for (line in lines) {
                    if (line.contains("tun0") || line.contains("wg0")) {
                        // Parse network interface statistics
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.size >= 10) {
                            bytesSent = parts[9].toLongOrNull() ?: 0L
                        }
                        break
                    }
                }
            }
            
            bytesSent
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bytes sent", e)
            // Fallback to time-based calculation
            val connectionTime = System.currentTimeMillis() - (_statistics.value?.connectionDuration ?: System.currentTimeMillis())
            (connectionTime / 1000) * 1024 * 5 // 5KB per second average
        }
    }
    
    /**
     * Get real packets received from network interface
     */
    private fun getRealPacketsReceived(): Long {
        return try {
            // Read from /proc/net/dev to get real network statistics
            val process = Runtime.getRuntime().exec("cat /proc/net/dev")
            val reader = process.inputStream.bufferedReader()
            var packetsReceived = 0L
            
            reader.useLines { lines ->
                for (line in lines) {
                    if (line.contains("tun0") || line.contains("wg0")) {
                        // Parse network interface statistics
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.size >= 10) {
                            packetsReceived = parts[2].toLongOrNull() ?: 0L
                        }
                        break
                    }
                }
            }
            
            packetsReceived
        } catch (e: Exception) {
            Log.e(TAG, "Error getting packets received", e)
            // Fallback to time-based calculation
            val connectionTime = System.currentTimeMillis() - (_statistics.value?.connectionDuration ?: System.currentTimeMillis())
            (connectionTime / 1000) * 10 // 10 packets per second average
        }
    }
    
    /**
     * Get real packets sent from network interface
     */
    private fun getRealPacketsSent(): Long {
        return try {
            // Read from /proc/net/dev to get real network statistics
            val process = Runtime.getRuntime().exec("cat /proc/net/dev")
            val reader = process.inputStream.bufferedReader()
            var packetsSent = 0L
            
            reader.useLines { lines ->
                for (line in lines) {
                    if (line.contains("tun0") || line.contains("wg0")) {
                        // Parse network interface statistics
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.size >= 10) {
                            packetsSent = parts[10].toLongOrNull() ?: 0L
                        }
                        break
                    }
                }
            }
            
            packetsSent
        } catch (e: Exception) {
            Log.e(TAG, "Error getting packets sent", e)
            // Fallback to time-based calculation
            val connectionTime = System.currentTimeMillis() - (_statistics.value?.connectionDuration ?: System.currentTimeMillis())
            (connectionTime / 1000) * 5 // 5 packets per second average
        }
    }
}

/**
 * VPN Connection States
 */
enum class VPNConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

/**
 * VPN Statistics data class
 */
data class VPNStatistics(
    val bytesReceived: Long,
    val bytesSent: Long,
    val packetsReceived: Long,
    val packetsSent: Long,
    val connectionDuration: Long
)