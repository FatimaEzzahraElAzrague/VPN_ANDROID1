package com.example.v.utils

import android.util.Log
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class to check current public IP address
 * Used to verify VPN is working by checking IP change
 */
object IPChecker {
    private const val TAG = "IPChecker"
    
    /**
     * Get current public IP address using api.ipify.org
     * @return IP address string or null if failed
     */
    suspend fun getCurrentIP(): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 DEBUG: Checking current IP...")
            
            val url = URL("https://api.ipify.org")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "WireGuard-VPN-Android/1.0")
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val currentIP = reader.readText().trim()
                reader.close()
                connection.disconnect()
                
                Log.d(TAG, "✅ Current IP: $currentIP")
                return@withContext currentIP
            } else {
                Log.w(TAG, "❌ IP check failed with response code: $responseCode")
                connection.disconnect()
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking IP", e)
            return@withContext null
        }
    }
    
    /**
     * Check if IP has changed compared to previous IP
     * @param previousIP the IP address before VPN connection
     * @return true if IP changed, false otherwise
     */
    suspend fun hasIPChanged(previousIP: String?): Boolean {
        val currentIP = getCurrentIP()
        return currentIP != null && currentIP != previousIP
    }
    
    /**
     * Verify VPN connection by checking IP change
     * @param originalIP IP before VPN connection
     * @param callback callback with result (true if VPN working, false otherwise)
     */
    fun verifyVPNConnection(originalIP: String?, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentIP = getCurrentIP()
                val isWorking = currentIP != null && currentIP != originalIP
                
                withContext(Dispatchers.Main) {
                    callback(isWorking, currentIP)
                }
                
                if (isWorking) {
                    Log.d(TAG, "✅ VPN verification PASSED: IP changed from $originalIP to $currentIP")
                } else {
                    Log.w(TAG, "❌ VPN verification FAILED: IP did not change (still $currentIP)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying VPN connection", e)
                withContext(Dispatchers.Main) {
                    callback(false, null)
                }
            }
        }
    }
}
