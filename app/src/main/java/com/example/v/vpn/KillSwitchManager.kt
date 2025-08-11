package com.example.v.vpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kill Switch Manager
 * Handles VPN connection monitoring and traffic blocking when VPN disconnects unexpectedly
 */
class KillSwitchManager(private val context: Context) {
    
    companion object {
        private const val TAG = "KillSwitchManager"
        private const val CONNECTION_CHECK_INTERVAL_MS = 5000L // 5 seconds
        private const val CONNECTION_TIMEOUT_MS = 10000L // 10 seconds
        private const val MAX_RECONNECTION_ATTEMPTS = 3
        
        @Volatile
        private var INSTANCE: KillSwitchManager? = null
        
        fun getInstance(context: Context): KillSwitchManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KillSwitchManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Kill switch state
    private var isKillSwitchEnabled = AtomicBoolean(false)
    private var isKillSwitchActive = AtomicBoolean(false)
    private var isVpnConnected = AtomicBoolean(false)
    
    // Connection monitoring
    private var connectionMonitorJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var connectivityManager: ConnectivityManager? = null
    
    // Reconnection attempts
    private var reconnectionAttempts = 0
    private var lastKnownServerEndpoint: InetSocketAddress? = null
    
    // Callbacks
    private val killSwitchListeners = mutableListOf<KillSwitchListener>()
    
    // Coroutine scope for background operations
    private val killSwitchScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Kill Switch Listener interface for UI notifications
     */
    interface KillSwitchListener {
        fun onKillSwitchActivated(reason: String)
        fun onKillSwitchDeactivated()
        fun onVpnDisconnected()
        fun onVpnReconnected()
        fun onReconnectionAttempt(attempt: Int, maxAttempts: Int)
        fun onReconnectionFailed()
    }
    
    /**
     * Enable kill switch functionality
     */
    fun enableKillSwitch() {
        if (isKillSwitchEnabled.compareAndSet(false, true)) {
            Log.d(TAG, "Kill switch enabled")
            startConnectionMonitoring()
            saveKillSwitchState(true)
        }
    }
    
    /**
     * Disable kill switch functionality
     */
    fun disableKillSwitch() {
        if (isKillSwitchEnabled.compareAndSet(true, false)) {
            Log.d(TAG, "Kill switch disabled")
            stopConnectionMonitoring()
            deactivateKillSwitch()
            saveKillSwitchState(false)
        }
    }
    
    /**
     * Check if kill switch is enabled
     */
    fun isKillSwitchEnabled(): Boolean = isKillSwitchEnabled.get()
    
    /**
     * Check if kill switch is currently active (blocking traffic)
     */
    fun isKillSwitchActive(): Boolean = isKillSwitchActive.get()
    
    /**
     * Check if VPN is currently connected
     */
    fun isVpnConnected(): Boolean = isVpnConnected.get()
    
    /**
     * Set VPN connection state
     */
    fun setVpnConnected(connected: Boolean, serverEndpoint: InetSocketAddress? = null) {
        val wasConnected = isVpnConnected.get()
        isVpnConnected.set(connected)
        
        if (connected) {
            Log.d(TAG, "VPN connected")
            lastKnownServerEndpoint = serverEndpoint
            reconnectionAttempts = 0
            deactivateKillSwitch()
            notifyVpnReconnected()
        } else {
            Log.d(TAG, "VPN disconnected")
            notifyVpnDisconnected()
            
            if (isKillSwitchEnabled.get() && wasConnected) {
                // VPN was connected but now disconnected - activate kill switch
                activateKillSwitch("VPN connection lost")
            }
        }
    }
    
    /**
     * Add kill switch listener
     */
    fun addKillSwitchListener(listener: KillSwitchListener) {
        if (!killSwitchListeners.contains(listener)) {
            killSwitchListeners.add(listener)
        }
    }
    
    /**
     * Remove kill switch listener
     */
    fun removeKillSwitchListener(listener: KillSwitchListener) {
        killSwitchListeners.remove(listener)
    }
    
    /**
     * Start connection monitoring
     */
    private fun startConnectionMonitoring() {
        Log.d(TAG, "Starting connection monitoring")
        
        // Start periodic connection checks
        connectionMonitorJob = killSwitchScope.launch {
            while (isKillSwitchEnabled.get()) {
                delay(CONNECTION_CHECK_INTERVAL_MS)
                
                if (isVpnConnected.get()) {
                    // Check if VPN connection is still alive
                    if (!isVpnConnectionAlive()) {
                        Log.w(TAG, "VPN connection check failed - connection appears dead")
                        handleVpnDisconnection("Connection check failed")
                    }
                } else if (isKillSwitchActive.get()) {
                    // Try to reconnect if kill switch is active
                    attemptReconnection()
                }
            }
        }
        
        // Start network connectivity monitoring
        startNetworkMonitoring()
    }
    
    /**
     * Stop connection monitoring
     */
    private fun stopConnectionMonitoring() {
        Log.d(TAG, "Stopping connection monitoring")
        
        connectionMonitorJob?.cancel()
        connectionMonitorJob = null
        
        stopNetworkMonitoring()
    }
    
    /**
     * Start network connectivity monitoring
     */
    private fun startNetworkMonitoring() {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                Log.w(TAG, "Network connection lost")
                if (isVpnConnected.get() && isKillSwitchEnabled.get()) {
                    handleVpnDisconnection("Network connection lost")
                }
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                // Monitor network capability changes
                if (isVpnConnected.get() && isKillSwitchEnabled.get()) {
                    // Check if VPN interface is still available
                    if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        Log.w(TAG, "VPN transport lost")
                        handleVpnDisconnection("VPN transport lost")
                    }
                }
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
        
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
    }
    
