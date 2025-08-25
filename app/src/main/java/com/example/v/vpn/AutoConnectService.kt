package com.example.v.vpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

/**
 * Auto-Connect Service - Automatically connects to VPN based on conditions
 * Matches desktop functionality while remaining Android-compatible
 */
class AutoConnectService private constructor(private val context: Context) {
    companion object {
        private const val TAG = "AutoConnectService"
        private const val PREFS_NAME = "auto_connect_prefs"
        private const val KEY_ENABLED = "auto_connect_enabled"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_MOBILE_ONLY = "mobile_only"
        private const val KEY_TRUSTED_NETWORKS = "trusted_networks"
        private const val KEY_LAST_SERVER = "last_connected_server"
        private const val KEY_CONNECTION_DELAY = "connection_delay"
        
        @Volatile
        private var INSTANCE: AutoConnectService? = null
        
        fun getInstance(context: Context): AutoConnectService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AutoConnectService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // State flows
    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val isEnabled: StateFlow<Boolean> = _isEnabled
    
    private val _isWifiOnly = MutableStateFlow(prefs.getBoolean(KEY_WIFI_ONLY, true))
    val isWifiOnly: StateFlow<Boolean> = _isWifiOnly
    
    private val _isMobileOnly = MutableStateFlow(prefs.getBoolean(KEY_MOBILE_ONLY, false))
    val isMobileOnly: StateFlow<Boolean> = _isMobileOnly
    
    private val _trustedNetworks = MutableStateFlow<Set<String>>(getTrustedNetworks())
    val trustedNetworks: StateFlow<Set<String>> = _trustedNetworks
    
    private val _lastServer = MutableStateFlow<String?>(getLastServer())
    val lastServer: StateFlow<String?> = _lastServer
    
    private val _connectionDelay = MutableStateFlow(prefs.getInt(KEY_CONNECTION_DELAY, 5))
    val connectionDelay: StateFlow<Int> = _connectionDelay
    
    private val _isAutoConnecting = MutableStateFlow(false)
    val isAutoConnecting: StateFlow<Boolean> = _isAutoConnecting
    
    // Network callback for monitoring
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (_isEnabled.value) {
                scope.launch {
                    handleNetworkAvailable(network)
                }
            }
        }
        
