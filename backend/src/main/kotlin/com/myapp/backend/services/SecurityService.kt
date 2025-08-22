package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.util.Logger
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class SecurityService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: SecurityService? = null
        
        fun getInstance(): SecurityService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecurityService().also { INSTANCE = it }
            }
        }
    }
    
    private val adBlockingService = AdBlockingService.getInstance()
    private val malwareProtectionService = MalwareProtectionService.getInstance()
    private val familyModeService = FamilyModeService.getInstance()
    private val dnsProtectionService = DNSProtectionService.getInstance()
    
    // In-memory storage for combined security analytics
    private val securityAnalytics = ConcurrentHashMap<String, SecurityAnalytics>()
    private val threatReports = ConcurrentHashMap<String, List<ThreatReport>>()
    
    fun getSecurityConfig(userId: String): SecurityConfig {
        val adBlocking = adBlockingService.getAdBlockingConfig(userId)
        val malwareProtection = malwareProtectionService.getMalwareProtectionConfig(userId)
        val familyMode = familyModeService.getFamilyModeConfig(userId)
        val dnsProtection = dnsProtectionService.getDNSProtectionConfig(userId)
        
        return SecurityConfig(
            userId = userId,
            adBlocking = adBlocking,
            malwareProtection = malwareProtection,
            familyMode = familyMode,
            dnsProtection = dnsProtection,
            lastUpdated = LocalDateTime.now()
        )
    }
    
    fun updateSecurityConfig(userId: String, request: SecurityConfigRequest): SecurityConfig {
        val adBlocking = if (request.adBlocking != null) {
            adBlockingService.updateAdBlockingConfig(userId, request.adBlocking)
        } else {
            adBlockingService.getAdBlockingConfig(userId)
        }
        
        val malwareProtection = if (request.malwareProtection != null) {
            malwareProtectionService.updateMalwareProtectionConfig(userId, request.malwareProtection)
        } else {
            malwareProtectionService.getMalwareProtectionConfig(userId)
        }
        
        val familyMode = if (request.familyMode != null) {
            familyModeService.updateFamilyModeConfig(userId, request.familyMode)
        } else {
            familyModeService.getFamilyModeConfig(userId)
        }
        
        val dnsProtection = if (request.dnsProtection != null) {
            dnsProtectionService.updateDNSProtectionConfig(userId, request.dnsProtection)
        } else {
            dnsProtectionService.getDNSProtectionConfig(userId)
        }
        
        val updatedConfig = SecurityConfig(
            userId = userId,
            adBlocking = adBlocking,
            malwareProtection = malwareProtection,
            familyMode = familyMode,
            dnsProtection = dnsProtection,
            lastUpdated = LocalDateTime.now()
        )
        
        Logger.info("Updated security config for user: $userId")
        return updatedConfig
    }
    
    fun checkSecurity(userId: String, url: String, domain: String): SecurityCheckResult {
        val results = mutableListOf<SecurityCheckResult>()
        
        // Check ad blocking
        if (adBlockingService.checkUrl(userId, url)) {
            results.add(SecurityCheckResult(
                service = "AdBlocking",
                blocked = true,
                reason = "Ad or tracker detected",
                severity = ThreatSeverity.MEDIUM
            ))
        }
        
        // Check malware protection
        val malwareThreat = malwareProtectionService.checkUrl(userId, url)
        if (malwareThreat != null) {
            results.add(SecurityCheckResult(
                service = "MalwareProtection",
                blocked = true,
                reason = malwareThreat.description,
                severity = malwareThreat.severity
            ))
        }
        
        // Check family mode
        if (familyModeService.checkUrl(userId, url)) {
            results.add(SecurityCheckResult(
                service = "FamilyMode",
                blocked = true,
                reason = "Inappropriate content detected",
                severity = ThreatSeverity.HIGH
            ))
        }
        
        // Check DNS protection
        val dnsLeak = dnsProtectionService.checkDNSLeak(userId, domain)
        if (dnsLeak != null) {
            results.add(SecurityCheckResult(
                service = "DNSProtection",
                blocked = false,
                reason = "DNS leak detected",
                severity = ThreatSeverity.MEDIUM
            ))
        }
        
        val isBlocked = results.any { it.blocked }
        val highestSeverity = results.maxByOrNull { it.severity.ordinal }?.severity ?: ThreatSeverity.LOW
        
        return SecurityCheckResult(
            service = "Combined",
            blocked = isBlocked,
            reason = if (isBlocked) "Multiple security checks failed" else "All security checks passed",
            severity = highestSeverity,
            details = results
        )
    }
    
    fun getSecurityAnalytics(userId: String, period: String = "daily"): SecurityAnalytics {
        val adBlockingStats = adBlockingService.getStats(userId)
        val malwareStats = malwareProtectionService.getStats(userId)
        val familyModeStats = familyModeService.getStats(userId)
        val dnsStats = dnsProtectionService.getStats(userId)
        
        val totalThreatsBlocked = adBlockingStats.totalBlocked + malwareStats.threatsBlocked
        val totalAdsBlocked = adBlockingStats.adsBlocked
        val totalContentFiltered = familyModeStats.contentBlocked
        val totalDNSQueries = dnsStats.dnsQueries
        
        // Calculate security score (0-100)
        val securityScore = calculateSecurityScore(
            threatsBlocked = totalThreatsBlocked,
            adsBlocked = totalAdsBlocked,
            contentFiltered = totalContentFiltered,
            dnsQueries = totalDNSQueries
        )
        
        val analytics = SecurityAnalytics(
            userId = userId,
            period = period,
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now(),
            threatsBlocked = totalThreatsBlocked,
            adsBlocked = totalAdsBlocked,
            contentFiltered = totalContentFiltered,
            dnsQueries = totalDNSQueries,
            securityScore = securityScore
        )
        
        securityAnalytics[userId] = analytics
        return analytics
    }
    
    fun getThreatReports(userId: String): List<ThreatReport> {
        val reports = mutableListOf<ThreatReport>()
        
        // Collect threat reports from all services
        // Note: In a real implementation, these would be stored in a database
        // For now, we'll return an empty list as the individual services handle their own reports
        
        return reports
    }
    
    fun resetAllStats(userId: String): SecurityConfig {
        adBlockingService.resetStats(userId)
        malwareProtectionService.resetStats(userId)
        familyModeService.resetStats(userId)
        dnsProtectionService.resetStats(userId)
        
        Logger.info("Reset all security stats for user: $userId")
        return getSecurityConfig(userId)
    }
    
    fun updateAllSecurityDatabases(): Boolean {
        val results = mutableListOf<Boolean>()
        
        try {
            results.add(adBlockingService.updateFilterLists())
            results.add(malwareProtectionService.updateThreatDatabase())
            results.add(dnsProtectionService.updateSecureDNSServers())
            
            val allSuccessful = results.all { it }
            if (allSuccessful) {
                Logger.info("All security databases updated successfully")
            } else {
                Logger.warning("Some security databases failed to update")
            }
            
            return allSuccessful
        } catch (e: Exception) {
            Logger.error("Error updating security databases: ${e.message}")
            return false
        }
    }
    
    fun getSecurityStatus(userId: String): SecurityStatus {
        val config = getSecurityConfig(userId)
        
        val activeServices = mutableListOf<String>()
        if (config.adBlocking.enabled) activeServices.add("Ad Blocking")
        if (config.malwareProtection.enabled) activeServices.add("Malware Protection")
        if (config.familyMode.enabled) activeServices.add("Family Mode")
        if (config.dnsProtection.enabled) activeServices.add("DNS Protection")
        
        val totalThreatsBlocked = config.adBlocking.stats.totalBlocked + 
                                 config.malwareProtection.stats.threatsBlocked
        
        return SecurityStatus(
            userId = userId,
            activeServices = activeServices,
            totalThreatsBlocked = totalThreatsBlocked,
            lastThreat = getLastThreatTime(config),
            overallStatus = if (activeServices.isNotEmpty()) "Protected" else "Unprotected",
            timestamp = LocalDateTime.now()
        )
    }
    
    fun enableAllSecurityFeatures(userId: String): SecurityConfig {
        val adBlockingRequest = AdBlockingRequest(enabled = true)
        val malwareRequest = MalwareProtectionRequest(enabled = true)
        val familyModeRequest = FamilyModeRequest(enabled = true)
        val dnsRequest = DNSProtectionRequest(enabled = true)
        
        val securityRequest = SecurityConfigRequest(
            adBlocking = adBlockingRequest,
            malwareProtection = malwareRequest,
            familyMode = familyModeRequest,
            dnsProtection = dnsRequest
        )
        
        return updateSecurityConfig(userId, securityRequest)
    }
    
    fun disableAllSecurityFeatures(userId: String): SecurityConfig {
        val adBlockingRequest = AdBlockingRequest(enabled = false)
        val malwareRequest = MalwareProtectionRequest(enabled = false)
        val familyModeRequest = FamilyModeRequest(enabled = false)
        val dnsRequest = DNSProtectionRequest(enabled = false)
        
        val securityRequest = SecurityConfigRequest(
            adBlocking = adBlockingRequest,
            malwareProtection = malwareRequest,
            familyMode = familyModeRequest,
            dnsProtection = dnsRequest
        )
        
        return updateSecurityConfig(userId, securityRequest)
    }
    
    private fun calculateSecurityScore(
        threatsBlocked: Long,
        adsBlocked: Long,
        contentFiltered: Long,
        dnsQueries: Long
    ): Int {
        // Simple scoring algorithm
        var score = 100
        
        // Deduct points for high threat activity
        if (threatsBlocked > 100) score -= 20
        else if (threatsBlocked > 50) score -= 10
        else if (threatsBlocked > 10) score -= 5
        
        // Add points for active protection
        if (adsBlocked > 0) score += 5
        if (contentFiltered > 0) score += 5
        if (dnsQueries > 0) score += 5
        
        // Ensure score is within bounds
        return score.coerceIn(0, 100)
    }
    
    private fun getLastThreatTime(config: SecurityConfig): LocalDateTime? {
        val times = listOfNotNull(
            config.adBlocking.stats.lastReset,
            config.malwareProtection.stats.lastThreat,
            config.familyMode.stats.lastBlock,
            config.dnsProtection.stats.lastLeak
        )
        
        return times.maxOrNull()
    }
    
    // Helper data classes
    data class SecurityCheckResult(
        val service: String,
        val blocked: Boolean,
        val reason: String,
        val severity: ThreatSeverity,
        val details: List<SecurityCheckResult> = emptyList()
    )
    
    data class SecurityStatus(
        val userId: String,
        val activeServices: List<String>,
        val totalThreatsBlocked: Long,
        val lastThreat: LocalDateTime?,
        val overallStatus: String,
        val timestamp: LocalDateTime
    )
}
