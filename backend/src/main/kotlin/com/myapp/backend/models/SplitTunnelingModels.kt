package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Split Tunneling Configuration Model
 * Represents user's split tunneling configuration
 */
@Serializable
data class SplitTunnelingConfig(
    val userId: String,
    val isEnabled: Boolean = false,
    val mode: SplitTunnelingMode = SplitTunnelingMode.EXCLUDE,
    val appPackages: List<String> = emptyList(),
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

/**
 * Split Tunneling Mode
 */
@Serializable
enum class SplitTunnelingMode {
    INCLUDE,  // Only selected apps use VPN
    EXCLUDE   // All apps use VPN except selected ones
}

/**
 * Split Tunneling Request Model
 */
@Serializable
data class SplitTunnelingRequest(
    val isEnabled: Boolean? = null,
    val mode: SplitTunnelingMode? = null,
    val appPackages: List<String>? = null
)

/**
 * Split Tunneling Response Model
 */
@Serializable
data class SplitTunnelingResponse(
    val success: Boolean,
    val message: String,
    val config: SplitTunnelingConfig? = null,
    val error: String? = null
)

/**
 * App Package Model
 * Represents an app that can be included/excluded from VPN
 */
@Serializable
data class AppPackage(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val category: String? = null,
    val icon: String? = null
)

/**
 * Split Tunneling Analytics Model
 * Tracks split tunneling usage
 */
@Serializable
data class SplitTunnelingAnalytics(
    val userId: String,
    val totalConfigurations: Int = 0,
    val includeModeUsage: Int = 0,
    val excludeModeUsage: Int = 0,
    val mostUsedApps: List<String> = emptyList(),
    val lastConfiguration: String? = null,
    val period: AnalyticsPeriod = AnalyticsPeriod.DAY
)

/**
 * Split Tunneling Preset Model
 * Predefined split tunneling configurations
 */
@Serializable
data class SplitTunnelingPreset(
    val id: String,
    val name: String,
    val description: String,
    val mode: SplitTunnelingMode,
    val appPackages: List<String>,
    val category: String,
    val isActive: Boolean = true
)

/**
 * Split Tunneling Configuration Response
 */
@Serializable
data class SplitTunnelingConfigResponse(
    val config: SplitTunnelingConfig,
    val availableApps: List<AppPackage> = emptyList(),
    val presets: List<SplitTunnelingPreset> = emptyList(),
    val analytics: SplitTunnelingAnalytics? = null
)
