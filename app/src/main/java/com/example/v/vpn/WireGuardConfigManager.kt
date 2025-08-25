package com.example.v.vpn

import android.content.Context
import android.util.Log
import com.example.v.data.models.VPNConnectionResponse
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Manages WireGuard configuration files and settings
 * Ensures Android compatibility while matching desktop functionality
 */
class WireGuardConfigManager(private val context: Context) {
    companion object {
        private const val TAG = "WireGuardConfigManager"
        private const val CONFIG_DIR = "wireguard_configs"
        private const val DEFAULT_MTU = 1420
        private const val DEFAULT_KEEPALIVE = 25
    }
    
    private val configDir = File(context.filesDir, CONFIG_DIR)
    
    init {
        createConfigDirectory()
    }
    
    /**
     * Create WireGuard configuration from API response
     */
    fun createConfig(vpnResponse: VPNConnectionResponse): WireGuardConfig {
        return WireGuardConfig(
            privateKey = vpnResponse.privateKey ?: "",
            publicKey = vpnResponse.publicKey ?: "",
            serverPublicKey = vpnResponse.serverPublicKey ?: "",
            serverEndpoint = vpnResponse.serverEndpoint ?: "",
            allowedIPs = vpnResponse.allowedIPs ?: "0.0.0.0/0,::/0",
            internalIP = vpnResponse.internalIP ?: "",
            dns = vpnResponse.dns ?: "8.8.8.8",
            mtu = vpnResponse.mtu ?: DEFAULT_MTU,
            presharedKey = vpnResponse.presharedKey ?: "",
            internalIPv6 = vpnResponse.internalIPv6 ?: "",
            keepalive = DEFAULT_KEEPALIVE,
            persistentKeepalive = true
        )
    }
    
    /**
     * Generate WireGuard configuration file content
     */
    fun generateConfigFile(config: WireGuardConfig): String {
        return """
        [Interface]
        PrivateKey = ${config.privateKey}
        Address = ${config.internalIP}/32
        ${if (config.internalIPv6.isNotEmpty()) "Address = ${config.internalIPv6}/128" else ""}
        DNS = ${config.dns}
        MTU = ${config.mtu}
        
        [Peer]
        PublicKey = ${config.serverPublicKey}
        ${if (config.presharedKey.isNotEmpty()) "PresharedKey = ${config.presharedKey}" else ""}
        AllowedIPs = ${config.allowedIPs}
        Endpoint = ${config.serverEndpoint}
        PersistentKeepalive = ${config.keepalive}
        """.trimIndent()
    }
    
    /**
     * Save configuration to file
     */
    fun saveConfig(config: WireGuardConfig, serverName: String): File? {
        return try {
            val configContent = generateConfigFile(config)
            val configFile = File(configDir, "${serverName}_wg.conf")
            
            FileWriter(configFile).use { writer ->
                writer.write(configContent)
            }
            
            Log.i(TAG, "✅ WireGuard config saved: ${configFile.absolutePath}")
            configFile
            
        } catch (e: IOException) {
            Log.e(TAG, "❌ Failed to save WireGuard config: ${e.message}")
            null
        }
    }
    
    /**
     * Load configuration from file
     */
    fun loadConfig(serverName: String): String? {
        return try {
            val configFile = File(configDir, "${serverName}_wg.conf")
            if (configFile.exists()) {
                configFile.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "❌ Failed to load WireGuard config: ${e.message}")
            null
        }
    }
    
    /**
     * Delete configuration file
     */
    fun deleteConfig(serverName: String): Boolean {
        return try {
            val configFile = File(configDir, "${serverName}_wg.conf")
            if (configFile.exists()) {
                configFile.delete()
                Log.i(TAG, "✅ WireGuard config deleted: $serverName")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete WireGuard config: ${e.message}")
            false
        }
    }
    
    /**
     * List all available configurations
     */
    fun listConfigs(): List<String> {
        return try {
            configDir.listFiles()
                ?.filter { it.name.endsWith("_wg.conf") }
                ?.map { it.name.removeSuffix("_wg.conf") }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to list configs: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Validate configuration
     */
    fun validateConfig(config: WireGuardConfig): Boolean {
        return try {
            val isValid = config.privateKey.isNotEmpty() &&
                    config.serverPublicKey.isNotEmpty() &&
                    config.serverEndpoint.isNotEmpty() &&
                    config.internalIP.isNotEmpty() &&
                    config.allowedIPs.isNotEmpty() &&
                    config.dns.isNotEmpty() &&
                    config.mtu > 0
            
            if (!isValid) {
                Log.w(TAG, "⚠️ Configuration validation failed:")
                Log.w(TAG, "   - PrivateKey: ${config.privateKey.isNotEmpty()}")
                Log.w(TAG, "   - ServerPublicKey: ${config.serverPublicKey.isNotEmpty()}")
                Log.w(TAG, "   - ServerEndpoint: ${config.serverEndpoint.isNotEmpty()}")
                Log.w(TAG, "   - InternalIP: ${config.internalIP.isNotEmpty()}")
                Log.w(TAG, "   - AllowedIPs: ${config.allowedIPs.isNotEmpty()}")
                Log.w(TAG, "   - DNS: ${config.dns.isNotEmpty()}")
                Log.w(TAG, "   - MTU: ${config.mtu > 0}")
            }
            
            isValid
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Configuration validation error: ${e.message}")
            false
        }
    }
    
    private fun createConfigDirectory() {
        if (!configDir.exists()) {
            configDir.mkdirs()
            Log.i(TAG, "✅ WireGuard config directory created: ${configDir.absolutePath}")
        }
    }
}

/**
 * Enhanced WireGuard configuration with additional settings
 */
data class WireGuardConfig(
    val privateKey: String,
    val publicKey: String,
    val serverPublicKey: String,
    val serverEndpoint: String,
    val allowedIPs: String,
    val internalIP: String,
    val dns: String,
    val mtu: Int,
    val presharedKey: String = "",
    val internalIPv6: String = "",
    val keepalive: Int = 25,
    val persistentKeepalive: Boolean = true
) {
    /**
     * Convert to JSON format for native code
     */
    fun toJson(): String {
        return """
        {
            "private_key": "$privateKey",
            "public_key": "$publicKey",
            "server_public_key": "$serverPublicKey",
            "server_endpoint": "$serverEndpoint",
            "allowed_ips": "$allowedIPs",
            "internal_ip": "$internalIP",
            "internal_ipv6": "$internalIPv6",
            "dns": "$dns",
            "mtu": $mtu,
            "preshared_key": "$presharedKey",
            "keepalive": $keepalive,
            "persistent_keepalive": $persistentKeepalive
        }
        """.trimIndent()
    }
}
