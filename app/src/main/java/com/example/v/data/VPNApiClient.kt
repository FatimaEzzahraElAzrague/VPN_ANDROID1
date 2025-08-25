package com.example.v.data

import android.util.Log
import com.example.v.data.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object VPNApiClient {
    private const val TAG = "VPNApiClient"
    
    // Production VPN server configuration
    private const val BASE_URL = "http://10.0.2.2:8080"
    
    private const val TIMEOUT_SECONDS = 30L
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 30L
    
    private val gson = Gson()
    
    /**
     * Get available VPN servers from backend
     */
    suspend fun getAvailableVPNServers(): List<VPNServer> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching available VPN servers from backend")
            
            val url = URL("$BASE_URL/vpn/servers")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "VPN-Android-App/1.0")
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "VPN servers response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                Log.d(TAG, "VPN servers response: $response")
                
                val responseData = gson.fromJson(response, Map::class.java)
                if (responseData["success"] == true) {
                    val serversJson = gson.toJson(responseData["servers"])
                    val servers = gson.fromJson(serversJson, Array<VPNServer>::class.java)
                    Log.d(TAG, "Successfully fetched ${servers.size} VPN servers")
                    return@withContext servers.toList()
                } else {
                    Log.e(TAG, "Backend returned error: ${responseData["error"]}")
                    return@withContext emptyList()
                }
            } else {
                Log.e(TAG, "Failed to fetch VPN servers. Response code: $responseCode")
                return@withContext emptyList()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching VPN servers", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Get VPN configuration for a user and server
     */
    suspend fun getVPNConfiguration(userId: String, serverId: String, authToken: String): VPNConfigResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching VPN configuration for user: $userId, server: $serverId")
            
            val url = URL("$BASE_URL/vpn/config/$userId/$serverId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $authToken")
                setRequestProperty("User-Agent", "VPN-Android-App/1.0")
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "VPN config response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                Log.d(TAG, "VPN config response: $response")
                
                val configResponse = gson.fromJson(response, VPNConfigResponse::class.java)
                if (configResponse.success) {
                    Log.d(TAG, "Successfully fetched VPN configuration")
                    return@withContext configResponse
                } else {
                    Log.e(TAG, "Backend returned error: ${configResponse.error}")
                    return@withContext configResponse
                }
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.e(TAG, "Unauthorized access to VPN configuration")
                return@withContext VPNConfigResponse(
                    success = false,
                    message = "Unauthorized access",
                    error = "Invalid or expired authentication token"
                )
            } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                Log.e(TAG, "Access forbidden to VPN configuration")
                return@withContext VPNConfigResponse(
                    success = false,
                    message = "Access forbidden",
                    error = "You don't have permission to access this configuration"
                )
            } else {
                Log.e(TAG, "Failed to fetch VPN configuration. Response code: $responseCode")
                return@withContext VPNConfigResponse(
                    success = false,
                    message = "Failed to fetch configuration",
                    error = "Server returned error code: $responseCode"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching VPN configuration", e)
            return@withContext VPNConfigResponse(
                success = false,
                message = "Network error",
                error = e.message ?: "Unknown error occurred"
            )
        }
    }
    
    /**
     * Request VPN configuration with custom parameters
     */
    suspend fun requestVPNConfiguration(
        userId: String,
        serverId: String,
        authToken: String,
        request: VPNConfigRequest
    ): VPNConfigResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Requesting VPN configuration for user: $userId, server: $serverId")
            
            val url = URL("$BASE_URL/vpn/config/$userId/$serverId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $authToken")
                setRequestProperty("User-Agent", "VPN-Android-App/1.0")
            }
            
            // Send request body
            val requestJson = gson.toJson(request)
            Log.d(TAG, "Sending request: $requestJson")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "VPN config request response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                Log.d(TAG, "VPN config request response: $response")
                
                val configResponse = gson.fromJson(response, VPNConfigResponse::class.java)
                if (configResponse.success) {
                    Log.d(TAG, "Successfully requested VPN configuration")
                    return@withContext configResponse
                } else {
                    Log.e(TAG, "Backend returned error: ${configResponse.error}")
                    return@withContext configResponse
                }
            } else {
                Log.e(TAG, "Failed to request VPN configuration. Response code: $responseCode")
                return@withContext VPNConfigResponse(
                    success = false,
                    message = "Failed to request configuration",
                    error = "Server returned error code: $responseCode"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting VPN configuration", e)
            return@withContext VPNConfigResponse(
                success = false,
                message = "Network error",
                error = e.message ?: "Unknown error occurred"
            )
        }
    }
    
    /**
     * Update VPN connection status
     */
    suspend fun updateConnectionStatus(
        userId: String,
        serverId: String,
        authToken: String,
        request: VPNConnectionRequest
    ): VPNStatusResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating connection status for user: $userId, server: $serverId, action: ${request.action}")
            
            val url = URL("$BASE_URL/vpn/connection/$userId/$serverId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $authToken")
                setRequestProperty("User-Agent", "VPN-Android-App/1.0")
            }
            
            // Send request body
            val requestJson = gson.toJson(request)
            Log.d(TAG, "Sending connection status update: $requestJson")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Connection status update response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                Log.d(TAG, "Connection status update response: $response")
                
                val connectionResponse = gson.fromJson(response, VPNStatusResponse::class.java)
                if (connectionResponse.success) {
                    Log.d(TAG, "Successfully updated connection status")
                    return@withContext connectionResponse
                } else {
                    Log.e(TAG, "Backend returned error: ${connectionResponse.error}")
                    return@withContext connectionResponse
                }
            } else {
                Log.e(TAG, "Failed to update connection status. Response code: $responseCode")
                return@withContext VPNStatusResponse(
                    success = false,
                    message = "Failed to update status",
                    error = "Server returned error code: $responseCode"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating connection status", e)
            return@withContext VPNStatusResponse(
                success = false,
                message = "Network error",
                error = e.message ?: "Unknown error occurred"
            )
        }
    }
    
    /**
     * Get VPN connection status
     */
    suspend fun getConnectionStatus(
        userId: String,
        serverId: String,
        authToken: String
    ): VPNConnectionStatus? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching connection status for user: $userId, server: $serverId")
            
            val url = URL("$BASE_URL/vpn/connection/$userId/$serverId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $authToken")
                setRequestProperty("User-Agent", "VPN-Android-App/1.0")
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Connection status response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                Log.d(TAG, "Connection status response: $response")
                
                val responseData = gson.fromJson(response, Map::class.java)
                if (responseData["success"] == true) {
                    val statusJson = gson.toJson(responseData["status"])
                    val status = gson.fromJson(statusJson, VPNConnectionStatus::class.java)
                    Log.d(TAG, "Successfully fetched connection status")
                    return@withContext status
                } else {
                    Log.e(TAG, "Backend returned error: ${responseData["error"]}")
                    return@withContext null
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                Log.d(TAG, "Connection status not found")
                return@withContext null
            } else {
                Log.e(TAG, "Failed to fetch connection status. Response code: $responseCode")
                return@withContext null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching connection status", e)
            return@withContext null
        }
    }
}
