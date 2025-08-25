package com.example.v.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Kill Switch Service - Prevents internet access when VPN is disconnected
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
            checkVPNStatus()
        }
        
        override fun onLost(network: Network) {
            checkVPNStatus()
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            checkVPNStatus()
        }
    }
    
    init {
        registerNetworkCallback()
    }
    
    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _isEnabled.value = enabled
        checkVPNStatus()
    }
    
    fun setStrictMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
        _isStrictMode.value = enabled
        checkVPNStatus()
    }
    
    private fun checkVPNStatus() {
        if (!_isEnabled.value) {
            _isActive.value = false
            return
        }
        
        val isVPNActive = isVPNInterfaceActive()
        _isActive.value = !isVPNActive
        
        Log.i(TAG, "VPN Status: ${if (isVPNActive) "Connected" else "Disconnected"}")
    }
    
    private fun isVPNInterfaceActive(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        } catch (e: Exception) {
            Log.w(TAG, "Could not check VPN interface status: ${e.message}")
            false
        }
    }
    
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}