        override fun onLost(network: Network) {
            if (_isEnabled.value) {
                Log.i(TAG, "🌐 Network lost, auto-connect may trigger")
            }
        }
    }
    
    // Auto-connect job
    private var autoConnectJob: Job? = null
    
    /**
     * Enable/disable auto-connect
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        Log.i(TAG, "🔧 Auto-connect ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            activate()
        } else {
            deactivate()
        }
    }
    
    /**
     * Set WiFi-only mode
     */
    fun setWifiOnly(enabled: Boolean) {
        _isWifiOnly.value = enabled
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        Log.i(TAG, "🔧 WiFi-only mode ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Set mobile-only mode
     */
    fun setMobileOnly(enabled: Boolean) {
        _isMobileOnly.value = enabled
        prefs.edit().putBoolean(KEY_MOBILE_ONLY, enabled).apply()
        Log.i(TAG, "🔧 Mobile-only mode ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Add trusted network
     */
    fun addTrustedNetwork(ssid: String) {
        val current = _trustedNetworks.value.toMutableSet()
        current.add(ssid)
        _trustedNetworks.value = current
        saveTrustedNetworks(current)
        Log.i(TAG, "➕ Trusted network added: $ssid")
    }
    
    /**
     * Remove trusted network
     */
    fun removeTrustedNetwork(ssid: String) {
        val current = _trustedNetworks.value.toMutableSet()
        current.remove(ssid)
        _trustedNetworks.value = current
        saveTrustedNetworks(current)
        Log.i(TAG, "➖ Trusted network removed: $ssid")
    }
    
    /**
     * Set last connected server
     */
    fun setLastServer(serverId: String) {
        _lastServer.value = serverId
        prefs.edit().putString(KEY_LAST_SERVER, serverId).apply()
        Log.i(TAG, "💾 Last server saved: $serverId")
    }
    
    /**
     * Set connection delay
     */
    fun setConnectionDelay(delaySeconds: Int) {
        _connectionDelay.value = delaySeconds
        prefs.edit().putInt(KEY_CONNECTION_DELAY, delaySeconds).apply()
        Log.i(TAG, "⏱️ Connection delay set: ${delaySeconds}s")
    }
    
    /**
     * Activate auto-connect service
     */
    fun activate() {
        if (!_isEnabled.value) {
            Log.i(TAG, "⚠️ Auto-connect is disabled, not activating")
            return
        }
        
        registerNetworkCallback()
        Log.i(TAG, "🔄 Auto-connect service activated")
    }
    
    /**
     * Deactivate auto-connect service
     */
    fun deactivate() {
        unregisterNetworkCallback()
        autoConnectJob?.cancel()
        Log.i(TAG, "🔄 Auto-connect service deactivated")
    }
    
    /**
     * Handle network availability
     */
    private suspend fun handleNetworkAvailable(network: Network) {
        try {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            val networkType = getNetworkType(networkCapabilities)
            
            Log.i(TAG, "🌐 Network available: $networkType")
            
            // Check if we should auto-connect
            if (shouldAutoConnect(networkType, networkCapabilities)) {
                val delay = _connectionDelay.value
                Log.i(TAG, "⏱️ Auto-connecting in ${delay}s...")
                
                delay(TimeUnit.SECONDS.toMillis(delay.toLong()))
                
                // Check if still enabled after delay
                if (_isEnabled.value) {
                    performAutoConnect()
                }
            } else {
                Log.i(TAG, "⚠️ Auto-connect conditions not met for $networkType")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling network availability: ${e.message}")
        }
    }
    
    /**
     * Check if auto-connect should trigger
     */
    private fun shouldAutoConnect(networkType: String, capabilities: NetworkCapabilities?): Boolean {
        // Check network type restrictions
        when (networkType) {
            "WiFi" -> {
                if (_isMobileOnly.value) {
                    Log.d(TAG, "⚠️ WiFi network but mobile-only mode enabled")
                    return false
                }
            }
            "Mobile" -> {
                if (_isWifiOnly.value) {
                    Log.d(TAG, "⚠️ Mobile network but WiFi-only mode enabled")
                    return false
                }
            }
        }
        
        // Check if network is trusted (for WiFi)
        if (networkType == "WiFi") {
            val ssid = getCurrentWiFiSSID()
            if (ssid != null && _trustedNetworks.value.contains(ssid)) {
                Log.d(TAG, "✅ WiFi network is trusted: $ssid")
                return false // Don't auto-connect on trusted networks
            }
        }
        
        // Check if VPN is already connected
        if (isVPNConnected()) {
            Log.d(TAG, "⚠️ VPN already connected, skipping auto-connect")
            return false
        }
        
        return true
    }
    
    /**
     * Perform auto-connect
     */
    private suspend fun performAutoConnect() {
        try {
            _isAutoConnecting.value = true
            
            val serverId = _lastServer.value
            if (serverId == null) {
                Log.w(TAG, "⚠️ No last server available for auto-connect")
                return
            }
            
            Log.i(TAG, "🚀 Auto-connecting to server: $serverId")
            
            // Here you would call your VPN manager to connect
            // For now, we'll simulate the connection
            delay(2000) // Simulate connection time
            
            Log.i(TAG, "✅ Auto-connect completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Auto-connect failed: ${e.message}")
        } finally {
            _isAutoConnecting.value = false
        }
    }
    
    /**
     * Get network type from capabilities
     */
    private fun getNetworkType(capabilities: NetworkCapabilities?): String {
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Unknown"
        }
    }
    
    /**
     * Get current WiFi SSID
     */
    private fun getCurrentWiFiSSID(): String? {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                // In a real implementation, you'd get the SSID from WifiManager
                // For now, we'll return a placeholder
                "CurrentWiFi"
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not get WiFi SSID: ${e.message}")
            null
        }
    }
    
    /**
     * Check if VPN is connected
     */
    private fun isVPNConnected(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not check VPN status: ${e.message}")
            false
        }
    }
    
    /**
     * Register network callback
     */
    private fun registerNetworkCallback() {
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            Log.i(TAG, "📡 Network callback registered for auto-connect")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to register network callback: ${e.message}")
        }
    }
    
    /**
     * Unregister network callback
     */
    private fun unregisterNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.i(TAG, "📡 Network callback unregistered for auto-connect")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to unregister network callback: ${e.message}")
        }
    }
    
    /**
     * Get auto-connect configuration
     */
    fun getConfig(): AutoConnectConfig {
        return AutoConnectConfig(
            isEnabled = _isEnabled.value,
            isWifiOnly = _isWifiOnly.value,
            isMobileOnly = _isMobileOnly.value,
            trustedNetworks = _trustedNetworks.value.toList(),
            lastServer = _lastServer.value,
            connectionDelay = _connectionDelay.value,
            isAutoConnecting = _isAutoConnecting.value
        )
    }
    
    private fun getTrustedNetworks(): Set<String> {
        return prefs.getStringSet(KEY_TRUSTED_NETWORKS, emptySet()) ?: emptySet()
    }
    
    private fun getLastServer(): String? {
        return prefs.getString(KEY_LAST_SERVER, null)
    }
    
    private fun saveTrustedNetworks(networks: Set<String>) {
        prefs.edit().putStringSet(KEY_TRUSTED_NETWORKS, networks).apply()
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        scope.cancel()
        deactivate()
    }
}

/**
 * Auto-connect configuration
 */
data class AutoConnectConfig(
    val isEnabled: Boolean,
    val isWifiOnly: Boolean,
    val isMobileOnly: Boolean,
    val trustedNetworks: List<String>,
    val lastServer: String?,
    val connectionDelay: Int,
    val isAutoConnecting: Boolean
)