    /**
     * Stop network connectivity monitoring
     */
    private fun stopNetworkMonitoring() {
        networkCallback?.let { callback ->
            connectivityManager?.unregisterNetworkCallback(callback)
            networkCallback = null
        }
    }
    
    /**
     * Check if VPN connection is alive by testing connectivity
     */
    private suspend fun isVpnConnectionAlive(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Try to connect to a known server or DNS server through VPN
            val socket = Socket()
            socket.connect(InetSocketAddress("1.1.1.1", 53), CONNECTION_TIMEOUT_MS.toInt())
            socket.close()
            true
        } catch (e: Exception) {
            Log.d(TAG, "VPN connection test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Handle VPN disconnection
     */
    private fun handleVpnDisconnection(reason: String) {
        Log.w(TAG, "Handling VPN disconnection: $reason")
        
        isVpnConnected.set(false)
        notifyVpnDisconnected()
        
        if (isKillSwitchEnabled.get()) {
            activateKillSwitch(reason)
        }
    }
    
    /**
     * Activate kill switch (block all traffic)
     */
    private fun activateKillSwitch(reason: String) {
        if (isKillSwitchActive.compareAndSet(false, true)) {
            Log.w(TAG, "Activating kill switch: $reason")
            
            // Send intent to VPN service to activate kill switch
            val intent = Intent(context, WireGuardVpnService::class.java).apply {
                action = WireGuardVpnService.ACTION_ACTIVATE_KILL_SWITCH
                putExtra("reason", reason)
            }
            context.startService(intent)
            
            notifyKillSwitchActivated(reason)
        }
    }
    
    /**
     * Deactivate kill switch (allow traffic)
     */
    private fun deactivateKillSwitch() {
        if (isKillSwitchActive.compareAndSet(true, false)) {
            Log.d(TAG, "Deactivating kill switch")
            
            // Send intent to VPN service to deactivate kill switch
            val intent = Intent(context, WireGuardVpnService::class.java).apply {
                action = WireGuardVpnService.ACTION_DEACTIVATE_KILL_SWITCH
            }
            context.startService(intent)
            
            notifyKillSwitchDeactivated()
        }
    }
    
    /**
     * Attempt to reconnect to VPN
     */
    private fun attemptReconnection() {
        if (reconnectionAttempts >= MAX_RECONNECTION_ATTEMPTS) {
            Log.e(TAG, "Max reconnection attempts reached")
            notifyReconnectionFailed()
            return
        }
        
        reconnectionAttempts++
        Log.d(TAG, "Attempting VPN reconnection (attempt $reconnectionAttempts/$MAX_RECONNECTION_ATTEMPTS)")
        
        notifyReconnectionAttempt(reconnectionAttempts, MAX_RECONNECTION_ATTEMPTS)
        
        // Send reconnection intent to VPN service
        lastKnownServerEndpoint?.let { endpoint ->
            val intent = Intent(context, WireGuardVpnService::class.java).apply {
                action = WireGuardVpnService.ACTION_RECONNECT_VPN
                putExtra("server_endpoint", endpoint.toString())
            }
            context.startService(intent)
        }
    }
    
    /**
     * Load kill switch state from SharedPreferences
     */
    fun loadKillSwitchState() {
        val preferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        val enabled = preferences.getBoolean("kill_switch_enabled", false)
        
        if (enabled) {
            enableKillSwitch()
        }
        
        Log.d(TAG, "Kill switch state loaded: enabled=$enabled")
    }
    
    /**
     * Save kill switch state to SharedPreferences
     */
    private fun saveKillSwitchState(enabled: Boolean) {
        val preferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("kill_switch_enabled", enabled).apply()
        Log.d(TAG, "Kill switch state saved: enabled=$enabled")
    }
    
    /**
     * Notify listeners of kill switch activation
     */
    private fun notifyKillSwitchActivated(reason: String) {
        killSwitchListeners.forEach { listener ->
            try {
                listener.onKillSwitchActivated(reason)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying kill switch listener", e)
            }
        }
    }
    
    /**
     * Notify listeners of kill switch deactivation
     */
    private fun notifyKillSwitchDeactivated() {
        killSwitchListeners.forEach { listener ->
            try {
                listener.onKillSwitchDeactivated()
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying kill switch listener", e)
            }
        }
    }
    
    /**
     * Notify listeners of VPN disconnection
     */
    private fun notifyVpnDisconnected() {
        killSwitchListeners.forEach { listener ->
            try {
                listener.onVpnDisconnected()
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying kill switch listener", e)
            }
        }
    }
    
    /**
     * Notify listeners of VPN reconnection
     */
    private fun notifyVpnReconnected() {
        killSwitchListeners.forEach { listener ->
            try {
                listener.onVpnReconnected()
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying kill switch listener", e)
            }
        }
    }
    
    /**
     * Notify listeners of reconnection attempt
     */
    private fun notifyReconnectionAttempt(attempt: Int, maxAttempts: Int) {
        killSwitchListeners.forEach { listener ->
            try {
                listener.onReconnectionAttempt(attempt, maxAttempts)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying kill switch listener", e)
            }
        }
    }
    
    /**
     * Notify listeners of reconnection failure
     */
    private fun notifyReconnectionFailed() {
        killSwitchListeners.forEach { listener ->
            try {
                listener.onReconnectionFailed()
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying kill switch listener", e)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up KillSwitchManager")
        
        stopConnectionMonitoring()
        killSwitchListeners.clear()
        killSwitchScope.cancel()
    }
}
