package com.example.v.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

/**
 * Network utilities for VPN debugging and connectivity testing
 */
object NetworkUtils {
    
    private const val TAG = "NetworkUtils"
    
    /**
     * Test connectivity to the Paris server
     */
    suspend fun testServerConnectivity(serverIP: String, port: Int = 51820): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Testing connectivity to $serverIP:$port...")
                
                // Test UDP socket connection (WireGuard uses UDP)
                val socket = Socket()
                socket.connect(java.net.InetSocketAddress(serverIP, port), 5000)
                socket.close()
                
                Log.d(TAG, "Successfully connected to $serverIP:$port")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to $serverIP:$port", e)
                false
            }
        }
    }
    
    /**
     * Test DNS resolution
     */
    suspend fun testDNSResolution(hostname: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Resolving DNS for $hostname...")
                val address = InetAddress.getByName(hostname)
                val ip = address.hostAddress
                Log.d(TAG, "DNS resolved: $hostname -> $ip")
                ip
            } catch (e: Exception) {
                Log.e(TAG, "DNS resolution failed for $hostname", e)
                null
            }
        }
    }
    
    /**
     * Check if device has internet connectivity
     */
    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Get network type (WiFi, Cellular, etc.)
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return "No Connection"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }
    
    /**
     * Test if we can reach the internet through the VPN
     */
    suspend fun testVPNConnectivity(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Testing VPN connectivity to 1.1.1.1...")
                
                val socket = Socket()
                socket.connect(java.net.InetSocketAddress("1.1.1.1", 53), 5000)
                socket.close()
                
                Log.d(TAG, "VPN connectivity test passed")
                true
            } catch (e: Exception) {
                Log.e(TAG, "VPN connectivity test failed", e)
                false
            }
        }
    }
    
    /**
     * Comprehensive network diagnostic
     */
    suspend fun runNetworkDiagnostic(context: Context, serverIP: String): Map<String, Any> {
        Log.d(TAG, "Running comprehensive network diagnostic...")
        
        val results = mutableMapOf<String, Any>()
        
        // Basic connectivity
        results["hasInternet"] = hasInternetConnection(context)
        results["networkType"] = getNetworkType(context)
        
        // DNS resolution
        results["dnsWorking"] = testDNSResolution("google.com") != null
        
        // Server connectivity
        results["serverReachable"] = testServerConnectivity(serverIP)
        
        // VPN connectivity (if connected)
        results["vpnConnectivity"] = testVPNConnectivity()
        
        Log.d(TAG, "Network diagnostic completed: $results")
        return results
    }
}
