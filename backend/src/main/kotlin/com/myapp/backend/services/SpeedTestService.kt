package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.repositories.SpeedTestRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

/**
 * Production-ready Speed Test Service
 * Handles speed test operations, rate limiting, and data management
 */
class SpeedTestService {
    
    companion object {
        private val logger = LoggerFactory.getLogger(SpeedTestService::class.java)
        
        @Volatile
        private var INSTANCE: SpeedTestService? = null
        
        fun getInstance(): SpeedTestService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SpeedTestService().also { INSTANCE = it }
            }
        }
    }
    
    private val repository = SpeedTestRepository()
    
    // Rate limiting for download/upload endpoints
    private val downloadRequests = AtomicLong(0)
    private val uploadRequests = AtomicLong(0)
    private val lastResetTime = AtomicLong(System.currentTimeMillis())
    
    private val MAX_REQUESTS_PER_MINUTE = 10L
    private val RATE_LIMIT_WINDOW_MS = 60_000L // 1 minute
    
    init {
        // Initialize default servers including Osaka and Paris
        repository.initializeDefaultServers()
        logger.info("SpeedTestService initialized with default servers including Osaka and Paris")
    }
    
    /**
     * Process speed test result (no storage)
     */
    fun processSpeedTestResult(
        userId: String,
        serverId: String,
        pingMs: Long,
        downloadMbps: Double,
        uploadMbps: Double
    ): SpeedTestResponse {
        logger.info("Processing speed test result for user: $userId, server: $serverId")
        
        // Just log the result, no storage
        logger.info("Speed test - Ping: ${pingMs}ms, Download: ${downloadMbps}Mbps, Upload: ${uploadMbps}Mbps")
        
        return SpeedTestResponse(
            success = true,
            message = "Speed test completed successfully"
        )
    }
    
    /**
     * Get speed test configuration for a user
     */
    fun getSpeedTestConfiguration(userId: String): SpeedTestConfigResponse {
        logger.debug("Getting speed test configuration for user: $userId")
        
        val config = SpeedTestConfig(userId = userId)
        val servers = repository.getAvailableServers()
        
        return SpeedTestConfigResponse(
            config = config,
            availableServers = servers
        )
    }
    

    
    /**
     * Get available speed test servers
     */
    fun getAvailableServers(): List<SpeedTestServer> {
        logger.debug("Getting available speed test servers")
        return repository.getAvailableServers()
    }
    
    /**
     * Get server by ID
     */
    fun getServerById(serverId: String): SpeedTestServer? {
        logger.debug("Getting server by ID: $serverId")
        return repository.getServerById(serverId)
    }
    
    /**
     * Check rate limit for download endpoint
     */
    fun checkDownloadRateLimit(): Boolean {
        return checkRateLimit(downloadRequests, "download")
    }
    
    /**
     * Check rate limit for upload endpoint
     */
    fun checkUploadRateLimit(): Boolean {
        return checkRateLimit(uploadRequests, "upload")
    }
    
    /**
     * Increment download request counter
     */
    fun incrementDownloadRequests() {
        downloadRequests.incrementAndGet()
    }
    
    /**
     * Increment upload request counter
     */
    fun incrementUploadRequests() {
        uploadRequests.incrementAndGet()
    }
    
    /**
     * Generate random data for download testing
     */
    fun generateRandomData(sizeBytes: Long): ByteArray {
        val actualSize = min(sizeBytes, MAX_DOWNLOAD_SIZE)
        logger.debug("Generating random data: $actualSize bytes")
        
        return ByteArray(actualSize.toInt()).apply {
            java.util.Random().nextBytes(this)
        }
    }
    
    /**
     * Process upload and calculate speed
     */
    fun processUpload(data: ByteArray, uploadTimeMs: Long): SpeedTestUploadResponse {
        val sizeBytes = data.size.toLong()
        val uploadSpeedMbps = if (uploadTimeMs > 0) {
            (sizeBytes * 8.0) / (uploadTimeMs * 1_000_000.0) // Convert to Mbps
        } else 0.0
        
        logger.info("Upload processed: $sizeBytes bytes in ${uploadTimeMs}ms, speed: ${uploadSpeedMbps}Mbps")
        
        return SpeedTestUploadResponse(
            success = true,
            message = "Upload processed successfully",
            sizeBytes = sizeBytes,
            uploadTimeMs = uploadTimeMs,
            uploadSpeedMbps = uploadSpeedMbps
        )
    }
    

    
    /**
     * Get speed test statistics for admin dashboard
     */
    fun getSpeedTestStatistics(): Map<String, Any> {
        logger.debug("Getting speed test statistics")
        
        // This would typically query the database for aggregated stats
        // For now, return basic info
        return mapOf(
            "totalServers" to repository.getAvailableServers().size,
            "lastCleanup" to LocalDateTime.now().toString(),
            "rateLimitEnabled" to true,
            "maxRequestsPerMinute" to MAX_REQUESTS_PER_MINUTE
        )
    }
    
    private fun checkRateLimit(counter: AtomicLong, endpoint: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastReset = lastResetTime.get()
        
        // Reset counter if window has passed
        if (currentTime - lastReset > RATE_LIMIT_WINDOW_MS) {
            counter.set(0)
            lastResetTime.set(currentTime)
        }
        
        val currentCount = counter.get()
        val allowed = currentCount < MAX_REQUESTS_PER_MINUTE
        
        if (!allowed) {
            logger.warn("Rate limit exceeded for $endpoint endpoint: $currentCount requests")
        }
        
        return allowed
    }
    
    companion object {
        private const val MAX_DOWNLOAD_SIZE = 100L * 1024L * 1024L // 100MB
    }
}
