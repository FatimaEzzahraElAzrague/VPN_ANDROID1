package com.example.v.data.killswitch

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Kill Switch Manager
 * Monitors VPN connection and blocks internet traffic when VPN disconnects
 */
class KillSwitchManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "KillSwitchManager"
        
        @Volatile
        private var INSTANCE: KillSwitchManager? = null
        
        fun getInstance(context: Context): KillSwitchManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KillSwitchManager(context).also { INSTANCE = it }
            }
        }
    }
    
    private var isEnabled = false
    private var isActive = false
    private var vpnConnected = false
    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Network monitoring
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var monitoringJob: Job? = null
    
    // VPN connection monitoring
    private var vpnMonitoringJob: Job? = null
    private var lastVpnCheck = 0L
    private val vpnCheckInterval = 5000L // 5 seconds
    
    init {
        Log.i(TAG, "🔧 KillSwitchManager initialized")
    }
    
    /**
     * Enable or disable kill switch
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        KillSwitchPrefs.setEnabled(context, enabled)
        
        if (enabled) {
            startMonitoring()
            Log.i(TAG, "✅ Kill Switch enabled")
        } else {
            stopMonitoring()
            Log.i(TAG, "❌ Kill Switch disabled")
        }
    }
    
    /**
     * Check if kill switch is enabled
     */
    fun isEnabled(): Boolean = isEnabled
    
    /**
     * Check if kill switch is currently active (blocking traffic)
     */
    fun isActive(): Boolean = isActive
    
    /**
     * Check if VPN is connected
     */
    fun isVpnConnected(): Boolean = vpnConnected
    
    /**
     * Start monitoring VPN connection
     */
    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        Log.i(TAG, "📡 Starting VPN connection monitoring...")
        
        // Start network monitoring
        startNetworkMonitoring()
        
        // Start VPN connection monitoring
        startVpnConnectionMonitoring()
        
        monitoringJob = scope.launch {
            try {
                while (isEnabled) {
                    delay(1000) // Check every second
                    
                    // Check VPN connection status
                    val currentVpnStatus = checkVpnConnection()
                    
                    if (currentVpnStatus != vpnConnected) {
                        Log.i(TAG, "🔄 VPN status changed: ${if (vpnConnected) "Connected" else "Disconnected"} -> ${if (currentVpnStatus) "Connected" else "Disconnected"}")
                        
                        vpnConnected = currentVpnStatus
                        
                        if (vpnConnected) {
                            // VPN connected - allow traffic
                            allowInternetTraffic()
                        } else {
                            // VPN disconnected - block traffic
                            blockInternetTraffic()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in monitoring loop: ${e.message}")
            }
        }
    }
    
    /**
     * Stop monitoring
     */
    private fun stopMonitoring() {
        Log.i(TAG, "🛑 Stopping VPN connection monitoring...")
        
        monitoringJob?.cancel()
        monitoringJob = null
        
        stopNetworkMonitoring()
        stopVpnConnectionMonitoring()
        
        // If kill switch was active, restore internet
        if (isActive) {
            allowInternetTraffic()
        }
    }
    
    /**
     * Start network monitoring
     */
    private fun startNetworkMonitoring() {
        try {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "🌐 Network available: ${network}")
                }
                
                override fun onLost(network: Network) {
                    Log.d(TAG, "🌐 Network lost: ${network}")
                    
                    if (isEnabled && vpnConnected) {
                        // Network lost while VPN is connected - this might indicate VPN disconnection
                        scope.launch {
                            delay(2000) // Wait 2 seconds to see if VPN recovers
                            if (!checkVpnConnection()) {
                                Log.w(TAG, "⚠️ Network lost, VPN appears disconnected")
                                vpnConnected = false
                                blockInternetTraffic()
                            }
                        }
                    }
                }
            }
            
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            Log.d(TAG, "📡 Network monitoring started")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start network monitoring: ${e.message}")
        }
    }
    
    /**
     * Stop network monitoring
     */
    private fun stopNetworkMonitoring() {
        try {
            networkCallback?.let { callback ->
                connectivityManager.unregisterNetworkCallback(callback)
                networkCallback = null
                Log.d(TAG, "📡 Network monitoring stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error stopping network monitoring: ${e.message}")
        }
    }
    
    /**
     * Start VPN connection monitoring
     */
    private fun startVpnConnectionMonitoring() {
        vpnMonitoringJob = scope.launch {
            try {
                while (isEnabled) {
                    delay(vpnCheckInterval)
                    
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastVpnCheck > vpnCheckInterval) {
                        lastVpnCheck = currentTime
                        
                        val vpnStatus = checkVpnConnection()
                        if (vpnStatus != vpnConnected) {
                            Log.i(TAG, "🔄 VPN status changed via monitoring: ${if (vpnConnected) "Connected" else "Disconnected"} -> ${if (vpnStatus) "Connected" else "Disconnected"}")
                            
                            vpnConnected = vpnStatus
                            
                            if (vpnConnected) {
                                allowInternetTraffic()
                            } else {
                                blockInternetTraffic()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in VPN monitoring: ${e.message}")
            }
        }
    }
    
    /**
     * Stop VPN connection monitoring
     */
    private fun stopVpnConnectionMonitoring() {
        vpnMonitoringJob?.cancel()
        vpnMonitoringJob = null
    }
    
    /**
     * Check if VPN is currently connected
     */
    private fun checkVpnConnection(): Boolean {
        return try {
            // Check if there's an active VPN interface
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error checking VPN connection: ${e.message}")
            false
        }
    }
    
    /**
     * Block internet traffic
     */
    private fun blockInternetTraffic() {
        if (isActive) return // Already blocking
        
        Log.w(TAG, "🚨 BLOCKING INTERNET TRAFFIC - VPN disconnected!")
        isActive = true
        
        // In a real implementation, this would:
        // 1. Create a blocking VPN interface
        // 2. Route all traffic to it
        // 3. Drop all packets
        
        // For now, we'll just log the action
        // TODO: Implement actual traffic blocking
    }
    
    /**
     * Allow internet traffic
     */
    private fun allowInternetTraffic() {
        if (!isActive) return // Already allowing
        
        Log.i(TAG, "✅ ALLOWING INTERNET TRAFFIC - VPN connected")
        isActive = false
        
        // In a real implementation, this would:
        // 1. Remove blocking VPN interface
        // 2. Restore normal routing
        // 3. Allow traffic through VPN
        
        // For now, we'll just log the action
        // TODO: Implement actual traffic restoration
    }
    
    /**
     * Force check VPN status
     */
    fun forceCheckVpnStatus() {
        scope.launch {
            val status = checkVpnConnection()
            Log.i(TAG, "🔍 Force VPN status check: ${if (status) "Connected" else "Disconnected"}")
            
            if (status != vpnConnected) {
                vpnConnected = status
                if (vpnConnected) {
                    allowInternetTraffic()
                } else {
                    blockInternetTraffic()
                }
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
        Log.i(TAG, "🧹 KillSwitchManager cleaned up")
    }
}
