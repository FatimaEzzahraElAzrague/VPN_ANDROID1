package com.example.v.vpn

import android.content.Intent
import android.net.VpnService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ProductionVPNService : VpnService() {
    companion object {
        private const val TAG = "ProductionVPNService"
        const val EXTRA_SERVER_ID = "server_id"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isConnecting = AtomicBoolean(false)
    private var currentServerId: String? = null
    private var vpnConfig: VPNConfig? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Log.e(TAG, "❌ Received null intent")
            return START_NOT_STICKY
        }

        serviceScope.launch {
            try {
                if (isConnecting.get()) {
                    Log.w(TAG, "⚠️ Already connecting, ignoring connect request")
                    return@launch
                }
                
                isConnecting.set(true)
                
                // Extract parameters
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID)
                
                if (serverId == null) {
                    Log.e(TAG, "❌ Missing server ID for VPN connection")
                    updateConnectionStatus(serverId ?: "", "ERROR", "Missing server ID")
                    isConnecting.set(false)
                    return@launch
                }
                
                currentServerId = serverId
                
                Log.i(TAG, "🔧 === STARTING PRODUCTION VPN CONNECTION ===")
                Log.i(TAG, "🖥️ Server: $serverId")
                
                // Step 1: Get VPN configuration
                val configResponse = VPNApiClient.getVPNConfiguration(serverId)
                if (configResponse == null) {
                    val error = "Failed to get VPN configuration"
                    Log.e(TAG, "❌ $error")
                    updateConnectionStatus(serverId, "ERROR", error)
                    isConnecting.set(false)
                    return@launch
                }
                
                vpnConfig = configResponse
                Log.i(TAG, "✅ VPN configuration received")
                
                // Step 2: Create VPN interface
                val builder = Builder()
                    .setMtu(vpnConfig?.mtu ?: 1420)
                    .addAddress(vpnConfig?.clientIp ?: "", 24)
                
                vpnConfig?.dns?.forEach { dns ->
                    builder.addDnsServer(dns)
                }
                
                vpnConfig?.routes?.forEach { route ->
                    builder.addRoute(route, 32)
                }
                
                // Step 3: Establish VPN connection
                val vpnInterface = builder.establish()
                if (vpnInterface == null) {
                    Log.e(TAG, "❌ Failed to establish VPN interface")
                    updateConnectionStatus(serverId, "ERROR", "Failed to establish VPN interface")
                    isConnecting.set(false)
                    return@launch
                }
                
                Log.i(TAG, "✅ VPN interface established")
                updateConnectionStatus(serverId, "CONNECTED", null)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ VPN connection failed", e)
                updateConnectionStatus(currentServerId ?: "", "ERROR", e.message)
            } finally {
                isConnecting.set(false)
            }
        }

        return START_STICKY
    }

    private fun updateConnectionStatus(serverId: String, status: String, error: String?) {
        // Implementation for updating connection status
        Log.i(TAG, "📊 Connection status updated: $status ${error ?: ""}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "🛑 VPN service destroyed")
    }
}

data class VPNConfig(
    val clientIp: String,
    val dns: List<String>,
    val routes: List<String>,
    val mtu: Int = 1420
)