package com.example.v.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.v.config.VPNConfig

/**
 * Boot receiver to automatically connect VPN when device boots up
 * This is useful for maintaining VPN connection across device restarts
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Boot completed, checking auto-connect settings")
                
                // Check if auto-connect is enabled in settings
                if (VPNConfig.Settings.AUTO_CONNECT_ON_BOOT) {
                    // Get stored VPN preferences
                    val sharedPreferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
                    val wasConnected = sharedPreferences.getBoolean("was_connected_before_reboot", false)
                    val autoConnect = sharedPreferences.getBoolean("auto_connect_enabled", true)
                    
                    if (wasConnected && autoConnect) {
                        Log.d(TAG, "Auto-connecting VPN after boot")
                        
                        // Start VPN service with auto-connect using default Paris server
                        val vpnIntent = Intent(context, WireGuardVpnService::class.java).apply {
                            action = WireGuardVpnService.ACTION_CONNECT
                            // Use default Paris server configuration for auto-connect
                            putExtra("server", VPNConfig.parisServer)
                            putExtra("client_config", VPNConfig.parisClientConfig)
                        }
                        
                        try {
                            context.startForegroundService(vpnIntent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to start VPN service on boot", e)
                        }
                    }
                }
            }
        }
    }
}