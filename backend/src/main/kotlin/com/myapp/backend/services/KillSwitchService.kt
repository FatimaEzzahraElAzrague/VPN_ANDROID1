package com.myapp.backend.services

import com.myapp.backend.models.*
import java.time.LocalDateTime
import java.util.*

/**
 * Kill Switch Service
 * Handles kill switch settings, events, and analytics on the backend
 */
class KillSwitchService {
    
    companion object {
        private const val TAG = "KillSwitchService"
        
        @Volatile
        private var INSTANCE: KillSwitchService? = null
        
        fun getInstance(): KillSwitchService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KillSwitchService().also { INSTANCE = it }
            }
        }
    }
    
    // In-memory storage (in production, this would be database)
    private val killSwitchSettings = mutableMapOf<String, KillSwitchSettings>()
    private val killSwitchEvents = mutableMapOf<String, MutableList<KillSwitchEvent>>()
    private val killSwitchPolicies = mutableMapOf<String, KillSwitchPolicy>()
    private val killSwitchAnalytics = mutableMapOf<String, KillSwitchAnalytics>()
    
    init {
        // Initialize default policies
        initializeDefaultPolicies()
    }
    
    /**
     * Get kill switch settings for a user
     */
    fun getKillSwitchSettings(userId: String): KillSwitchSettings {
        return killSwitchSettings[userId] ?: createDefaultSettings(userId)
    }
    
    /**
     * Update kill switch settings for a user
     */
    fun updateKillSwitchSettings(userId: String, request: KillSwitchConfigRequest): KillSwitchSettings {
        val currentSettings = getKillSwitchSettings(userId)
        
        val updatedSettings = currentSettings.copy(
            isEnabled = request.isEnabled ?: currentSettings.isEnabled,
            autoReconnectEnabled = request.autoReconnectEnabled ?: currentSettings.autoReconnectEnabled,
            maxReconnectionAttempts = request.maxReconnectionAttempts ?: currentSettings.maxReconnectionAttempts,
            connectionCheckIntervalMs = request.connectionCheckIntervalMs ?: currentSettings.connectionCheckIntervalMs,
            connectionTimeoutMs = request.connectionTimeoutMs ?: currentSettings.connectionTimeoutMs,
            notifyOnKillSwitch = request.notifyOnKillSwitch ?: currentSettings.notifyOnKillSwitch,
            blockAllTraffic = request.blockAllTraffic ?: currentSettings.blockAllTraffic,
            updatedAt = LocalDateTime.now().toString()
        )
        
        killSwitchSettings[userId] = updatedSettings
        
        // Log settings update event
        logKillSwitchEvent(userId, KillSwitchEventType.SETTINGS_UPDATED, "Settings updated")
        
        return updatedSettings
    }
    
    /**
     * Log a kill switch event
     */
    fun logKillSwitchEvent(
        userId: String,
        eventType: KillSwitchEventType,
        reason: String? = null,
        serverEndpoint: String? = null,
        deviceInfo: DeviceInfo? = null
    ): KillSwitchEvent {
        val event = KillSwitchEvent(
            id = UUID.randomUUID().toString(),
            userId = userId,
            eventType = eventType,
            reason = reason,
            serverEndpoint = serverEndpoint,
            deviceInfo = deviceInfo,
            timestamp = LocalDateTime.now().toString()
        )
        
        // Store event
        if (!killSwitchEvents.containsKey(userId)) {
            killSwitchEvents[userId] = mutableListOf()
        }
        killSwitchEvents[userId]!!.add(event)
        
        // Update analytics
        updateAnalytics(userId, event)
        
        return event
    }
    
    /**
     * Get kill switch analytics for a user
     */
    fun getKillSwitchAnalytics(userId: String, period: AnalyticsPeriod = AnalyticsPeriod.DAY): KillSwitchAnalytics {
        return killSwitchAnalytics[userId] ?: KillSwitchAnalytics(userId = userId, period = period)
    }
    
    /**
     * Get kill switch policy for a user
     */
    fun getKillSwitchPolicy(userId: String): KillSwitchPolicy? {
        // In a real implementation, you would determine the policy based on user groups, subscription, etc.
        return killSwitchPolicies["default"] ?: createDefaultPolicy()
    }
    
    /**
     * Get kill switch configuration with settings, policy, and analytics
     */
    fun getKillSwitchConfiguration(userId: String): KillSwitchConfigResponse {
        val settings = getKillSwitchSettings(userId)
        val policy = getKillSwitchPolicy(userId)
        val analytics = getKillSwitchAnalytics(userId)
        
        return KillSwitchConfigResponse(
            settings = settings,
            policy = policy,
            analytics = analytics
        )
    }
    
    /**
     * Get recent kill switch events for a user
     */
    fun getRecentKillSwitchEvents(userId: String, limit: Int = 10): List<KillSwitchEvent> {
        return killSwitchEvents[userId]?.takeLast(limit)?.reversed() ?: emptyList()
    }
    
    /**
     * Check if kill switch is allowed for a user based on policy
     */
    fun isKillSwitchAllowed(userId: String, reason: String): Boolean {
        val policy = getKillSwitchPolicy(userId) ?: return true
        
        if (!policy.isEnabled) return false
        
        // Check if reason is blocked
        if (policy.blockedReasons.contains(reason)) return false
        
        // Check if reason is explicitly allowed
        if (policy.allowedReasons.isNotEmpty() && !policy.allowedReasons.contains(reason)) return false
        
        return true
    }
    
    /**
     * Get kill switch statistics for admin dashboard
     */
    fun getKillSwitchStatistics(): Map<String, Any> {
        val totalUsers = killSwitchSettings.size
        val enabledUsers = killSwitchSettings.values.count { it.isEnabled }
        val totalEvents = killSwitchEvents.values.sumOf { it.size }
        val totalActivations = killSwitchEvents.values.flatten().count { it.eventType == KillSwitchEventType.ACTIVATED }
        val totalDeactivations = killSwitchEvents.values.flatten().count { it.eventType == KillSwitchEventType.DEACTIVATED }
        
        return mapOf(
            "totalUsers" to totalUsers,
            "enabledUsers" to enabledUsers,
            "enabledPercentage" to if (totalUsers > 0) (enabledUsers * 100.0 / totalUsers) else 0.0,
            "totalEvents" to totalEvents,
            "totalActivations" to totalActivations,
            "totalDeactivations" to totalDeactivations,
            "successRate" to if (totalActivations > 0) (totalDeactivations * 100.0 / totalActivations) else 0.0
        )
    }
    
    /**
     * Create default kill switch settings for a user
     */
    private fun createDefaultSettings(userId: String): KillSwitchSettings {
        val defaultSettings = KillSwitchSettings(userId = userId)
        killSwitchSettings[userId] = defaultSettings
        return defaultSettings
    }
    
    /**
     * Create default kill switch policy
     */
    private fun createDefaultPolicy(): KillSwitchPolicy {
        val defaultPolicy = KillSwitchPolicy(
            id = "default",
            name = "Default Kill Switch Policy",
            description = "Default policy for all users",
            isEnabled = true,
            maxReconnectionAttempts = 3,
            connectionCheckIntervalMs = 5000L,
            connectionTimeoutMs = 10000L,
            allowedReasons = listOf(
                "VPN connection lost",
                "Network connection lost",
                "VPN transport lost",
                "Connection check failed",
                "Manual activation by user"
            ),
            blockedReasons = emptyList(),
            userGroups = listOf("all")
        )
        
        killSwitchPolicies["default"] = defaultPolicy
        return defaultPolicy
    }
    
    /**
     * Initialize default policies
     */
    private fun initializeDefaultPolicies() {
        // Default policy
        createDefaultPolicy()
        
        // Premium policy
        val premiumPolicy = KillSwitchPolicy(
            id = "premium",
            name = "Premium Kill Switch Policy",
            description = "Enhanced policy for premium users",
            isEnabled = true,
            maxReconnectionAttempts = 5,
            connectionCheckIntervalMs = 3000L,
            connectionTimeoutMs = 8000L,
            allowedReasons = listOf(
                "VPN connection lost",
                "Network connection lost",
                "VPN transport lost",
                "Connection check failed",
                "Manual activation by user",
                "Server maintenance",
                "Geographic restrictions"
            ),
            blockedReasons = emptyList(),
            userGroups = listOf("premium")
        )
        
        killSwitchPolicies["premium"] = premiumPolicy
        
        // Enterprise policy
        val enterprisePolicy = KillSwitchPolicy(
            id = "enterprise",
            name = "Enterprise Kill Switch Policy",
            description = "Strict policy for enterprise users",
            isEnabled = true,
            maxReconnectionAttempts = 10,
            connectionCheckIntervalMs = 2000L,
            connectionTimeoutMs = 5000L,
            allowedReasons = listOf(
                "VPN connection lost",
                "Network connection lost",
                "VPN transport lost",
                "Connection check failed",
                "Manual activation by user",
                "Server maintenance",
                "Geographic restrictions",
                "Security breach detected"
            ),
            blockedReasons = listOf(
                "User disabled kill switch",
                "Policy violation"
            ),
            userGroups = listOf("enterprise")
        )
        
        killSwitchPolicies["enterprise"] = enterprisePolicy
    }
    
    /**
     * Update analytics based on new event
     */
    private fun updateAnalytics(userId: String, event: KillSwitchEvent) {
        val currentAnalytics = killSwitchAnalytics[userId] ?: KillSwitchAnalytics(userId = userId)
        
        val updatedAnalytics = when (event.eventType) {
            KillSwitchEventType.ACTIVATED -> {
                currentAnalytics.copy(
                    totalActivations = currentAnalytics.totalActivations + 1,
                    lastActivation = event.timestamp
                )
            }
            KillSwitchEventType.DEACTIVATED -> {
                currentAnalytics.copy(
                    totalDeactivations = currentAnalytics.totalDeactivations + 1,
                    lastDeactivation = event.timestamp
                )
            }
            KillSwitchEventType.RECONNECTION_ATTEMPT -> {
                currentAnalytics.copy(
                    totalReconnectionAttempts = currentAnalytics.totalReconnectionAttempts + 1
                )
            }
            KillSwitchEventType.RECONNECTION_SUCCESS -> {
                currentAnalytics.copy(
                    successfulReconnections = currentAnalytics.successfulReconnections + 1
                )
            }
            KillSwitchEventType.RECONNECTION_FAILED -> {
                currentAnalytics.copy(
                    failedReconnections = currentAnalytics.failedReconnections + 1
                )
            }
            KillSwitchEventType.SETTINGS_UPDATED -> {
                currentAnalytics // No change for settings updates
            }
        }
        
        killSwitchAnalytics[userId] = updatedAnalytics
    }
    
    /**
     * Clean up old events (keep only last 1000 events per user)
     */
    fun cleanupOldEvents() {
        killSwitchEvents.forEach { (userId, events) ->
            if (events.size > 1000) {
                killSwitchEvents[userId] = events.takeLast(1000).toMutableList()
            }
        }
    }
}
