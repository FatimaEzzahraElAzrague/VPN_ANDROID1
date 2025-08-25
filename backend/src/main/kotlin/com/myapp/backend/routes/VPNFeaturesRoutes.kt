package com.myapp.backend.routes

import com.myapp.backend.models.*
import com.myapp.backend.services.SpeedTestService
import com.myapp.backend.services.SplitTunnelingService
import com.myapp.backend.config.Jwt
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.slf4j.LoggerFactory
import java.util.*

fun Route.vpnFeaturesRoutes() {
    val speedTestService = SpeedTestService.getInstance()
    val splitTunnelingService = SplitTunnelingService.getInstance()
    val logger = LoggerFactory.getLogger("VPNFeaturesRoutes")
    
    // Speed Test Routes
    route("/speedtest") {
        
        /**
         * GET /speedtest/config/{userId}
         * Get speed test configuration for a user
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
                
                val config = speedTestService.getSpeedTestConfiguration(userId)
                call.respond(HttpStatusCode.OK, config)
                
            } catch (e: Exception) {
                logger.error("Error getting speed test config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test configuration")
                )
            }
        }
        
        /**
         * POST /speedtest/result/{userId}
         * Process speed test result (no storage)
         * Requires JWT authentication
         */
        post("/result/{userId}") {
            try {
                // Extract and validate JWT token
                val token = call.request.header("Authorization")?.removePrefix("Bearer ")
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Missing authorization token")
                    )
                
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                // Validate JWT and extract user ID
                val tokenUserId = try {
                    Jwt.getUserIdFromToken(token)
                } catch (e: Exception) {
                    logger.warn("Invalid JWT token: ${e.message}")
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid authorization token")
                    )
                }
                
                // Ensure user can only access their own data
                if (tokenUserId.toString() != userId) {
                    logger.warn("User $tokenUserId attempted to access data for user $userId")
                    return@post call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Access denied")
                    )
                }
                
                val request = call.receive<SpeedTestRequest>()
                
                val response = speedTestService.processSpeedTestResult(
                    userId = userId,
                    serverId = request.serverId,
                    pingMs = request.pingMs,
                    downloadMbps = request.downloadMbps,
                    uploadMbps = request.uploadMbps
                )
                
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                logger.error("Error processing speed test result", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to process speed test result")
                )
            }
        }
        

        

        
        /**
         * GET /speedtest/ping
         * Lightweight ping endpoint for latency measurement
         * Public endpoint (no authentication required)
         */
        get("/ping") {
            try {
                // Simple ping response - just return success immediately
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "pong",
                    "timestamp" to System.currentTimeMillis()
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
        
        /**
         * GET /speedtest/download
         * Streams random data for download testing
         * Rate limited to prevent abuse
         */
        get("/download") {
            try {
                // Check rate limit
                if (!speedTestService.checkDownloadRateLimit()) {
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf("error" to "Rate limit exceeded. Try again later.")
                    )
                    return@get
                }
                
                // Increment request counter
                speedTestService.incrementDownloadRequests()
                
                val sizeParam = call.request.queryParameters["size"]?.toLongOrNull() ?: 50_000_000L // 50MB default
                val size = minOf(sizeParam, 100_000_000L) // Max 100MB
                
                logger.info("Download request: ${size} bytes")
                
                // Generate random data
                val data = speedTestService.generateRandomData(size)
                
                // Stream the response
                call.respondOutputStream(
                    contentType = ContentType.Application.OctetStream,
                    status = HttpStatusCode.OK
                ) { output ->
                    output.write(data)
                    output.flush()
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
         * Accepts binary upload for speed testing
         * Rate limited to prevent abuse
         */
        post("/upload") {
            try {
                // Check rate limit
                if (!speedTestService.checkUploadRateLimit()) {
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf("error" to "Rate limit exceeded. Try again later.")
                    )
                    return@post
                }
                
                // Increment request counter
                speedTestService.incrementUploadRequests()
                
                val startTime = System.currentTimeMillis()
                val channel = call.receiveChannel()
                
                // Read all data
                val data = channel.toByteArray()
                val uploadTime = System.currentTimeMillis() - startTime
                
                logger.info("Upload request: ${data.size} bytes in ${uploadTime}ms")
                
                // Process upload and calculate speed
                val response = speedTestService.processUpload(data, uploadTime)
                
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                logger.error("Error in upload endpoint", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Upload failed")
                )
            }
        }
        
        /**
         * GET /speedtest/statistics
         * Get speed test statistics for admin dashboard
         * Requires JWT authentication (admin only)
         */
        get("/statistics") {
            try {
                // Extract and validate JWT token
                val token = call.request.header("Authorization")?.removePrefix("Bearer ")
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Missing authorization token")
                    )
                
                // Validate JWT
                try {
                    Jwt.getUserIdFromToken(token)
                } catch (e: Exception) {
                    logger.warn("Invalid JWT token: ${e.message}")
                    return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid authorization token")
                    )
                }
                
                val statistics = speedTestService.getSpeedTestStatistics()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "statistics" to statistics
                ))
                
            } catch (e: Exception) {
                logger.error("Error getting speed test statistics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test statistics")
                )
            }
        }
    }
    
    // Split Tunneling Routes (existing functionality)
    route("/split-tunneling") {
        
        /**
         * GET /splittunneling/config/{userId}
         * Get split tunneling configuration for a user
         */
        get("/config/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val response = splitTunnelingService.getSplitTunnelingConfig(userId)
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting split tunneling config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get split tunneling configuration")
                )
            }
        }
        
        /**
         * PUT /splittunneling/config/{userId}
         * Update split tunneling settings for a user
         */
        put("/config/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<SplitTunnelingRequest>()
                
                val config = splitTunnelingService.updateSplitTunnelingConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, SplitTunnelingResponse(
                    success = true,
                    message = "Split tunneling settings updated successfully",
                    config = config
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error updating split tunneling config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    SplitTunnelingResponse(
                        success = false,
                        message = "Failed to update split tunneling configuration",
                        error = e.message
                    )
                )
            }
        }
        
        /**
         * GET /splittunneling/presets
         * Get available split tunneling presets
         */
        get("/presets") {
            try {
                val presets = splitTunnelingService.getDefaultPresets()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "presets" to presets
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting split tunneling presets", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get split tunneling presets")
                )
            }
        }
        
        /**
         * POST /splittunneling/preset/{userId}/{presetId}
         * Apply a preset to user's configuration
         */
        post("/preset/{userId}/{presetId}") {
            try {
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val presetId = call.parameters["presetId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Preset ID is required")
                )
                
                val config = splitTunnelingService.applyPreset(userId, presetId)
                
                call.respond(HttpStatusCode.OK, SplitTunnelingResponse(
                    success = true,
                    message = "Preset applied successfully",
                    config = config
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error applying preset", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    SplitTunnelingResponse(
                        success = false,
                        message = "Failed to apply preset",
                        error = e.message
                    )
                )
            }
        }
        
        /**
         * GET /splittunneling/analytics/{userId}
         * Get split tunneling analytics for a user
         */
        get("/analytics/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val periodStr = call.request.queryParameters["period"] ?: "DAY"
                val period = try {
                    AnalyticsPeriod.valueOf(periodStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    AnalyticsPeriod.DAY
                }
                
                val analytics = splitTunnelingService.getSplitTunnelingAnalytics(userId, period)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "analytics" to analytics
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting split tunneling analytics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get split tunneling analytics")
                )
            }
        }
        
        /**
         * GET /splittunneling/apps/search
         * Search apps by name or package
         */
        get("/apps/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Search query is required")
                )
                
                val apps = splitTunnelingService.searchApps(query)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "apps" to apps,
                    "count" to apps.size
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error searching apps", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to search apps")
                )
            }
        }
        
        /**
         * GET /splittunneling/apps/popular
         * Get popular apps for split tunneling
         */
        get("/apps/popular") {
            try {
                val apps = splitTunnelingService.getPopularApps()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "apps" to apps,
                    "count" to apps.size
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting popular apps", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get popular apps")
                )
            }
        }
    }
}
