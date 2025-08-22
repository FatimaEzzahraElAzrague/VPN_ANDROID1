package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.util.Logger
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.net.InetAddress
import java.net.UnknownHostException

class DNSProtectionService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: DNSProtectionService? = null
        
        fun getInstance(): DNSProtectionService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DNSProtectionService().also { INSTANCE = it }
            }
        }
    }
    
    // In-memory storage (replace with database in production)
    private val userConfigs = ConcurrentHashMap<String, DNSProtectionConfig>()
    private val secureDNSServers = ConcurrentHashMap<String, DNSServerInfo>()
    private val dnsQueries = ConcurrentHashMap<String, List<DNSQuery>>()
    private val leakReports = ConcurrentHashMap<String, List<DNSLeakReport>>()
    
    init {
        initializeSecureDNSServers()
    }
    
    private fun initializeSecureDNSServers() {
        val servers = mapOf(
            "cloudflare" to DNSServerInfo(
                "1.1.1.1",
                "Cloudflare DNS",
                "Fast and privacy-focused DNS service",
                true,
                "https://1.1.1.1"
            ),
            "google" to DNSServerInfo(
                "8.8.8.8",
                "Google DNS",
                "Reliable and fast DNS service",
                true,
                "https://8.8.8.8"
            ),
            "opendns" to DNSServerInfo(
                "208.67.222.222",
                "OpenDNS",
                "Free DNS service with content filtering",
                true,
                "https://208.67.222.222"
            ),
            "quad9" to DNSServerInfo(
                "9.9.9.9",
                "Quad9 DNS",
                "Security-focused DNS with malware blocking",
                true,
                "https://9.9.9.9"
            ),
            "adguard" to DNSServerInfo(
                "176.103.130.130",
                "AdGuard DNS",
                "DNS with ad and tracker blocking",
                true,
                "https://176.103.130.130"
            )
        )
        
        secureDNSServers.putAll(servers)
    }
    
    fun getDNSProtectionConfig(userId: String): DNSProtectionConfig {
        return userConfigs[userId] ?: createDefaultConfig(userId)
    }
    
    fun updateDNSProtectionConfig(userId: String, request: DNSProtectionRequest): DNSProtectionConfig {
        val currentConfig = getDNSProtectionConfig(userId)
        
        val updatedConfig = currentConfig.copy(
            enabled = request.enabled,
            secureDNSServers = request.secureDNSServers ?: currentConfig.secureDNSServers,
            leakDetection = request.leakDetection ?: currentConfig.leakDetection,
            autoSwitch = request.autoSwitch ?: currentConfig.autoSwitch,
            customDNS = request.customDNS,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Updated DNS protection config for user: $userId")
        
        return updatedConfig
    }
    
    fun checkDNSLeak(userId: String, domain: String): DNSLeakReport? {
        val config = getDNSProtectionConfig(userId)
        if (!config.enabled || !config.leakDetection) return null
        
        try {
            val localDNS = getLocalDNSServer()
            val secureDNS = getSecureDNSServer(config)
            
            if (localDNS != null && secureDNS != null && localDNS != secureDNS) {
                val leakReport = DNSLeakReport(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    domain = domain,
                    localDNS = localDNS,
                    secureDNS = secureDNS,
                    timestamp = LocalDateTime.now(),
                    severity = DNSLeakSeverity.MEDIUM,
                    description = "DNS leak detected: using local DNS instead of secure DNS"
                )
                
                addLeakReport(userId, leakReport)
                incrementStats(userId, "leaksDetected")
                
                Logger.warning("DNS leak detected for user: $userId - domain: $domain")
                return leakReport
            }
            
            // Log DNS query
            logDNSQuery(userId, domain, localDNS ?: "unknown")
            
        } catch (e: Exception) {
            Logger.error("Error checking DNS leak: ${e.message}")
        }
        
        return null
    }
    
    fun switchToSecureDNS(userId: String): Boolean {
        val config = getDNSProtectionConfig(userId)
        if (!config.enabled || !config.autoSwitch) return false
        
        try {
            val secureDNS = getSecureDNSServer(config)
            if (secureDNS != null) {
                // In a real implementation, this would change the system DNS
                Logger.info("Switched to secure DNS for user: $userId: $secureDNS")
                return true
            }
        } catch (e: Exception) {
            Logger.error("Error switching to secure DNS: ${e.message}")
        }
        
        return false
    }
    
    fun testDNSServer(server: String): DNSTestResult {
        return try {
            val startTime = System.currentTimeMillis()
            val address = InetAddress.getByName(server)
            val responseTime = System.currentTimeMillis() - startTime
            
            DNSTestResult(
                server = server,
                reachable = true,
                responseTime = responseTime,
                ipAddress = address.hostAddress,
                timestamp = LocalDateTime.now()
            )
        } catch (e: UnknownHostException) {
            DNSTestResult(
                server = server,
                reachable = false,
                responseTime = -1,
                ipAddress = null,
                timestamp = LocalDateTime.now(),
                error = e.message
            )
        }
    }
    
    fun getSecureDNSServers(): List<DNSServerInfo> {
        return secureDNSServers.values.toList()
    }
    
    fun addCustomDNSServer(userId: String, server: String, name: String, description: String): DNSProtectionConfig {
        val config = getDNSProtectionConfig(userId)
        val customServer = DNSServerInfo(
            ip = server,
            name = name,
            description = description,
            secure = true,
            url = "https://$server"
        )
        
        secureDNSServers[name.lowercase()] = customServer
        
        val updatedConfig = config.copy(
            secureDNSServers = config.secureDNSServers + server,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Added custom DNS server for user: $userId: $server")
        
        return updatedConfig
    }
    
    fun removeCustomDNSServer(userId: String, server: String): DNSProtectionConfig {
        val config = getDNSProtectionConfig(userId)
        val updatedServers = config.secureDNSServers - server
        
        val updatedConfig = config.copy(
            secureDNSServers = updatedServers,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
        Logger.info("Removed custom DNS server for user: $userId: $server")
        
        return updatedConfig
    }
    
    fun getStats(userId: String): DNSProtectionStats {
        val config = getDNSProtectionConfig(userId)
        return config.stats
    }
    
    fun resetStats(userId: String): DNSProtectionConfig {
        val config = getDNSProtectionConfig(userId)
        val resetStats = DNSProtectionStats(
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
    
    fun getDNSQueries(userId: String): List<DNSQuery> {
        return dnsQueries[userId] ?: emptyList()
    }
    
    fun getLeakReports(userId: String): List<DNSLeakReport> {
        return leakReports[userId] ?: emptyList()
    }
    
    fun clearDNSQueries(userId: String) {
        dnsQueries[userId] = emptyList()
        Logger.info("Cleared DNS queries for user: $userId")
    }
    
    fun clearLeakReports(userId: String) {
        leakReports[userId] = emptyList()
        Logger.info("Cleared DNS leak reports for user: $userId")
    }
    
    fun updateSecureDNSServers(): Boolean {
        try {
            // In a real implementation, this would fetch updated DNS server information
            Logger.info("Updating secure DNS servers from external sources")
            
            // Simulate updating DNS servers
            Thread.sleep(1500)
            
            Logger.info("Secure DNS servers updated successfully")
            return true
        } catch (e: Exception) {
            Logger.error("Failed to update secure DNS servers: ${e.message}")
            return false
        }
    }
    
    private fun createDefaultConfig(userId: String): DNSProtectionConfig {
        val defaultConfig = DNSProtectionConfig(
            userId = userId,
            enabled = false,
            secureDNSServers = listOf("1.1.1.1", "8.8.8.8"),
            leakDetection = true,
            autoSwitch = true
        )
        
        userConfigs[userId] = defaultConfig
        Logger.info("Created default DNS protection config for user: $userId")
        
        return defaultConfig
    }
    
    private fun getLocalDNSServer(): String? {
        // In a real implementation, this would detect the current system DNS
        // For universal distribution, return null to use system default
        return null
    }
    
    private fun getSecureDNSServer(config: DNSProtectionConfig): String? {
        return config.secureDNSServers.firstOrNull()
    }
    
    private fun addLeakReport(userId: String, report: DNSLeakReport) {
        val currentReports = leakReports[userId] ?: emptyList()
        val updatedReports = currentReports + report
        
        leakReports[userId] = updatedReports
    }
    
    private fun logDNSQuery(userId: String, domain: String, dnsServer: String) {
        val query = DNSQuery(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            domain = domain,
            dnsServer = dnsServer,
            timestamp = LocalDateTime.now()
        )
        
        val currentQueries = dnsQueries[userId] ?: emptyList()
        val updatedQueries = currentQueries + query
        
        dnsQueries[userId] = updatedQueries
        
        incrementStats(userId, "dnsQueries")
    }
    
    private fun incrementStats(userId: String, statType: String) {
        val config = getDNSProtectionConfig(userId)
        val currentStats = config.stats
        
        val updatedStats = when (statType) {
            "leaksDetected" -> currentStats.copy(
                leaksDetected = currentStats.leaksDetected + 1,
                lastLeak = LocalDateTime.now()
            )
            "dnsQueries" -> currentStats.copy(
                dnsQueries = currentStats.dnsQueries + 1
            )
            else -> currentStats
        }
        
        val updatedConfig = config.copy(
            stats = updatedStats,
            lastUpdated = LocalDateTime.now()
        )
        
        userConfigs[userId] = updatedConfig
    }
    
    // Helper data classes
    private data class DNSServerInfo(
        val ip: String,
        val name: String,
        val description: String,
        val secure: Boolean,
        val url: String
    )
    
    private data class DNSQuery(
        val id: String,
        val userId: String,
        val domain: String,
        val dnsServer: String,
        val timestamp: LocalDateTime
    )
    
    private data class DNSLeakReport(
        val id: String,
        val userId: String,
        val domain: String,
        val localDNS: String,
        val secureDNS: String,
        val timestamp: LocalDateTime,
        val severity: DNSLeakSeverity,
        val description: String
    )
    
    private enum class DNSLeakSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    private data class DNSTestResult(
        val server: String,
        val reachable: Boolean,
        val responseTime: Long,
        val ipAddress: String?,
        val timestamp: LocalDateTime,
        val error: String? = null
    )
}
