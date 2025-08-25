package com.example.v.vpn

import android.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

/**
 * Native interface to WireGuard-Go implementation
 * This bridges Android VpnService with the actual WireGuard protocol
 */
class WireGuardGoInterface {
    companion object {
        private const val TAG = "WireGuardGoInterface"
        
        // Load native library
        init {
            try {
                System.loadLibrary("wireguard-go")
                Log.i(TAG, "✅ WireGuard-Go native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "❌ Failed to load WireGuard-Go native library: ${e.message}")
            }
        }
    }
    
    // Native method declarations
    external fun createTunnel(config: String): Long
    external fun startTunnel(tunnelHandle: Long): Boolean
    external fun stopTunnel(tunnelHandle: Long): Boolean
    external fun destroyTunnel(tunnelHandle: Long)
    external fun getTunnelStatus(tunnelHandle: Long): String
    external fun updateTunnelConfig(tunnelHandle: Long, config: String): Boolean
    
    // Tunnel handle
    private var tunnelHandle: Long = 0
    private var isRunning: Boolean = false
    
    /**
     * Create and start WireGuard tunnel
     */
    fun startTunnel(config: WireGuardConfig): Boolean {
        return try {
            Log.i(TAG, "🚀 Starting WireGuard tunnel...")
            
            // Create tunnel
            val configJson = config.toJson()
            Log.d(TAG, "📋 Tunnel config: $configJson")
            
            tunnelHandle = createTunnel(configJson)
            if (tunnelHandle == 0L) {
                Log.e(TAG, "❌ Failed to create tunnel")
                return false
            }
            
            Log.i(TAG, "✅ Tunnel created with handle: $tunnelHandle")
            
            // Start tunnel
            val started = startTunnel(tunnelHandle)
            if (started) {
                isRunning = true
                Log.i(TAG, "🎉 WireGuard tunnel started successfully!")
            } else {
                Log.e(TAG, "❌ Failed to start tunnel")
                destroyTunnel(tunnelHandle)
                tunnelHandle = 0
            }
            
            started
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting tunnel: ${e.message}")
            false
        }
    }
    
    /**
     * Stop and destroy tunnel
     */
    fun stopTunnel(): Boolean {
        return try {
            if (tunnelHandle == 0L) {
                Log.w(TAG, "⚠️ No tunnel to stop")
                return true
            }
            
            Log.i(TAG, "🛑 Stopping WireGuard tunnel...")
            
            // Stop tunnel
            val stopped = stopTunnel(tunnelHandle)
            if (stopped) {
                isRunning = false
                Log.i(TAG, "✅ Tunnel stopped successfully")
            }
            
            // Destroy tunnel
            destroyTunnel(tunnelHandle)
            tunnelHandle = 0
            
            stopped
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error stopping tunnel: ${e.message}")
            false
        }
    }
    
    /**
     * Get tunnel status
     */
    fun getStatus(): String {
        return try {
            if (tunnelHandle == 0L) {
                return "No tunnel"
            }
            
            getTunnelStatus(tunnelHandle)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting tunnel status: ${e.message}")
            "Error: ${e.message}"
        }
    }
    
    /**
     * Check if tunnel is running
     */
    fun isTunnelRunning(): Boolean = isRunning && tunnelHandle != 0L
    
    /**
     * Update tunnel configuration
     */
    fun updateConfig(config: WireGuardConfig): Boolean {
        return try {
            if (tunnelHandle == 0L) {
                Log.w(TAG, "⚠️ No tunnel to update")
                return false
            }
            
            Log.i(TAG, "🔄 Updating tunnel configuration...")
            val configJson = config.toJson()
            val updated = updateTunnelConfig(tunnelHandle, configJson)
            
            if (updated) {
                Log.i(TAG, "✅ Tunnel configuration updated successfully")
            } else {
                Log.e(TAG, "❌ Failed to update tunnel configuration")
            }
            
            updated
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating tunnel config: ${e.message}")
            false
        }
    }
}

// WireGuardConfig is now defined in WireGuardConfigManager.kt
