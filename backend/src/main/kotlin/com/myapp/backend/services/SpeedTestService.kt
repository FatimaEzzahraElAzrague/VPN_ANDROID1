package com.myapp.backend.services

import com.myapp.backend.models.*
import java.time.LocalDateTime
import java.util.*

/**
 * Speed Test Service
 * Handles speed test results, analytics, and server management on the backend
 */
class SpeedTestService {
    
    companion object {
        private const val TAG = "SpeedTestService"
        
        @Volatile
        private var INSTANCE: SpeedTestService? = null
        
        fun getInstance(): SpeedTestService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SpeedTestService().also { INSTANCE = it }
            }
        }
    }
    
    // In-memory storage (in production, this would be database)
    private val speedTestResults = mutableMapOf<String, MutableList<SpeedTestResult>>()
    private val speedTestConfigs = mutableMapOf<String, SpeedTestConfig>()
    private val speedTestAnalytics = mutableMapOf<String, SpeedTestAnalytics>()
    private val speedTestServers = mutableListOf<SpeedTestServer>()
    
    init {
        // Initialize default speed test servers
        initializeDefaultServers()
    }
    
    /**
     * Save speed test result
     */
    fun saveSpeedTestResult(
        userId: String,
        pingMs: Long,
        downloadMbps: Double,
        uploadMbps: Double,
        jitterMs: Long? = null,
        testServer: String,
        deviceInfo: DeviceInfo? = null,
        networkType: String? = null
    ): SpeedTestResult {
        val result = SpeedTestResult(
            id = UUID.randomUUID().toString(),
            userId = userId,
            pingMs = pingMs,
            downloadMbps = downloadMbps,
            uploadMbps = uploadMbps,
            jitterMs = jitterMs,
            testServer = testServer,
            deviceInfo = deviceInfo,
            networkType = networkType,
            timestamp = LocalDateTime.now().toString()
        )
        
        // Store result
        if (!speedTestResults.containsKey(userId)) {
            speedTestResults[userId] = mutableListOf()
        }
        speedTestResults[userId]!!.add(result)
        
        // Update analytics
        updateAnalytics(userId, result)
        
        return result
    }
    
    /**
     * Get speed test configuration for a user
     */
    fun getSpeedTestConfig(userId: String): SpeedTestConfig {
        return speedTestConfigs[userId] ?: createDefaultConfig(userId)
    }
    
    /**
     * Update speed test configuration for a user
     */
    fun updateSpeedTestConfig(userId: String, request: SpeedTestConfigRequest): SpeedTestConfig {
        val currentConfig = getSpeedTestConfig(userId)
        
        val updatedConfig = currentConfig.copy(
            preferredServer = request.preferredServer ?: currentConfig.preferredServer,
            uploadSizeBytes = request.uploadSizeBytes ?: currentConfig.uploadSizeBytes,
            autoTestEnabled = request.autoTestEnabled ?: currentConfig.autoTestEnabled,
            testIntervalMinutes = request.testIntervalMinutes ?: currentConfig.testIntervalMinutes,
            saveResults = request.saveResults ?: currentConfig.saveResults,
            updatedAt = LocalDateTime.now().toString()
        )
        
        speedTestConfigs[userId] = updatedConfig
        return updatedConfig
    }
    
    /**
     * Get speed test analytics for a user
     */
    fun getSpeedTestAnalytics(userId: String, period: AnalyticsPeriod = AnalyticsPeriod.DAY): SpeedTestAnalytics {
        return speedTestAnalytics[userId] ?: SpeedTestAnalytics(userId = userId, period = period)
    }
    
    /**
     * Get recent speed test results for a user
     */
    fun getRecentSpeedTestResults(userId: String, limit: Int = 10): List<SpeedTestResult> {
        return speedTestResults[userId]?.takeLast(limit)?.reversed() ?: emptyList()
    }
    
    /**
     * Get available speed test servers
     */
    fun getAvailableServers(): List<SpeedTestServer> {
        return speedTestServers.filter { it.isActive }.sortedBy { it.priority }
    }
    
    /**
     * Get speed test configuration with servers and analytics
     */
    fun getSpeedTestConfiguration(userId: String): SpeedTestConfigResponse {
        val config = getSpeedTestConfig(userId)
        val servers = getAvailableServers()
        val analytics = getSpeedTestAnalytics(userId)
        
        return SpeedTestConfigResponse(
            config = config,
            availableServers = servers,
            analytics = analytics
        )
    }
    
    /**
     * Get speed test statistics for admin dashboard
     */
    fun getSpeedTestStatistics(): Map<String, Any> {
        val totalUsers = speedTestConfigs.size
        val totalTests = speedTestResults.values.sumOf { it.size }
        val totalResults = speedTestResults.values.flatten()
        
        val averagePing = if (totalResults.isNotEmpty()) {
            totalResults.map { it.pingMs }.average()
        } else 0.0
        
        val averageDownload = if (totalResults.isNotEmpty()) {
            totalResults.map { it.downloadMbps }.average()
        } else 0.0
        
        val averageUpload = if (totalResults.isNotEmpty()) {
            totalResults.map { it.uploadMbps }.average()
        } else 0.0
        
        return mapOf(
            "totalUsers" to totalUsers,
            "totalTests" to totalTests,
            "averagePingMs" to averagePing,
            "averageDownloadMbps" to averageDownload,
            "averageUploadMbps" to averageUpload,
            "activeServers" to speedTestServers.count { it.isActive }
        )
    }
    
    /**
     * Create default speed test configuration for a user
     */
    private fun createDefaultConfig(userId: String): SpeedTestConfig {
        val defaultConfig = SpeedTestConfig(userId = userId)
        speedTestConfigs[userId] = defaultConfig
        return defaultConfig
    }
    
    /**
     * Initialize default speed test servers
     */
    private fun initializeDefaultServers() {
        val defaultServers = listOf(
            SpeedTestServer(
                id = "cloudflare-1",
                name = "Cloudflare Speed Test",
                url = "https://speed.cloudflare.com/__down",
                location = "Global",
                country = "Global",
                isActive = true,
                priority = 1
            ),
            SpeedTestServer(
                id = "httpbin-1",
                name = "HTTPBin Test Server",
                url = "https://httpbin.org/get",
                location = "Global",
                country = "Global",
                isActive = true,
                priority = 2
            ),
            SpeedTestServer(
                id = "fastly-1",
                name = "Fastly CDN",
                url = "https://www.fastly.com/",
                location = "Global",
                country = "Global",
                isActive = true,
                priority = 3
            )
        )
        
        speedTestServers.addAll(defaultServers)
    }
    
    /**
     * Update analytics based on new result
     */
    private fun updateAnalytics(userId: String, result: SpeedTestResult) {
        val currentAnalytics = speedTestAnalytics[userId] ?: SpeedTestAnalytics(userId = userId)
        val userResults = speedTestResults[userId] ?: listOf(result)
        
        val totalTests = userResults.size
        val averagePingMs = userResults.map { it.pingMs }.average()
        val averageDownloadMbps = userResults.map { it.downloadMbps }.average()
        val averageUploadMbps = userResults.map { it.uploadMbps }.average()
        val bestPingMs = userResults.minOfOrNull { it.pingMs }
        val bestDownloadMbps = userResults.maxOfOrNull { it.downloadMbps }
        val bestUploadMbps = userResults.maxOfOrNull { it.uploadMbps }
        val lastTestDate = result.timestamp
        
        val updatedAnalytics = currentAnalytics.copy(
            totalTests = totalTests,
            averagePingMs = averagePingMs,
            averageDownloadMbps = averageDownloadMbps,
            averageUploadMbps = averageUploadMbps,
            bestPingMs = bestPingMs,
            bestDownloadMbps = bestDownloadMbps,
            bestUploadMbps = bestUploadMbps,
            lastTestDate = lastTestDate
        )
        
        speedTestAnalytics[userId] = updatedAnalytics
    }
    
    /**
     * Clean up old results (keep only last 100 results per user)
     */
    fun cleanupOldResults() {
        speedTestResults.forEach { (userId, results) ->
            if (results.size > 100) {
                speedTestResults[userId] = results.takeLast(100).toMutableList()
            }
        }
    }
}
