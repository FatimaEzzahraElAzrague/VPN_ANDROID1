package com.example.v.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import android.util.Log

/**
 * API Client for VPN Features Backend
 * Handles communication with speed test, split tunneling, and kill switch endpoints
 */
class VPNFeaturesApiClient {
    
    companion object {
        private const val TAG = "VPNFeaturesAPI"
        private const val BASE_URL = "http://192.168.100.190:8080" // Android emulator
        // For physical device, use: "http://192.168.100.190:8080"
        
        @Volatile
        private var INSTANCE: VPNFeaturesApiClient? = null
        
        fun getInstance(): VPNFeaturesApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VPNFeaturesApiClient().also { INSTANCE = it }
            }
        }
    }
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                encodeDefaults = true
            })
        }
    }

    // Auto-Connect API
    suspend fun getAutoConnectSettings(token: String): Result<AutoConnectSettingsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("$BASE_URL/settings/auto-connect") {
                    header("Authorization", "Bearer $token")
                }
                Result.success(response.body())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun saveAutoConnectSettings(token: String, enabled: Boolean, mode: String): Result<AutoConnectSettingsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.post("$BASE_URL/settings/auto-connect") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("enabled" to enabled, "mode" to mode))
                }
                Result.success(response.body())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // Speed Test API Methods
    suspend fun getSpeedTestServers(): Result<List<SpeedTestServer>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔗 Requesting: $BASE_URL/speedtest/servers")
            val response = client.get("$BASE_URL/speedtest/servers")
            Log.d(TAG, "📡 Response status: ${response.status}")
            
            val data = response.body<Map<String, Any>>()
            Log.d(TAG, "📋 Response data: $data")
            
            val servers = data["servers"] as? List<Map<String, Any>> ?: emptyList()
            Log.d(TAG, "🖥️ Found ${servers.size} servers")
            
            Result.success(servers.map { server ->
                SpeedTestServer(
                    id = server["id"] as? String ?: "",
                    name = server["name"] as? String ?: "",
                    url = server["url"] as? String ?: "",
                    location = server["location"] as? String ?: "",
                    country = server["country"] as? String ?: "",
                    isActive = server["isActive"] as? Boolean ?: true,
                    priority = server["priority"] as? Int ?: 0
                )
            })
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting servers: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getSpeedTestConfig(userId: String): Result<SpeedTestConfig> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$BASE_URL/speedtest/config/$userId")
            val data = response.body<Map<String, Any>>()
            val config = data["config"] as? Map<String, Any> ?: emptyMap()
            
            Result.success(SpeedTestConfig(
                userId = config["userId"] as? String ?: userId,
                preferredServer = config["preferredServer"] as? String,
                uploadSizeBytes = config["uploadSizeBytes"] as? Int ?: 5_000_000,
                autoTestEnabled = config["autoTestEnabled"] as? Boolean ?: false,
                testIntervalMinutes = config["testIntervalMinutes"] as? Int ?: 60,
                saveResults = config["saveResults"] as? Boolean ?: true
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveSpeedTestResult(
        userId: String,
        pingMs: Long,
        downloadMbps: Double,
        uploadMbps: Double,
        testServer: String,
        networkType: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "pingMs" to pingMs,
                "downloadMbps" to downloadMbps,
                "uploadMbps" to uploadMbps,
                "testServer" to testServer,
                "networkType" to networkType
            )
            
            Log.d(TAG, "💾 Saving result: $requestBody")
            val response = client.post("$BASE_URL/speedtest/result/$userId") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            Log.d(TAG, "📡 Save response status: ${response.status}")
            
            val data = response.body<Map<String, Any>>()
            Log.d(TAG, "📋 Save response data: $data")
            
            val success = data["success"] as? Boolean ?: false
            Log.d(TAG, if (success) "✅ Result saved successfully!" else "❌ Failed to save result")
            
            Result.success(success)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving result: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Split Tunneling API Methods
    suspend fun getSplitTunnelingConfig(userId: String): Result<SplitTunnelingConfig> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$BASE_URL/splittunneling/config/$userId")
            val data = response.body<Map<String, Any>>()
            val config = data["config"] as? Map<String, Any> ?: emptyMap()
            
            Result.success(SplitTunnelingConfig(
                userId = config["userId"] as? String ?: userId,
                isEnabled = config["isEnabled"] as? Boolean ?: false,
                mode = when (config["mode"] as? String) {
                    "INCLUDE" -> SplitTunnelingMode.INCLUDE
                    "EXCLUDE" -> SplitTunnelingMode.EXCLUDE
                    else -> SplitTunnelingMode.EXCLUDE
                },
                appPackages = (config["appPackages"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSplitTunnelingConfig(
        userId: String,
        isEnabled: Boolean,
        mode: SplitTunnelingMode,
        appPackages: List<String>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "isEnabled" to isEnabled,
                "mode" to mode.name,
                "appPackages" to appPackages
            )
            
            val response = client.put("$BASE_URL/splittunneling/config/$userId") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            
            val data = response.body<Map<String, Any>>()
            Result.success(data["success"] as? Boolean ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Kill Switch API Methods
    suspend fun getKillSwitchConfig(userId: String): Result<KillSwitchConfig> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$BASE_URL/killswitch/config/$userId")
            val data = response.body<Map<String, Any>>()
            val config = data["settings"] as? Map<String, Any> ?: emptyMap()
            
            Result.success(KillSwitchConfig(
                userId = config["userId"] as? String ?: userId,
                isEnabled = config["isEnabled"] as? Boolean ?: false,
                autoReconnectEnabled = config["autoReconnectEnabled"] as? Boolean ?: true,
                maxReconnectionAttempts = config["maxReconnectionAttempts"] as? Int ?: 3,
                connectionCheckIntervalMs = config["connectionCheckIntervalMs"] as? Long ?: 5000L,
                connectionTimeoutMs = config["connectionTimeoutMs"] as? Long ?: 10000L,
                notifyOnKillSwitch = config["notifyOnKillSwitch"] as? Boolean ?: true,
                blockAllTraffic = config["blockAllTraffic"] as? Boolean ?: true
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateKillSwitchConfig(
        userId: String,
        isEnabled: Boolean,
        autoReconnectEnabled: Boolean = true,
        maxReconnectionAttempts: Int = 3
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "isEnabled" to isEnabled,
                "autoReconnectEnabled" to autoReconnectEnabled,
                "maxReconnectionAttempts" to maxReconnectionAttempts
            )
            
            val response = client.put("$BASE_URL/killswitch/config/$userId") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            
            val data = response.body<Map<String, Any>>()
            Result.success(data["success"] as? Boolean ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        client.close()
    }
}

// Data Classes for API responses
data class SpeedTestServer(
    val id: String,
    val name: String,
    val url: String,
    val location: String,
    val country: String,
    val isActive: Boolean,
    val priority: Int
)

data class SpeedTestConfig(
    val userId: String,
    val preferredServer: String?,
    val uploadSizeBytes: Int,
    val autoTestEnabled: Boolean,
    val testIntervalMinutes: Int,
    val saveResults: Boolean
)

data class SplitTunnelingConfig(
    val userId: String,
    val isEnabled: Boolean,
    val mode: SplitTunnelingMode,
    val appPackages: List<String>
)

enum class SplitTunnelingMode {
    INCLUDE, EXCLUDE
}

data class KillSwitchConfig(
    val userId: String,
    val isEnabled: Boolean,
    val autoReconnectEnabled: Boolean,
    val maxReconnectionAttempts: Int,
    val connectionCheckIntervalMs: Long,
    val connectionTimeoutMs: Long,
    val notifyOnKillSwitch: Boolean,
    val blockAllTraffic: Boolean
)

// Auto-Connect response DTO
@kotlinx.serialization.Serializable
data class AutoConnectSettingsResponse(
    val enabled: Boolean,
    val mode: String
)
