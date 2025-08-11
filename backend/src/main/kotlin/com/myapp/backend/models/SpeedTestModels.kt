package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Speed Test Result Model
 * Represents a completed speed test result
 */
@Serializable
data class SpeedTestResult(
    val id: String? = null,
    val userId: String,
    val pingMs: Long,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val jitterMs: Long? = null,
    val testServer: String,
    val deviceInfo: DeviceInfo? = null,
    val networkType: String? = null,
    val timestamp: String = LocalDateTime.now().toString()
)

/**
 * Speed Test Request Model
 * For initiating speed tests
 */
@Serializable
data class SpeedTestRequest(
    val testServer: String? = null,
    val uploadSizeBytes: Int = 5_000_000,
    val deviceInfo: DeviceInfo? = null
)

/**
 * Speed Test Response Model
 */
@Serializable
data class SpeedTestResponse(
    val success: Boolean,
    val message: String,
    val result: SpeedTestResult? = null,
    val error: String? = null
)

/**
 * Speed Test Analytics Model
 * Aggregated statistics for speed test performance
 */
@Serializable
data class SpeedTestAnalytics(
    val userId: String,
    val totalTests: Int = 0,
    val averagePingMs: Double = 0.0,
    val averageDownloadMbps: Double = 0.0,
    val averageUploadMbps: Double = 0.0,
    val bestPingMs: Long? = null,
    val bestDownloadMbps: Double? = null,
    val bestUploadMbps: Double? = null,
    val lastTestDate: String? = null,
    val period: AnalyticsPeriod = AnalyticsPeriod.DAY
)

/**
 * Speed Test Server Model
 * Available speed test servers
 */
@Serializable
data class SpeedTestServer(
    val id: String,
    val name: String,
    val url: String,
    val location: String,
    val country: String,
    val isActive: Boolean = true,
    val priority: Int = 0
)

/**
 * Speed Test Configuration Model
 * User-specific speed test settings
 */
@Serializable
data class SpeedTestConfig(
    val userId: String,
    val preferredServer: String? = null,
    val uploadSizeBytes: Int = 5_000_000,
    val autoTestEnabled: Boolean = false,
    val testIntervalMinutes: Int = 60,
    val saveResults: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

/**
 * Speed Test Configuration Request
 */
@Serializable
data class SpeedTestConfigRequest(
    val preferredServer: String? = null,
    val uploadSizeBytes: Int? = null,
    val autoTestEnabled: Boolean? = null,
    val testIntervalMinutes: Int? = null,
    val saveResults: Boolean? = null
)

/**
 * Speed Test Configuration Response
 */
@Serializable
data class SpeedTestConfigResponse(
    val config: SpeedTestConfig,
    val availableServers: List<SpeedTestServer> = emptyList(),
    val analytics: SpeedTestAnalytics? = null
)
