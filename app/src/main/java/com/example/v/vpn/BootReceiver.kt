package com.example.v.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.v.data.autoconnect.AutoConnectRepository

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
                val repo = AutoConnectRepository(context)
                // Since isAutoConnectEnabled() returns a Flow, just check shared preferences directly
                val sharedPreferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
                val autoConnect = sharedPreferences.getBoolean("auto_connect_enabled", true)
                if (autoConnect) {
                    val wasConnected = sharedPreferences.getBoolean("was_connected_before_reboot", false)
                    
                    if (wasConnected && autoConnect) {
                        Log.d(TAG, "Auto-connecting VPN after boot")
                        
                        // Start VPN service with auto-connect using default Paris server
                        val vpnIntent = Intent(context, RealWireGuardVPNService::class.java).apply {
                            action = "CONNECT"
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