package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Kill Switch Settings Model
 * Represents user's kill switch configuration
 */
@Serializable
data class KillSwitchSettings(
    val userId: String,
    val isEnabled: Boolean = false,
    val autoReconnectEnabled: Boolean = true,
    val maxReconnectionAttempts: Int = 3,
    val connectionCheckIntervalMs: Long = 5000L,
    val connectionTimeoutMs: Long = 10000L,
    val notifyOnKillSwitch: Boolean = true,
    val blockAllTraffic: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

/**
 * Kill Switch Event Model
 * Represents kill switch activation/deactivation events
 */
@Serializable
data class KillSwitchEvent(
    val id: String? = null,
    val userId: String,
    val eventType: KillSwitchEventType,
    val reason: String? = null,
    val serverEndpoint: String? = null,
    val deviceInfo: DeviceInfo? = null,
    val timestamp: String = LocalDateTime.now().toString()
)

/**
 * Kill Switch Event Types
 */
@Serializable
enum class KillSwitchEventType {
    ACTIVATED,
    DEACTIVATED,
    RECONNECTION_ATTEMPT,
    RECONNECTION_SUCCESS,
    RECONNECTION_FAILED,
    SETTINGS_UPDATED
}

/**
 * Device Information
 */
@Serializable
data class DeviceInfo(
    val deviceId: String,
    val deviceModel: String? = null,
    val androidVersion: String? = null,
    val appVersion: String? = null,
    val networkType: String? = null
)

/**
 * Kill Switch Analytics Model
 * Aggregated statistics for kill switch usage
 */
@Serializable
data class KillSwitchAnalytics(
    val userId: String,
    val totalActivations: Int = 0,
    val totalDeactivations: Int = 0,
    val totalReconnectionAttempts: Int = 0,
    val successfulReconnections: Int = 0,
    val failedReconnections: Int = 0,
    val averageReconnectionTimeMs: Long = 0L,
    val mostCommonReason: String? = null,
    val lastActivation: String? = null,
    val lastDeactivation: String? = null,
    val period: AnalyticsPeriod = AnalyticsPeriod.DAY
)

/**
 * Analytics Period
 */
@Serializable
enum class AnalyticsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

/**
 * Kill Switch Policy Model
 * Server-side policies for kill switch behavior
 */
@Serializable
data class KillSwitchPolicy(
    val id: String? = null,
    val name: String,
    val description: String,
    val isEnabled: Boolean = true,
    val maxReconnectionAttempts: Int = 3,
    val connectionCheckIntervalMs: Long = 5000L,
    val connectionTimeoutMs: Long = 10000L,
    val allowedReasons: List<String> = emptyList(),
    val blockedReasons: List<String> = emptyList(),
    val userGroups: List<String> = emptyList(), // For targeting specific user groups
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

/**
 * Kill Switch Configuration Request
 */
@Serializable
data class KillSwitchConfigRequest(
    val isEnabled: Boolean? = null,
    val autoReconnectEnabled: Boolean? = null,
    val maxReconnectionAttempts: Int? = null,
    val connectionCheckIntervalMs: Long? = null,
    val connectionTimeoutMs: Long? = null,
    val notifyOnKillSwitch: Boolean? = null,
    val blockAllTraffic: Boolean? = null
)

/**
 * Kill Switch Configuration Response
 */
@Serializable
data class KillSwitchConfigResponse(
    val settings: KillSwitchSettings,
    val policy: KillSwitchPolicy? = null,
    val analytics: KillSwitchAnalytics? = null
)

/**
 * Kill Switch Event Request
 */
@Serializable
data class KillSwitchEventRequest(
    val eventType: KillSwitchEventType,
    val reason: String? = null,
    val serverEndpoint: String? = null,
    val deviceInfo: DeviceInfo? = null
)

/**
 * Kill Switch Event Response
 */
@Serializable
data class KillSwitchEventResponse(
    val success: Boolean,
    val message: String,
    val eventId: String? = null
)
