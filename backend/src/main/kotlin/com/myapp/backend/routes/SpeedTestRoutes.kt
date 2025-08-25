package com.myapp.backend.routes

import com.myapp.backend.services.SpeedTestService
import com.myapp.backend.config.Jwt
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

fun Route.speedTestRoutes() {
    val speedTestService = SpeedTestService.getInstance()
    val logger = LoggerFactory.getLogger("SpeedTestRoutes")
    
    // Rate limiting for download/upload endpoints
    val downloadRequests = AtomicLong(0)
    val uploadRequests = AtomicLong(0)
    val lastResetTime = AtomicLong(System.currentTimeMillis())
    
    val MAX_REQUESTS_PER_MINUTE = 20L
    val RATE_LIMIT_WINDOW_MS = 60_000L // 1 minute
    
    route("/speedtest") {
        
        /**
         * GET /speedtest/config/{userId}
         * Get speed test configuration and available servers for a user
         * Requires JWT authentication
         */
        get("/config/{userId}") {
            try {
                // Extract and validate JWT token
                val token = call.request.header("Authorization")?.removePrefix("Bearer ")
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Missing authorization token")
                    )
                
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                // Validate JWT and extract user ID
                val tokenUserId = try {
                    Jwt.getUserIdFromToken(token)
                } catch (e: Exception) {
                    logger.warn("Invalid JWT token: ${e.message}")
                    return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid authorization token")
                    )
                }
                
                // Ensure user can only access their own data
                if (tokenUserId.toString() != userId) {
                    logger.warn("User $tokenUserId attempted to access data for user $userId")
                    return@get call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Access denied")
                    )
                }
                
                // Get speed test configuration
                val response = speedTestService.getSpeedTestConfiguration(userId)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "config" to response.config,
                    "servers" to response.availableServers
                ))
                
            } catch (e: Exception) {
                logger.error("Error getting speed test configuration", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test configuration")
                )
            }
        }
        
        /**
         * GET /speedtest/ping
         * Lightweight endpoint for latency measurement
         * No authentication required for basic ping
         */
        get("/ping") {
            try {
                val startTime = System.currentTimeMillis()
                
                // Simple response to measure round-trip time
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "pong",
                    "timestamp" to startTime,
                    "server" to "VPN Backend"
                ))
                
            } catch (e: Exception) {
                logger.error("Error in ping endpoint", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Ping failed")
                )
            }
        }
        
        /**
         * GET /speedtest/download
         * Streams random bytes for download measurement
         * Rate limited to prevent abuse
         */
        get("/download") {
            try {
                // Check rate limit
                if (!checkDownloadRateLimit()) {
                    return@get call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf("error" to "Rate limit exceeded. Try again later.")
                    )
                }
                
                // Get requested size (default 10MB, max 100MB)
                val requestedSize = call.request.queryParameters["size"]?.toLongOrNull() ?: 10_000_000L
                val sizeBytes = min(requestedSize, 100_000_000L) // Cap at 100MB
                
                // Increment rate limit counter
                downloadRequests.incrementAndGet()
                
                logger.info("Download request: $sizeBytes bytes")
                
                // Set response headers
                call.response.headers.append("Content-Type", "application/octet-stream")
                call.response.headers.append("Content-Length", sizeBytes.toString())
                call.response.headers.append("Cache-Control", "no-cache, no-store, must-revalidate")
                
                // Stream random data
                val random = java.util.Random()
                val chunkSize = 1024 * 1024 // 1MB chunks
                var remainingBytes = sizeBytes
                
                while (remainingBytes > 0) {
                    val currentChunkSize = min(remainingBytes, chunkSize.toLong()).toInt()
                    val chunk = ByteArray(currentChunkSize)
                    random.nextBytes(chunk)
                    
                    call.respondOutputStream {
                        write(chunk)
                        flush()
                    }
                    
                    remainingBytes -= currentChunkSize
                }
                
            } catch (e: Exception) {
                logger.error("Error in download endpoint", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Download failed")
                )
            }
        }
        
        /**
         * POST /speedtest/upload
         * Accepts POST data for upload measurement
         * Rate limited to prevent abuse
         */
        post("/upload") {
            try {
                // Check rate limit
                if (!checkUploadRateLimit()) {
                    return@post call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf("error" to "Rate limit exceeded. Try again later.")
                    )
                }
                
                // Get expected size from query parameter
                val expectedSize = call.request.queryParameters["expected_size"]?.toLongOrNull()
                
                // Increment rate limit counter
                uploadRequests.incrementAndGet()
                
                val startTime = System.currentTimeMillis()
                
                // Read uploaded data
                val uploadedData = call.receive<ByteArray>()
                val actualSize = uploadedData.size.toLong()
                
                // Validate size if expected size was provided
                if (expectedSize != null && actualSize != expectedSize) {
                    logger.warn("Size mismatch: expected $expectedSize, got $actualSize")
                }
                
                // Calculate upload speed
                val uploadTimeMs = System.currentTimeMillis() - startTime
                val uploadSpeedMbps = if (uploadTimeMs > 0) {
                    (actualSize * 8.0) / (uploadTimeMs * 1_000_000.0) // Convert to Mbps
                } else 0.0
                
                logger.info("Upload completed: $actualSize bytes in ${uploadTimeMs}ms, speed: ${uploadSpeedMbps}Mbps")
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Upload processed successfully",
                    "sizeBytes" to actualSize,
                    "uploadTimeMs" to uploadTimeMs,
                    "uploadSpeedMbps" to String.format("%.2f", uploadSpeedMbps)
                ))
                
            } catch (e: Exception) {
                logger.error("Error in upload endpoint", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Upload failed")
                )
            }
        }
        
        /**
         * GET /speedtest/servers
         * Get available speed test servers
         * Public endpoint (no authentication required)
         */
        get("/servers") {
            try {
                val servers = speedTestService.getAvailableServers()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "servers" to servers
                ))
                
            } catch (e: Exception) {
                logger.error("Error getting speed test servers", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test servers")
                )
            }
        }
    }
    
    /**
     * Check rate limit for download endpoint
     */
    private fun checkDownloadRateLimit(): Boolean {
        return checkRateLimit(downloadRequests, "download")
    }
    
    /**
     * Check rate limit for upload endpoint
     */
    private fun checkUploadRateLimit(): Boolean {
        return checkRateLimit(uploadRequests, "upload")
    }
    
    /**
     * Generic rate limit checker
     */
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
}
