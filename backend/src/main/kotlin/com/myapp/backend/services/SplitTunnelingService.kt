package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.db.DatabaseFactory
import com.myapp.backend.util.Logger
import java.time.LocalDateTime
import java.util.*

/**
 * Split Tunneling Service
 * Manages split tunneling configurations and operations
 */
class SplitTunnelingService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: SplitTunnelingService? = null
        
        fun getInstance(): SplitTunnelingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SplitTunnelingService().also { INSTANCE = it }
            }
        }
    }
    
    private val logger = Logger.getLogger(SplitTunnelingService::class.java)
    
    /**
     * Get split tunneling configuration for a user
     */
    suspend fun getSplitTunnelingConfig(userId: String): SplitTunnelingConfigResponse {
        return try {
            logger.info("Getting split tunneling config for user: $userId")
            
            // For now, return a basic configuration
            // TODO: Implement database integration
            val config = SplitTunnelingConfig(
                userId = userId,
                isEnabled = false,
                mode = SplitTunnelingMode.EXCLUDE,
                appPackages = emptyList()
            )
            
            val availableApps = getAvailableApps()
            val presets = getDefaultPresets()
            
            SplitTunnelingConfigResponse(
                config = config,
                availableApps = availableApps,
                presets = presets
            )
        } catch (e: Exception) {
            logger.error("Error getting split tunneling config for user $userId", e)
            throw e
        }
    }
    
    /**
     * Update split tunneling configuration for a user
     */
    suspend fun updateSplitTunnelingConfig(
        userId: String, 
        request: SplitTunnelingRequest
    ): SplitTunnelingConfig {
        return try {
            logger.info("Updating split tunneling config for user: $userId")
            
            // For now, create a new configuration
            // TODO: Implement database persistence
            val config = SplitTunnelingConfig(
                userId = userId,
                isEnabled = request.isEnabled ?: false,
                mode = request.mode ?: SplitTunnelingMode.EXCLUDE,
                appPackages = request.appPackages ?: emptyList(),
                updatedAt = LocalDateTime.now().toString()
            )
            
            logger.info("Updated split tunneling config: $config")
            config
        } catch (e: Exception) {
            logger.error("Error updating split tunneling config for user $userId", e)
            throw e
        }
    }
    
    /**
     * Get available apps for split tunneling
     */
    private suspend fun getAvailableApps(): List<AppPackage> {
        // TODO: Implement app discovery from device
        return listOf(
            AppPackage(
                packageName = "com.android.chrome",
                appName = "Chrome",
                isSystemApp = false,
                category = "Browser"
            ),
            AppPackage(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                isSystemApp = false,
                category = "Communication"
            ),
            AppPackage(
                packageName = "com.spotify.music",
                appName = "Spotify",
                isSystemApp = false,
                category = "Entertainment"
            ),
            AppPackage(
                packageName = "com.netflix.mediaclient",
                appName = "Netflix",
                isSystemApp = false,
                category = "Entertainment"
            ),
            AppPackage(
                packageName = "com.ubercab",
                appName = "Uber",
                isSystemApp = false,
                category = "Transportation"
            )
        )
    }
    
    /**
     * Get default split tunneling presets
     */
    suspend fun getDefaultPresets(): List<SplitTunnelingPreset> {
        return listOf(
            SplitTunnelingPreset(
                id = "banking",
                name = "Banking Apps Only",
                description = "Only banking and financial apps use VPN",
                mode = SplitTunnelingMode.INCLUDE,
                appPackages = listOf(
                    "com.chase.smartphone",
                    "com.wellsfargo.mobile",
                    "com.bankofamerica.mobile",
                    "com.citibank.mobile"
                ),
                category = "Security"
            ),
            SplitTunnelingPreset(
                id = "streaming",
                name = "Exclude Streaming",
                description = "All apps use VPN except streaming apps",
                mode = SplitTunnelingMode.EXCLUDE,
                appPackages = listOf(
                    "com.netflix.mediaclient",
                    "com.spotify.music",
                    "com.amazon.avod.thirdpartyclient",
                    "com.hulu.plus"
                ),
                category = "Entertainment"
            ),
            SplitTunnelingPreset(
                id = "gaming",
                name = "Exclude Gaming",
                description = "All apps use VPN except gaming apps for better latency",
                mode = SplitTunnelingMode.EXCLUDE,
                appPackages = listOf(
                    "com.activision.callofduty.shooter",
                    "com.epicgames.fortnite",
                    "com.tencent.ig",
                    "com.roblox.client"
                ),
                category = "Gaming"
            ),
            SplitTunnelingPreset(
                id = "work",
                name = "Work Apps Only",
                description = "Only work-related apps use VPN",
                mode = SplitTunnelingMode.INCLUDE,
                appPackages = listOf(
                    "com.microsoft.teams",
                    "com.slack",
                    "com.zoom.us",
                    "com.google.android.apps.docs"
                ),
                category = "Productivity"
            )
        )
    }
    
    /**
     * Apply a preset to user's configuration
     */
    suspend fun applyPreset(userId: String, presetId: String): SplitTunnelingConfig {
        return try {
            logger.info("Applying preset $presetId for user: $userId")
            
            val presets = getDefaultPresets()
            val preset = presets.find { it.id == presetId }
                ?: throw IllegalArgumentException("Preset not found: $presetId")
            
            val config = SplitTunnelingConfig(
                userId = userId,
                isEnabled = true,
                mode = preset.mode,
                appPackages = preset.appPackages,
                updatedAt = LocalDateTime.now().toString()
            )
            
            logger.info("Applied preset $presetId: $config")
            config
        } catch (e: Exception) {
            logger.error("Error applying preset $presetId for user $userId", e)
            throw e
        }
    }
    
    /**
     * Get split tunneling analytics for a user
     */
    suspend fun getSplitTunnelingAnalytics(
        userId: String, 
        period: AnalyticsPeriod
    ): SplitTunnelingAnalytics {
        return try {
            logger.info("Getting split tunneling analytics for user: $userId, period: $period")
            
            // TODO: Implement actual analytics calculation
            SplitTunnelingAnalytics(
                userId = userId,
                totalConfigurations = 1,
                includeModeUsage = 0,
                excludeModeUsage = 1,
                mostUsedApps = emptyList(),
                lastConfiguration = LocalDateTime.now().toString(),
                period = period
            )
        } catch (e: Exception) {
            logger.error("Error getting split tunneling analytics for user $userId", e)
            throw e
        }
    }
    
    /**
     * Validate app packages
     */
    suspend fun validateAppPackages(packages: List<String>): List<String> {
        return try {
            logger.info("Validating app packages: $packages")
            
            // TODO: Implement actual app validation
            // For now, just return the packages as-is
            packages
        } catch (e: Exception) {
            logger.error("Error validating app packages", e)
            emptyList()
        }
    }
    
    /**
     * Get popular apps for split tunneling
     */
    suspend fun getPopularApps(): List<AppPackage> {
        return try {
            logger.info("Getting popular apps for split tunneling")
            
            // TODO: Implement popularity ranking
            getAvailableApps().take(10)
        } catch (e: Exception) {
            logger.error("Error getting popular apps", e)
            emptyList()
        }
    }
    
    /**
     * Search apps by name or package
     */
    suspend fun searchApps(query: String): List<AppPackage> {
        return try {
            logger.info("Searching apps with query: $query")
            
            val allApps = getAvailableApps()
            allApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true) ||
                app.category?.contains(query, ignoreCase = true) == true
            }
        } catch (e: Exception) {
            logger.error("Error searching apps with query: $query", e)
            emptyList()
        }
    }
}
