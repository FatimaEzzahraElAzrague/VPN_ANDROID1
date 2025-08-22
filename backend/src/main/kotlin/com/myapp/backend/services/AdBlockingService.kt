package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.util.Logger
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class AdBlockingService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: AdBlockingService? = null
        
        fun getInstance(): AdBlockingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdBlockingService().also { INSTANCE = it }
            }
        }
    }
    
    // In-memory storage (replace with database in production)
    private val userConfigs = ConcurrentHashMap<String, AdBlockingConfig>()
    private val filterLists = ConcurrentHashMap<String, List<String>>()
    private val threatDatabase = ConcurrentHashMap<String, Boolean>()
    
    init {
        initializeDefaultFilterLists()
        initializeThreatDatabase()
    }
    
    private fun initializeDefaultFilterLists() {
        // Default ad blocking filter lists
        filterLists["easylist"] = listOf(
            "||ads.example.com^",
            "||tracker.example.com^",
            "||adserver.example.com^",
            "||banner.example.com^"
        )
        
        filterLists["malware"] = listOf(
            "||malware.example.com^",
            "||phishing.example.com^",
            "||scam.example.com^"
        )
        
        filterLists["social"] = listOf(
            "||facebook.com/tr^",
            "||google-analytics.com^",
            "||googletagmanager.com^"
        )
    }
    
    private fun initializeThreatDatabase() {
        // Initialize with known malicious domains
        val maliciousDomains = listOf(
            "malware.example.com",
            "phishing.example.com",
            "scam.example.com",
            "ads.example.com",
            "tracker.example.com"
        )
        
        maliciousDomains.forEach { domain ->
            threatDatabase[domain] = true
        }
    }
    
    fun getAdBlockingConfig(userId: String): AdBlockingConfig {
        return userConfigs[userId] ?: createDefaultConfig(userId)
    }
    
    fun updateAdBlockingConfig(userId: String, request: AdBlockingRequest): AdBlockingConfig {
        val currentConfig = getAdBlockingConfig(userId)
        
        val updatedConfig = currentConfig.copy(
            enabled = request.enabled,
            filterLists = request.filterLists ?: currentConfig.filterLists,
            customRules = request.customRules ?: currentConfig.customRules,
            whitelist = request.whitelist ?: currentConfig.whitelist,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Updated ad blocking config for user: $userId")
        
        return updatedConfig
    }
    
    fun checkUrl(userId: String, url: String): Boolean {
        val config = getAdBlockingConfig(userId)
        if (!config.enabled) return false
        
        // Check against filter lists
        if (isUrlBlocked(url, config)) {
            incrementStats(userId, "adsBlocked")
            Logger.info("Blocked ad/tracker URL: $url for user: $userId")
            return true
        }
        
        // Check whitelist
        if (config.whitelist.any { url.contains(it) }) {
            return false
        }
        
        return false
    }
    
    fun checkDomain(userId: String, domain: String): Boolean {
        val config = getAdBlockingConfig(userId)
        if (!config.enabled) return false
        
        // Check against threat database
        if (threatDatabase[domain] == true) {
            incrementStats(userId, "adsBlocked")
            Logger.info("Blocked malicious domain: $domain for user: $userId")
            return true
        }
        
        // Check custom rules
        if (config.customRules.any { rule -> domain.matches(rule.toRegex()) }) {
            incrementStats(userId, "adsBlocked")
            Logger.info("Blocked domain by custom rule: $domain for user: $userId")
            return true
        }
        
        return false
    }
    
    fun getFilterLists(): Map<String, List<String>> {
        return filterLists.toMap()
    }
    
    fun addCustomRule(userId: String, rule: String): AdBlockingConfig {
        val config = getAdBlockingConfig(userId)
        val updatedRules = config.customRules + rule
        
        val updatedConfig = config.copy(
            customRules = updatedRules,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Added custom rule for user: $userId: $rule")
        
        return updatedConfig
    }
    
    fun removeCustomRule(userId: String, rule: String): AdBlockingConfig {
        val config = getAdBlockingConfig(userId)
        val updatedRules = config.customRules - rule
        
        val updatedConfig = config.copy(
            customRules = updatedRules,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Removed custom rule for user: $userId: $rule")
        
        return updatedConfig
    }
    
    fun addToWhitelist(userId: String, domain: String): AdBlockingConfig {
        val config = getAdBlockingConfig(userId)
        val updatedWhitelist = config.whitelist + domain
        
        val updatedConfig = config.copy(
            whitelist = updatedWhitelist,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Added domain to whitelist for user: $userId: $domain")
        
        return updatedConfig
    }
    
    fun removeFromWhitelist(userId: String, domain: String): AdBlockingConfig {
        val config = getAdBlockingConfig(userId)
        val updatedWhitelist = config.whitelist - domain
        
        val updatedConfig = config.copy(
            whitelist = updatedWhitelist,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Removed domain from whitelist for user: $userId: $domain")
        
        return updatedConfig
    }
    
    fun getStats(userId: String): AdBlockingStats {
        val config = getAdBlockingConfig(userId)
        return config.stats
    }
    
    fun resetStats(userId: String): AdBlockingConfig {
        val config = getAdBlockingConfig(userId)
        val resetStats = AdBlockingStats(
            lastReset = LocalDateTime.now()
        )
        
        val updatedConfig = config.copy(
            stats = resetStats,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Reset stats for user: $userId")
        
        return updatedConfig
    }
    
    fun updateFilterLists(): Boolean {
        try {
            // In a real implementation, this would fetch updated filter lists from external sources
            // For now, we'll just log the action
            Logger.info("Updating filter lists from external sources")
            
            // Simulate updating filter lists
            Thread.sleep(1000)
            
            Logger.info("Filter lists updated successfully")
            return true
        } catch (e: Exception) {
            Logger.error("Failed to update filter lists: ${e.message}")
            return false
        }
    }
    
    private fun createDefaultConfig(userId: String): AdBlockingConfig {
        val defaultConfig = AdBlockingConfig(
            userId = userId,
            enabled = false,
            filterLists = listOf("easylist"),
            customRules = emptyList(),
            whitelist = emptyList()
        )
        
        userConfigs[userId] = defaultConfig
        Logger.info("Created default ad blocking config for user: $userId")
        
        return defaultConfig
    }
    
    private fun isUrlBlocked(url: String, config: AdBlockingConfig): Boolean {
        // Check against all filter lists
        config.filterLists.forEach { filterListName ->
            val rules = filterLists[filterListName] ?: emptyList()
            if (rules.any { rule -> url.matches(rule.toRegex()) }) {
                return true
            }
        }
        
        // Check custom rules
        if (config.customRules.any { rule -> url.matches(rule.toRegex()) }) {
            return true
        }
        
        return false
    }
    
    private fun incrementStats(userId: String, statType: String) {
        val config = getAdBlockingConfig(userId)
        val currentStats = config.stats
        
        val updatedStats = when (statType) {
            "adsBlocked" -> currentStats.copy(
                adsBlocked = currentStats.adsBlocked + 1,
                totalBlocked = currentStats.totalBlocked + 1
            )
            "trackersBlocked" -> currentStats.copy(
                trackersBlocked = currentStats.trackersBlocked + 1,
                totalBlocked = currentStats.totalBlocked + 1
            )
            else -> currentStats
        }
        
        val updatedConfig = config.copy(
            stats = updatedStats,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
    }
}
