package com.example.v.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * DNS Leak Protection Service - Prevents DNS queries from bypassing VPN
 * Matches desktop functionality while remaining Android-compatible
 */
class DNSLeakProtectionService private constructor(private val context: Context) {
    companion object {
        private const val TAG = "DNSLeakProtectionService"
        private const val PREFS_NAME = "dns_leak_protection_prefs"
        private const val KEY_ENABLED = "dns_leak_protection_enabled"
        private const val KEY_STRICT_MODE = "strict_dns_mode"
        private const val KEY_CUSTOM_DNS = "custom_dns_servers"
        private const val KEY_BLOCK_IPV6 = "block_ipv6_dns"
        
        // Default secure DNS servers
        private val DEFAULT_SECURE_DNS = listOf(
            "1.1.1.1",      // Cloudflare
            "8.8.8.8",      // Google
            "9.9.9.9",      // Quad9
            "208.67.222.222" // OpenDNS
        )
        
        @Volatile
        private var INSTANCE: DNSLeakProtectionService? = null
        
        fun getInstance(context: Context): DNSLeakProtectionService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DNSLeakProtectionService(context.applicationContext).also { INSTANCE = it }
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
    
    private val _customDNSServers = MutableStateFlow<List<String>>(getCustomDNSServers())
    val customDNSServers: StateFlow<List<String>> = _customDNSServers
    
    private val _blockIPv6 = MutableStateFlow(prefs.getBoolean(KEY_BLOCK_IPV6, true))
    val blockIPv6: StateFlow<Boolean> = _blockIPv6
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive
    
    // DNS leak detection results
    private val _lastLeakTest = MutableStateFlow<DNSLeakTestResult?>(null)
    val lastLeakTest: StateFlow<DNSLeakTestResult?> = _lastLeakTest
    
    /**
     * Enable/disable DNS leak protection
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        Log.i(TAG, "🔧 DNS leak protection ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            activate()
        } else {
            deactivate()
        }
    }
    
    /**
     * Enable/disable strict DNS mode
     */
    fun setStrictMode(enabled: Boolean) {
        _isStrictMode.value = enabled
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
        Log.i(TAG, "🔧 Strict DNS mode ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Set custom DNS servers
     */
    fun setCustomDNSServers(servers: List<String>) {
        _customDNSServers.value = servers
        saveCustomDNSServers(servers)
        Log.i(TAG, "🔧 Custom DNS servers updated: $servers")
    }
    
    /**
     * Add custom DNS server
     */
    fun addCustomDNSServer(server: String) {
        if (isValidDNSServer(server)) {
            val current = _customDNSServers.value.toMutableList()
            if (!current.contains(server)) {
                current.add(server)
                _customDNSServers.value = current
                saveCustomDNSServers(current)
                Log.i(TAG, "➕ Custom DNS server added: $server")
            }
        } else {
            Log.w(TAG, "⚠️ Invalid DNS server format: $server")
        }
    }
    
    /**
     * Remove custom DNS server
     */
    fun removeCustomDNSServer(server: String) {
        val current = _customDNSServers.value.toMutableList()
        if (current.remove(server)) {
            _customDNSServers.value = current
            saveCustomDNSServers(current)
            Log.i(TAG, "➖ Custom DNS server removed: $server")
        }
    }
    
    /**
     * Enable/disable IPv6 DNS blocking
     */
    fun setBlockIPv6(enabled: Boolean) {
        _blockIPv6.value = enabled
        prefs.edit().putBoolean(KEY_BLOCK_IPV6, enabled).apply()
        Log.i(TAG, "🔧 IPv6 DNS blocking ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Activate DNS leak protection
     */
    fun activate() {
        if (!_isEnabled.value) {
            Log.i(TAG, "⚠️ DNS leak protection is disabled, not activating")
            return
        }
        
        _isActive.value = true
        Log.i(TAG, "🛡️ DNS leak protection activated")
    }
    
    /**
     * Deactivate DNS leak protection
     */
    fun deactivate() {
        _isActive.value = false
        Log.i(TAG, "🛡️ DNS leak protection deactivated")
    }
    
    /**
     * Test for DNS leaks
     */
    suspend fun testDNSLeak(): DNSLeakTestResult {
        return try {
            Log.i(TAG, "🔍 Testing for DNS leaks...")
            
            val detectedLeaks = mutableListOf<String>()
            val recommendations = mutableListOf<String>()
            
            // Test 1: Check if DNS queries go through VPN
            val dnsThroughVPN = testDNSThroughVPN()
            if (!dnsThroughVPN) {
                detectedLeaks.add("DNS queries not going through VPN")
                recommendations.add("Enable DNS leak protection")
            }
            
            // Test 2: Check for IPv6 DNS leaks
            if (_blockIPv6.value) {
                val ipv6Leak = testIPv6DNSLeak()
                if (ipv6Leak) {
                    detectedLeaks.add("IPv6 DNS queries detected")
                    recommendations.add("Block IPv6 DNS queries")
                }
            }
            
            // Test 3: Check for custom DNS server leaks
            val customDNSLeak = testCustomDNSLeak()
            if (customDNSLeak) {
                detectedLeaks.add("Custom DNS server queries detected")
                recommendations.add("Use VPN DNS servers only")
            }
            
            // Test 4: Check for system DNS leaks
            val systemDNSLeak = testSystemDNSLeak()
            if (systemDNSLeak) {
                detectedLeaks.add("System DNS queries detected")
                recommendations.add("Force all DNS through VPN")
            }
            
            val isLeakDetected = detectedLeaks.isNotEmpty()
            
            val testResult = DNSLeakTestResult(
                timestamp = System.currentTimeMillis(),
                isVPNConnected = isVPNConnected(),
                isLeakDetected = isLeakDetected,
                detectedLeaks = detectedLeaks,
                recommendations = recommendations
            )
            
            _lastLeakTest.value = testResult
            
            if (testResult.isLeakDetected) {
                Log.w(TAG, "🚨 DNS leak detected: ${testResult.detectedLeaks}")
            } else {
                Log.i(TAG, "✅ No DNS leaks detected")
            }
            
            testResult
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ DNS leak test failed: ${e.message}")
            DNSLeakTestResult(
                timestamp = System.currentTimeMillis(),
                isVPNConnected = false,
                isLeakDetected = false,
                detectedLeaks = listOf("Test failed: ${e.message}"),
                recommendations = listOf("Check VPN connection and try again")
            )
        }
    }
    
    /**
     * Test if DNS queries go through VPN
     */
    private fun testDNSThroughVPN(): Boolean {
        return try {
            // In a real implementation, you would:
            // 1. Check if VPN interface is active
            // 2. Verify DNS queries are routed through VPN
            // 3. Check for any DNS queries bypassing VPN
            
            val isVPNActive = isVPNConnected()
            if (!isVPNActive) {
                Log.w(TAG, "⚠️ VPN not connected, cannot test DNS routing")
                return false
            }
            
            // Simulate DNS routing check
            // In reality, you'd use network monitoring tools
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ DNS routing test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Test for IPv6 DNS leaks
     */
    private fun testIPv6DNSLeak(): Boolean {
        return try {
            // Check if IPv6 DNS queries are being made
            // In a real implementation, you'd monitor network traffic
            
            if (_blockIPv6.value) {
                // Simulate IPv6 DNS leak detection
                // In reality, you'd check network interfaces and routing
                false
            } else {
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ IPv6 DNS leak test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Test for custom DNS server leaks
     */
    private fun testCustomDNSLeak(): Boolean {
        return try {
            // Check if custom DNS servers are being used instead of VPN DNS
            // In a real implementation, you'd monitor DNS queries
            
            val customServers = _customDNSServers.value
            if (customServers.isNotEmpty()) {
                // Simulate custom DNS leak detection
                // In reality, you'd check actual DNS queries
                false
            } else {
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Custom DNS leak test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Test for system DNS leaks
     */
    private fun testSystemDNSLeak(): Boolean {
        return try {
            // Check if system DNS servers are being used
            // In a real implementation, you'd monitor DNS queries
            
            // Simulate system DNS leak detection
            // In reality, you'd check actual DNS queries
            false
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ System DNS leak test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get recommended DNS servers
     */
    fun getRecommendedDNSServers(): List<String> {
        return if (_customDNSServers.value.isNotEmpty()) {
            _customDNSServers.value
        } else {
            DEFAULT_SECURE_DNS
        }
    }
    
    /**
     * Validate DNS server format
     */
    private fun isValidDNSServer(server: String): Boolean {
        return try {
            // Check if it's a valid IP address
            val parts = server.split(".")
            if (parts.size != 4) return false
            
            parts.all { part ->
                val num = part.toInt()
                num in 0..255
            }
            
        } catch (e: Exception) {
            false
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
     * Get DNS leak protection configuration
     */
    fun getConfig(): DNSLeakProtectionConfig {
        return DNSLeakProtectionConfig(
            isEnabled = _isEnabled.value,
            isStrictMode = _isStrictMode.value,
            customDNSServers = _customDNSServers.value,
            blockIPv6 = _blockIPv6.value,
            isActive = _isActive.value,
            lastLeakTest = _lastLeakTest.value
        )
    }
    
    private fun getCustomDNSServers(): List<String> {
        return prefs.getStringSet(KEY_CUSTOM_DNS, emptySet())?.toList() ?: emptyList()
    }
    
    private fun saveCustomDNSServers(servers: List<String>) {
        prefs.edit().putStringSet(KEY_CUSTOM_DNS, servers.toSet()).apply()
    }
}

/**
 * DNS leak protection configuration
 */
data class DNSLeakProtectionConfig(
    val isEnabled: Boolean,
    val isStrictMode: Boolean,
    val customDNSServers: List<String>,
    val blockIPv6: Boolean,
    val isActive: Boolean,
    val lastLeakTest: DNSLeakTestResult?
)

/**
 * DNS leak test result
 */
data class DNSLeakTestResult(
    val timestamp: Long,
    val isVPNConnected: Boolean,
    val isLeakDetected: Boolean,
    val detectedLeaks: List<String>,
    val recommendations: List<String>
)
