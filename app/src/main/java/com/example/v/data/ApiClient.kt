package com.example.v.data

import android.util.Log
import com.example.v.models.*
import com.example.v.data.models.VPNConnectionResponse
import com.example.v.models.Server
import com.example.v.data.ServerStatus
import com.example.v.data.PingResult
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class SignupRequest(
    val email: String,
    val password: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String
)

data class SignupResponse(
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    @SerializedName("id_token")
    val idToken: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,
    val user: UserResponse? = null,
    val message: String? = null,
    val email: String? = null,
    @SerializedName("requires_verification")
    val requiresVerification: Boolean? = null
)

data class UserResponse(
    val id: Int,
    val email: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("last_login")
    val lastLogin: String?
)

data class ProfileUpdateRequest(
    val username: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null
)

data class PasswordUpdateRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)





object ApiClient {
    // Local testing configuration
    private const val BASE_URL = "http://10.0.2.2:8080"
    
    private const val TIMEOUT_SECONDS = 30L
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 30L
    
    private val gson = Gson()
    
    private suspend fun makeRequest(
        endpoint: String,
        method: String = "GET",
        body: String? = null,
        token: String? = null
    ): String = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Android-App")
            
            // Set timeouts
            connection.connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
            connection.readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
            
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { os ->
                    os.write(body.toByteArray())
                }
            }
            
            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val response = inputStream?.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: ""
            
            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode: $response")
            }
            
            response
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }
    
    suspend fun signup(request: SignupRequest): SignupResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/signup", "POST", body)
        return gson.fromJson(response, SignupResponse::class.java)
    }
    
    suspend fun verifyOtp(request: VerifyOtpRequest): AuthResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/verify-otp", "POST", body)
        return gson.fromJson(response, AuthResponse::class.java)
    }
    
    suspend fun login(request: LoginRequest): AuthResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/login", "POST", body)
        return gson.fromJson(response, AuthResponse::class.java)
    }
    
    suspend fun googleAuth(request: GoogleAuthRequest): AuthResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/google-auth", "POST", body)
        return gson.fromJson(response, AuthResponse::class.java)
    }
    
    suspend fun getProfile(token: String): UserResponse {
        val response = makeRequest("/profile", "GET", token = token)
        return gson.fromJson(response, UserResponse::class.java)
    }
    
    suspend fun updateProfile(request: ProfileUpdateRequest, token: String): UserResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/profile", "PUT", body, token)
        return gson.fromJson(response, UserResponse::class.java)
    }
    
    suspend fun updatePassword(request: PasswordUpdateRequest, token: String): Map<String, String> {
        val body = gson.toJson(request)
        val response = makeRequest("/profile/password", "PATCH", body, token)
        return gson.fromJson(response, Map::class.java) as Map<String, String>
    }
    
    suspend fun deleteProfile(token: String): Map<String, String> {
        val response = makeRequest("/profile", "DELETE", token = token)
        return gson.fromJson(response, Map::class.java) as Map<String, String>
    }

    /**
     * Get VPN configuration using JWT authentication
     * User must be logged in to get VPN configs
     */
    suspend fun getVPNConfig(locationId: String, options: VPNOptions, userToken: String): VPNConnectionResponse {
        val requestBody = """
        {
            "location": "$locationId",
            "ad_block_enabled": ${options.adBlockEnabled},
            "anti_malware_enabled": ${options.antiMalwareEnabled},
            "family_safe_mode_enabled": ${options.familySafeModeEnabled}
        }
        """.trimIndent()
        
        Log.d("ApiClient", "📡 Getting VPN config for $locationId with JWT authentication")
        // Use user's JWT token for secure authentication
        val response = makeRequest("/vpn/connect", "POST", requestBody, userToken)
        return gson.fromJson(response, VPNConnectionResponse::class.java)
    }
    
    /**
     * Get VPN configuration for specific user (requires JWT auth)
     */
    suspend fun getUserVPNConfig(userId: String, userToken: String): VPNConnectionResponse {
        Log.d("ApiClient", "📡 Getting VPN config for user: $userId")
        val response = makeRequest("/vpn/config/$userId", "GET", token = userToken)
        return gson.fromJson(response, VPNConnectionResponse::class.java)
    }

    /**
     * Get all available VPN servers from backend (replaces hardcoded ServersData)
     */
    suspend fun getServers(): List<Server> {
        Log.d("ApiClient", "📡 Getting servers from backend")
        val response = makeRequest("/vpn/servers")
        val jsonResponse = gson.fromJson(response, Map::class.java)
        
        if (jsonResponse["success"] == true && jsonResponse["servers"] != null) {
            val serversJson = gson.toJson(jsonResponse["servers"])
            return gson.fromJson(serversJson, Array<Server>::class.java).toList()
        } else {
            Log.w("ApiClient", "⚠️ Backend returned no servers, using fallback")
            return getFallbackServers()
        }
    }

    /**
     * Get servers grouped by region
     */
    suspend fun getServersByRegion(): Map<String, List<Server>> {
        Log.d("ApiClient", "📡 Getting servers by region from backend")
        val response = makeRequest("/vpn/servers/regions")
        val jsonResponse = gson.fromJson(response, Map::class.java)
        
        if (jsonResponse["success"] == true && jsonResponse["regions"] != null) {
            val regionsJson = gson.toJson(jsonResponse["regions"])
            return gson.fromJson(regionsJson, Map::class.java) as Map<String, List<Server>>
        } else {
            Log.w("ApiClient", "⚠️ Backend returned no regions, using fallback")
            return mapOf("Default" to getFallbackServers())
        }
    }

    /**
     * Get server status
     */
    suspend fun getServerStatus(serverId: String): ServerStatus {
        Log.d("ApiClient", "📡 Getting status for server: $serverId")
        val response = makeRequest("/vpn/servers/$serverId/status")
        val jsonResponse = gson.fromJson(response, Map::class.java)
        
        if (jsonResponse["success"] == true && jsonResponse["status"] != null) {
            val statusJson = gson.toJson(jsonResponse["status"])
            return gson.fromJson(statusJson, ServerStatus::class.java)
        } else {
            throw Exception("Failed to get server status")
        }
    }

    /**
     * Get server ping/latency
     */
    suspend fun getServerPing(serverId: String): PingResult {
        Log.d("ApiClient", "📡 Getting ping for server: $serverId")
        val response = makeRequest("/vpn/servers/$serverId/ping")
        val jsonResponse = gson.fromJson(response, Map::class.java)
        
        if (jsonResponse["success"] == true && jsonResponse["ping"] != null) {
            val pingJson = gson.toJson(jsonResponse["ping"])
            return gson.fromJson(pingJson, PingResult::class.java)
        } else {
            throw Exception("Failed to get server ping")
        }
    }

    /**
     * Fallback servers if backend is unavailable
     */
    private fun getFallbackServers(): List<Server> {
        return listOf(
            Server(
                id = "paris",
                name = "Paris",
                city = "Paris",
                country = "France",
                flag = "🇫🇷",
                ip = "52.47.190.220",
                port = 51820,
                subnet = "10.77.26.0/24",
                serverIP = "10.77.26.1",
                dnsServers = listOf("1.1.1.1", "8.8.8.8", "208.67.222.222"),
                latency = 25,
                isConnected = false,
                countryCode = "FR",
                ping = 25,
                load = 45
            ),
            Server(
                id = "osaka",
                name = "Osaka",
                city = "Osaka",
                country = "Japan",
                flag = "🇯🇵",
                ip = "15.168.240.118",
                port = 51820,
                subnet = "10.77.27.0/24",
                serverIP = "10.77.27.1",
                dnsServers = listOf("1.1.1.1", "8.8.8.8", "208.67.222.222"),
                latency = 45,
                isConnected = false,
                countryCode = "JP",
                ping = 45,
                load = 35
            )
        )
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/health")
            val connection = url.openConnection() as HttpURLConnection
            
            // Set timeouts
            connection.connectTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
            connection.readTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()
            
            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val response = inputStream?.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: ""
            
            if (responseCode !in 200..299) {
                Log.e("ApiClient", "Health check failed: HTTP $responseCode - $response")
                false
            } else {
                Log.d("ApiClient", "Health check successful")
                true
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "Health check failed: ${e.message}")
            false
        }
    }
}
