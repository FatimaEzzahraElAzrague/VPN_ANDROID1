package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.util.Logger
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

class FamilyModeService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: FamilyModeService? = null
        
        fun getInstance(): FamilyModeService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FamilyModeService().also { INSTANCE = it }
            }
        }
    }
    
    // In-memory storage (replace with database in production)
    private val userConfigs = ConcurrentHashMap<String, FamilyModeConfig>()
    private val contentCategories = ConcurrentHashMap<String, ContentCategory>()
    private val blockedUrls = ConcurrentHashMap<String, List<String>>()
    private val safeSearchEngines = ConcurrentHashMap<String, Boolean>()
    
    init {
        initializeContentCategories()
        initializeBlockedUrls()
        initializeSafeSearchEngines()
    }
    
    private fun initializeContentCategories() {
        val categories = listOf(
            ContentCategory("adult", "Adult Content", "Inappropriate content for minors", true, ContentSeverity.CRITICAL),
            ContentCategory("violence", "Violence", "Violent or graphic content", true, ContentSeverity.HIGH),
            ContentCategory("gambling", "Gambling", "Online gambling and betting sites", true, ContentSeverity.HIGH),
            ContentCategory("drugs", "Drugs", "Illegal drugs and substances", true, ContentSeverity.CRITICAL),
            ContentCategory("weapons", "Weapons", "Weapons and ammunition", true, ContentSeverity.HIGH),
            ContentCategory("social", "Social Media", "Social networking platforms", false, ContentSeverity.LOW),
            ContentCategory("gaming", "Gaming", "Online gaming platforms", false, ContentSeverity.LOW),
            ContentCategory("shopping", "Shopping", "Online shopping and e-commerce", false, ContentSeverity.LOW)
        )
        
        categories.forEach { category ->
            contentCategories[category.id] = category
        }
    }
    
    private fun initializeBlockedUrls() {
        val blocked = mapOf(
            "adult" to listOf(
                "pornhub.com", "xvideos.com", "redtube.com", "youporn.com",
                "adultfriendfinder.com", "ashleymadison.com"
            ),
            "violence" to listOf(
                "liveleak.com", "bestgore.com", "documentingreality.com"
            ),
            "gambling" to listOf(
                "bet365.com", "pokerstars.com", "888casino.com", "williamhill.com"
            ),
            "drugs" to listOf(
                "silkroad.com", "alphabay.com", "dreammarket.com"
            ),
            "weapons" to listOf(
                "armslist.com", "gunbroker.com", "gunsamerica.com"
            )
        )
        
        blockedUrls.putAll(blocked)
    }
    
    private fun initializeSafeSearchEngines() {
        val engines = mapOf(
            "google.com" to true,
            "bing.com" to true,
            "yahoo.com" to true,
            "duckduckgo.com" to true,
            "baidu.com" to false,
            "yandex.com" to false
        )
        
        safeSearchEngines.putAll(engines)
    }
    
    fun getFamilyModeConfig(userId: String): FamilyModeConfig {
        return userConfigs[userId] ?: createDefaultConfig(userId)
    }
    
    fun updateFamilyModeConfig(userId: String, request: FamilyModeRequest): FamilyModeConfig {
        val currentConfig = getFamilyModeConfig(userId)
        
        val updatedContentCategories = if (request.contentCategories != null) {
            currentConfig.contentCategories.map { category ->
                category.copy(blocked = request.contentCategories.contains(category.id))
            }
        } else {
            currentConfig.contentCategories
        }
        
        val updatedConfig = currentConfig.copy(
            enabled = request.enabled,
            contentCategories = updatedContentCategories,
            timeRestrictions = request.timeRestrictions ?: currentConfig.timeRestrictions,
            safeSearch = request.safeSearch ?: currentConfig.safeSearch,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Updated family mode config for user: $userId")
        
        return updatedConfig
    }
    
    fun checkUrl(userId: String, url: String): Boolean {
        val config = getFamilyModeConfig(userId)
        if (!config.enabled) return false
        
        val domain = extractDomain(url)
        
        // Check content categories
        config.contentCategories.forEach { category ->
            if (category.blocked) {
                val blockedDomains = blockedUrls[category.id] ?: emptyList()
                if (blockedDomains.any { domain.contains(it) }) {
                    incrementStats(userId, "contentBlocked")
                    Logger.info("Blocked inappropriate content: $url for user: $userId (category: ${category.name})")
                    return true
                }
            }
        }
        
        return false
    }
    
    fun checkTimeRestrictions(userId: String): Boolean {
        val config = getFamilyModeConfig(userId)
        if (!config.enabled || config.timeRestrictions?.enabled != true) return false
        
        val restrictions = config.timeRestrictions!!
        val currentTime = LocalTime.now()
        
        // Check bedtime
        if (restrictions.bedtime != null) {
            val bedtime = LocalTime.parse(restrictions.bedtime, DateTimeFormatter.ofPattern("HH:mm"))
            if (currentTime.isAfter(bedtime) || currentTime.isBefore(LocalTime.of(6, 0))) {
                incrementStats(userId, "timeRestrictions")
                Logger.info("Blocked access due to bedtime for user: $userId")
                return true
            }
        }
        
        // Check wake time
        if (restrictions.wakeTime != null) {
            val wakeTime = LocalTime.parse(restrictions.wakeTime, DateTimeFormatter.ofPattern("HH:mm"))
            if (currentTime.isBefore(wakeTime)) {
                incrementStats(userId, "timeRestrictions")
                Logger.info("Blocked access due to wake time for user: $userId")
                return true
            }
        }
        
        return false
    }
    
    fun enforceSafeSearch(userId: String, searchEngine: String, query: String): String {
        val config = getFamilyModeConfig(userId)
        if (!config.enabled || !config.safeSearch) return query
        
        val isSafeEngine = safeSearchEngines[searchEngine] ?: false
        if (!isSafeEngine) {
            // Redirect to safe search engine
            Logger.info("Redirected to safe search engine for user: $userId")
            return "safe:$query"
        }
        
        // Add safe search parameters
        val safeQuery = when {
            searchEngine.contains("google") -> "$query safe:on"
            searchEngine.contains("bing") -> "$query safe:strict"
            searchEngine.contains("yahoo") -> "$query safe:on"
            else -> query
        }
        
        Logger.info("Applied safe search for user: $userId")
        return safeQuery
    }
    
    fun getContentCategories(): List<ContentCategory> {
        return contentCategories.values.toList()
    }
    
    fun updateContentCategory(categoryId: String, blocked: Boolean): ContentCategory? {
        val category = contentCategories[categoryId] ?: return null
        
        val updatedCategory = category.copy(blocked = blocked)
        contentCategories[categoryId] = updatedCategory
        
        Logger.info("Updated content category: $categoryId - blocked: $blocked")
        return updatedCategory
    }
    
    fun addCustomBlockedUrl(userId: String, categoryId: String, url: String): FamilyModeConfig {
        val config = getFamilyModeConfig(userId)
        val currentBlocked = blockedUrls[categoryId] ?: emptyList()
        val updatedBlocked = currentBlocked + url
        
        blockedUrls[categoryId] = updatedBlocked
        
        Logger.info("Added custom blocked URL for user: $userId - category: $categoryId, URL: $url")
        return config
    }
    
    fun removeCustomBlockedUrl(userId: String, categoryId: String, url: String): FamilyModeConfig {
        val config = getFamilyModeConfig(userId)
        val currentBlocked = blockedUrls[categoryId] ?: emptyList()
        val updatedBlocked = currentBlocked - url
        
        blockedUrls[categoryId] = updatedBlocked
        
        Logger.info("Removed custom blocked URL for user: $userId - category: $categoryId, URL: $url")
        return config
    }
    
    fun getStats(userId: String): FamilyModeStats {
        val config = getFamilyModeConfig(userId)
        return config.stats
    }
    
    fun resetStats(userId: String): FamilyModeConfig {
        val config = getFamilyModeConfig(userId)
        val resetStats = FamilyModeStats(
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
    
    fun getTimeRestrictions(userId: String): TimeRestrictions? {
        val config = getFamilyModeConfig(userId)
        return config.timeRestrictions
    }
    
    fun updateTimeRestrictions(userId: String, restrictions: TimeRestrictions): FamilyModeConfig {
        val config = getFamilyModeConfig(userId)
        
        val updatedConfig = config.copy(
            timeRestrictions = restrictions,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Updated time restrictions for user: $userId")
        
        return updatedConfig
    }
    
    fun isAccessAllowed(userId: String, url: String): Boolean {
        val config = getFamilyModeConfig(userId)
        if (!config.enabled) return true
        
        // Check content filtering
        if (checkUrl(userId, url)) return false
        
        // Check time restrictions
        if (checkTimeRestrictions(userId)) return false
        
        return true
    }
    
    fun getBlockedDomains(categoryId: String): List<String> {
        return blockedUrls[categoryId] ?: emptyList()
    }
    
    fun addBlockedDomain(categoryId: String, domain: String) {
        val currentBlocked = blockedUrls[categoryId] ?: emptyList()
        val updatedBlocked = currentBlocked + domain
        
        blockedUrls[categoryId] = updatedBlocked
        Logger.info("Added blocked domain: $domain to category: $categoryId")
    }
    
    fun removeBlockedDomain(categoryId: String, domain: String) {
        val currentBlocked = blockedUrls[categoryId] ?: emptyList()
        val updatedBlocked = currentBlocked - domain
        
        blockedUrls[categoryId] = updatedBlocked
        Logger.info("Removed blocked domain: $domain from category: $categoryId")
    }
    
    private fun createDefaultConfig(userId: String): FamilyModeConfig {
        val defaultConfig = FamilyModeConfig(
            userId = userId,
            enabled = false,
            contentCategories = contentCategories.values.toList(),
            timeRestrictions = null,
            safeSearch = true
        )
        
        userConfigs[userId] = defaultConfig
        Logger.info("Created default family mode config for user: $userId")
        
        return defaultConfig
    }
    
    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }
    
    private fun incrementStats(userId: String, statType: String) {
        val config = getFamilyModeConfig(userId)
        val currentStats = config.stats
        
        val updatedStats = when (statType) {
            "contentBlocked" -> currentStats.copy(
                contentBlocked = currentStats.contentBlocked + 1,
                lastBlock = LocalDateTime.now()
            )
            "timeRestrictions" -> currentStats.copy(
                timeRestrictions = currentStats.timeRestrictions + 1,
                lastBlock = LocalDateTime.now()
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
