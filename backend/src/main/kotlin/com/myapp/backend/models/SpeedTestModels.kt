package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class SpeedTestRequest(
    val serverId: String,
    val pingMs: Long,
    val downloadMbps: Double,
    val uploadMbps: Double
)

@Serializable
data class SpeedTestResponse(
    val success: Boolean,
    val message: String
)



@Serializable
data class SpeedTestServer(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val location: String,
    val country: String,
    val ip: String = "", // Android app expects this field
    val isActive: Boolean = true,
    val priority: Int = 1
)

@Serializable
data class SpeedTestConfig(
    val userId: String,
    val preferredServer: String? = null,
    val autoTestEnabled: Boolean = false,
    val testIntervalMinutes: Int = 60,
    val saveResults: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class SpeedTestConfigRequest(
    val preferredServer: String? = null,
    val autoTestEnabled: Boolean? = null,
    val testIntervalMinutes: Int? = null,
    val saveResults: Boolean? = null
)

@Serializable
data class SpeedTestConfigResponse(
    val config: SpeedTestConfig,
    val availableServers: List<SpeedTestServer> = emptyList()
)

@Serializable
data class SpeedTestDownloadResponse(
    val success: Boolean,
    val message: String,
    val sizeBytes: Long,
    val chunkSize: Int
)

@Serializable
data class SpeedTestUploadResponse(
    val success: Boolean,
    val message: String,
    val sizeBytes: Long,
    val uploadTimeMs: Long,
    val uploadSpeedMbps: Double
)

enum class AnalyticsPeriod {
    DAY, WEEK, MONTH, YEAR
}
