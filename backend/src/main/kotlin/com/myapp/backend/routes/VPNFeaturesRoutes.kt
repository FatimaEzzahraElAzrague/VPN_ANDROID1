package com.myapp.backend.routes

import com.myapp.backend.models.*
import com.myapp.backend.services.SpeedTestService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.vpnFeaturesRoutes() {
    val speedTestService = SpeedTestService.getInstance()
    
    // Speed Test Routes
    route("/speedtest") {
        
        /**
         * GET /speedtest/config/{userId}
         * Get speed test configuration for a user
         */
        get("/config/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val config = speedTestService.getSpeedTestConfiguration(userId)
                call.respond(HttpStatusCode.OK, config)
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting speed test config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test configuration")
                )
            }
        }
        
        /**
         * PUT /speedtest/config/{userId}
         * Update speed test settings for a user
         */
        put("/config/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<SpeedTestConfigRequest>()
                val updatedConfig = speedTestService.updateSpeedTestConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Speed test settings updated successfully",
                    "config" to updatedConfig
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error updating speed test config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update speed test configuration")
                )
            }
        }
        
        /**
         * POST /speedtest/result/{userId}
         * Save speed test result
         */
        post("/result/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<Map<String, Any>>()
                
                val result = speedTestService.saveSpeedTestResult(
                    userId = userId,
                    pingMs = (request["pingMs"] as? Number)?.toLong() ?: 0L,
                    downloadMbps = (request["downloadMbps"] as? Number)?.toDouble() ?: 0.0,
                    uploadMbps = (request["uploadMbps"] as? Number)?.toDouble() ?: 0.0,
                    jitterMs = (request["jitterMs"] as? Number)?.toLong(),
                    testServer = request["testServer"] as? String ?: "",
                    deviceInfo = null, // Parse device info if needed
                    networkType = request["networkType"] as? String
                )
                
                call.respond(HttpStatusCode.OK, SpeedTestResponse(
                    success = true,
                    message = "Speed test result saved successfully",
                    result = result
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error saving speed test result", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    SpeedTestResponse(
                        success = false,
                        message = "Failed to save speed test result",
                        error = e.message
                    )
                )
            }
        }
        
        /**
         * GET /speedtest/results/{userId}
         * Get recent speed test results for a user
         */
        get("/results/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val results = speedTestService.getRecentSpeedTestResults(userId, limit)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "results" to results,
                    "count" to results.size
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting speed test results", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test results")
                )
            }
        }
        
        /**
         * GET /speedtest/analytics/{userId}
         * Get speed test analytics for a user
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
                
                val analytics = speedTestService.getSpeedTestAnalytics(userId, period)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "analytics" to analytics
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting speed test analytics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test analytics")
                )
            }
        }
        
        /**
         * GET /speedtest/servers
         * Get available speed test servers
         */
        get("/servers") {
            try {
                val servers = speedTestService.getAvailableServers()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "servers" to servers
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting speed test servers", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test servers")
                )
            }
        }
        
        /**
         * GET /speedtest/statistics
         * Get speed test statistics for admin dashboard
         */
        get("/statistics") {
            try {
                val statistics = speedTestService.getSpeedTestStatistics()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "statistics" to statistics
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting speed test statistics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get speed test statistics")
                )
            }
        }
    }
    
    // Split Tunneling Routes
    route("/splittunneling") {
        
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
                
                // For now, return a basic response - implement actual service later
                val config = SplitTunnelingConfig(userId = userId)
                val response = SplitTunnelingConfigResponse(
                    config = config,
                    availableApps = emptyList(),
                    presets = emptyList()
                )
                
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
                
                // For now, return a basic response - implement actual service later
                val config = SplitTunnelingConfig(
                    userId = userId,
                    isEnabled = request.isEnabled ?: false,
                    mode = request.mode ?: SplitTunnelingMode.EXCLUDE,
                    appPackages = request.appPackages ?: emptyList()
                )
                
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
                // Return some example presets
                val presets = listOf(
                    SplitTunnelingPreset(
                        id = "banking",
                        name = "Banking Apps Only",
                        description = "Only banking apps use VPN",
                        mode = SplitTunnelingMode.INCLUDE,
                        appPackages = listOf("com.chase.smartphone", "com.wellsfargo.mobile"),
                        category = "Security"
                    ),
                    SplitTunnelingPreset(
                        id = "streaming",
                        name = "Exclude Streaming",
                        description = "All apps use VPN except streaming apps",
                        mode = SplitTunnelingMode.EXCLUDE,
                        appPackages = listOf("com.netflix.mediaclient", "com.spotify.music"),
                        category = "Entertainment"
                    )
                )
                
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
    }
}
