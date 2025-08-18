package com.example.v.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager

/**
 * Utility class for network-related operations
 */
object NetworkUtils {

    /**
     * Check if the device has an active internet connection
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Check if the current network is considered secure
     */
    fun isNetworkSecure(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        // Check if it's a WiFi network
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            // WiFi networks are generally considered secure if they have internet access
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        
        // Cellular networks are generally considered secure
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        }
        
        return false
    }

    /**
     * Get the security level of the current network
     */
    fun getNetworkSecurityLevel(context: Context): NetworkSecurityLevel {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkSecurityLevel.UNKNOWN
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkSecurityLevel.UNKNOWN
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    NetworkSecurityLevel.SECURE
                } else {
                    NetworkSecurityLevel.UNSECURE
                }
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkSecurityLevel.SECURE
            }
            else -> NetworkSecurityLevel.UNKNOWN
        }
    }

    /**
     * Get the SSID of the currently connected WiFi network
     */
    fun getCurrentNetworkSSID(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            
            // Check if WiFi is enabled
            if (!wifiManager.isWifiEnabled) {
                return null
            }
            
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid
            
            // Remove quotes if present (Android sometimes adds quotes to SSIDs)
            return if (ssid.isNotEmpty() && ssid != "<unknown ssid>") {
                ssid.removeSurrounding("\"")
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Get the current network type (WiFi, Cellular, etc.)
     */
    fun getCurrentNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return "None"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "None"
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
            else -> "Unknown"
        }
    }

    /**
     * Test server connectivity
     */
    fun testServerConnectivity(serverUrl: String): Boolean {
        return try {
            val url = java.net.URL(serverUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Enum representing network security levels
 */
enum class NetworkSecurityLevel {
    SECURE,      // Encrypted, trusted network
    MODERATE,    // Somewhat secure network
    UNSECURE,    // Open, unencrypted network
    UNKNOWN      // Unable to determine security level
}

/**
 * Enum representing user's network security preferences
 */
enum class NetworkSecurity(val title: String, val description: String) {
    SECURE_ONLY("Secure Only", "Connect only to encrypted networks"),
    UNSECURE_ONLY("Unsecure Only", "Connect only to open networks"),
    AUTO("Automatic", "Let the app decide based on network type"),
    ALWAYS("Always", "Connect to any available network"),
    NEVER("Never", "Don't auto-connect to any network")
}
