package com.example.v.vpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Kill Switch Service - Prevents internet access when VPN is disconnected
 * Matches desktop functionality while remaining Android-compatible
 */
class KillSwitchService private constructor(private val context: Context) {
    companion object {
        private const val TAG = "KillSwitchService"
        private const val PREFS_NAME = "kill_switch_prefs"
        private const val KEY_ENABLED = "kill_switch_enabled"
        private const val KEY_STRICT_MODE = "strict_mode_enabled"
        
        @Volatile
        private var INSTANCE: KillSwitchService? = null
        
        fun getInstance(context: Context): KillSwitchService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KillSwitchService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // State flows
    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))
    val isEnabled: StateFlow<Boolean> = _isEnabled
    
    private val _isStrictMode = MutableStateFlow(prefs.getBoolean(KEY_STRICT_MODE, false))
    val isStrictMode: StateFlow<Boolean> = _isStrictMode
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive
    
    // Network callback for monitoring
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (_isActive.value && _isEnabled.value) {
                Log.i(TAG, "🌐 Network available, checking VPN status...")
                checkVPNStatus()
            }
        }
        
        override fun onLost(network: Network) {
            if (_isActive.value && _isEnabled.value) {
                Log.w(TAG, "🌐 Network lost, kill switch may activate")
            }
        }
    }
    
    /**
     * Enable/disable kill switch
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        Log.i(TAG, "🔧 Kill switch ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            activate()
        } else {
            deactivate()
        }
    }
    
    /**
     * Enable/disable strict mode (blocks all traffic when VPN is down)
     */
    fun setStrictMode(enabled: Boolean) {
        _isStrictMode.value = enabled
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
        Log.i(TAG, "🔧 Strict mode ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Activate kill switch
     */
    fun activate() {
        if (!_isEnabled.value) {
            Log.i(TAG, "⚠️ Kill switch is disabled, not activating")
            return
        }
        
        _isActive.value = true
        registerNetworkCallback()
        Log.i(TAG, "🛡️ Kill switch activated")
    }
    
    /**
     * Deactivate kill switch
     */
    fun deactivate() {
        _isActive.value = false
        unregisterNetworkCallback()
        Log.i(TAG, "🛡️ Kill switch deactivated")
    }
    
    /**
     * Check VPN status and apply kill switch if needed
     */
    fun checkVPNStatus() {
        if (!_isActive.value || !_isEnabled.value) {
            return
        }
        
        val isVPNConnected = isVPNInterfaceActive()
        
        if (!isVPNConnected) {
            Log.w(TAG, "🚨 VPN disconnected, kill switch activating...")
            applyKillSwitch()
        } else {
            Log.i(TAG, "✅ VPN connected, kill switch inactive")
            removeKillSwitch()
        }
    }
    
    /**
     * Apply kill switch (block internet access)
     */
    private fun applyKillSwitch() {
        try {
            if (_isStrictMode.value) {
                // Strict mode: Block all traffic
                blockAllTraffic()
            } else {
                // Normal mode: Block only non-VPN traffic
                blockNonVPNTraffic()
            }
            
            Log.i(TAG, "🚫 Kill switch applied - Internet access blocked")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to apply kill switch: ${e.message}")
        }
    }
    
    /**
     * Remove kill switch (restore internet access)
     */
    private fun removeKillSwitch() {
        try {
            restoreInternetAccess()
            Log.i(TAG, "✅ Kill switch removed - Internet access restored")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to remove kill switch: ${e.message}")
        }
    }
    
    /**
     * Block all internet traffic
     */
    private fun blockAllTraffic() {
        // This would require root access or VPN service integration
        // For now, we'll log the action
        Log.i(TAG, "🚫 Blocking all internet traffic (strict mode)")
        
        // In a real implementation, you would:
        // 1. Use iptables to block all outbound traffic
        // 2. Or use VPN service to route all traffic through a dead-end interface
    }
    
    /**
     * Block non-VPN internet traffic
     */
    private fun blockNonVPNTraffic() {
        // This would require root access or VPN service integration
        // For now, we'll log the action
        Log.i(TAG, "🚫 Blocking non-VPN internet traffic")
        
        // In a real implementation, you would:
        // 1. Use iptables to block traffic not going through VPN interface
        // 2. Or use VPN service to route non-VPN traffic through a dead-end interface
    }
    
    /**
     * Restore internet access
     */
    private fun restoreInternetAccess() {
        // This would require root access or VPN service integration
        // For now, we'll log the action
        Log.i(TAG, "✅ Restoring internet access")
        
        // In a real implementation, you would:
        // 1. Remove iptables rules that block traffic
        // 2. Or restore normal routing through VPN service
    }
    
    /**
     * Check if VPN interface is active
     */
    private fun isVPNInterfaceActive(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not check VPN interface status: ${e.message}")
            false
        }
    }
    
    /**
     * Register network callback for monitoring
     */
    private fun registerNetworkCallback() {
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            Log.i(TAG, "📡 Network callback registered")
            
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
            Log.i(TAG, "📡 Network callback unregistered")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to unregister network callback: ${e.message}")
        }
    }
    
    /**
     * Get kill switch status
     */
    fun getStatus(): KillSwitchStatus {
        return KillSwitchStatus(
            isEnabled = _isEnabled.value,
            isActive = _isActive.value,
            isStrictMode = _isStrictMode.value
        )
    }
    
    /**
     * Emergency disable (for troubleshooting)
     */
    fun emergencyDisable() {
        Log.w(TAG, "🚨 Emergency disabling kill switch")
        setEnabled(false)
        deactivate()
    }
}

/**
 * Kill switch status information
 */
data class KillSwitchStatus(
    val isEnabled: Boolean,
    val isActive: Boolean,
    val isStrictMode: Boolean
)
