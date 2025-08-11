package com.myapp.backend.routes

import com.myapp.backend.models.*
import com.myapp.backend.services.KillSwitchService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.killSwitchRoutes() {
    val killSwitchService = KillSwitchService.getInstance()
    
    route("/killswitch") {
        
        /**
         * GET /killswitch/config/{userId}
         * Get kill switch configuration for a user
         */
        get("/config/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val config = killSwitchService.getKillSwitchConfiguration(userId)
                call.respond(HttpStatusCode.OK, config)
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting kill switch config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get kill switch configuration")
                )
            }
        }
        
        /**
         * PUT /killswitch/config/{userId}
         * Update kill switch settings for a user
         */
        put("/config/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<KillSwitchConfigRequest>()
                val updatedSettings = killSwitchService.updateKillSwitchSettings(userId, request)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Kill switch settings updated successfully",
                    "settings" to updatedSettings
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error updating kill switch config", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update kill switch configuration")
                )
            }
        }
        
        /**
         * POST /killswitch/event/{userId}
         * Log a kill switch event
         */
        post("/event/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<KillSwitchEventRequest>()
                
                // Check if kill switch is allowed for this reason
                if (request.reason != null && !killSwitchService.isKillSwitchAllowed(userId, request.reason)) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        KillSwitchEventResponse(
                            success = false,
                            message = "Kill switch not allowed for this reason: ${request.reason}"
                        )
                    )
                    return@post
                }
                
                val event = killSwitchService.logKillSwitchEvent(
                    userId = userId,
                    eventType = request.eventType,
                    reason = request.reason,
                    serverEndpoint = request.serverEndpoint,
                    deviceInfo = request.deviceInfo
                )
                
                call.respond(HttpStatusCode.OK, KillSwitchEventResponse(
                    success = true,
                    message = "Kill switch event logged successfully",
                    eventId = event.id
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error logging kill switch event", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    KillSwitchEventResponse(
                        success = false,
                        message = "Failed to log kill switch event"
                    )
                )
            }
        }
        
        /**
         * GET /killswitch/events/{userId}
         * Get recent kill switch events for a user
         */
        get("/events/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val events = killSwitchService.getRecentKillSwitchEvents(userId, limit)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "events" to events,
                    "count" to events.size
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting kill switch events", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get kill switch events")
                )
            }
        }
        
        /**
         * GET /killswitch/analytics/{userId}
         * Get kill switch analytics for a user
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
                
                val analytics = killSwitchService.getKillSwitchAnalytics(userId, period)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "analytics" to analytics
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting kill switch analytics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get kill switch analytics")
                )
            }
        }
        
        /**
         * GET /killswitch/policy/{userId}
         * Get kill switch policy for a user
         */
        get("/policy/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val policy = killSwitchService.getKillSwitchPolicy(userId)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "policy" to policy
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting kill switch policy", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get kill switch policy")
                )
            }
        }
        
        /**
         * POST /killswitch/activate/{userId}
         * Manually activate kill switch for a user
         */
        post("/activate/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<Map<String, String>>()
                val reason = request["reason"] ?: "Manual activation by admin"
                val serverEndpoint = request["serverEndpoint"]
                val deviceInfo = request["deviceInfo"]?.let { deviceInfoStr ->
                    // In a real implementation, you would parse the device info JSON
                    DeviceInfo(deviceId = "admin-activated")
                }
                
                // Check if kill switch is allowed
                if (!killSwitchService.isKillSwitchAllowed(userId, reason)) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Kill switch not allowed for this reason: $reason")
                    )
                    return@post
                }
                
                val event = killSwitchService.logKillSwitchEvent(
                    userId = userId,
                    eventType = KillSwitchEventType.ACTIVATED,
                    reason = reason,
                    serverEndpoint = serverEndpoint,
                    deviceInfo = deviceInfo
                )
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Kill switch activated successfully",
                    "eventId" to event.id
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error activating kill switch", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to activate kill switch")
                )
            }
        }
        
        /**
         * POST /killswitch/deactivate/{userId}
         * Manually deactivate kill switch for a user
         */
        post("/deactivate/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "User ID is required")
                )
                
                val request = call.receive<Map<String, String>>()
                val reason = request["reason"] ?: "Manual deactivation by admin"
                val serverEndpoint = request["serverEndpoint"]
                val deviceInfo = request["deviceInfo"]?.let { deviceInfoStr ->
                    DeviceInfo(deviceId = "admin-deactivated")
                }
                
                val event = killSwitchService.logKillSwitchEvent(
                    userId = userId,
                    eventType = KillSwitchEventType.DEACTIVATED,
                    reason = reason,
                    serverEndpoint = serverEndpoint,
                    deviceInfo = deviceInfo
                )
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Kill switch deactivated successfully",
                    "eventId" to event.id
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error deactivating kill switch", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to deactivate kill switch")
                )
            }
        }
        
        /**
         * GET /killswitch/statistics
         * Get kill switch statistics for admin dashboard
         */
        get("/statistics") {
            try {
                val statistics = killSwitchService.getKillSwitchStatistics()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "statistics" to statistics
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error getting kill switch statistics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get kill switch statistics")
                )
            }
        }
        
        /**
         * POST /killswitch/cleanup
         * Clean up old events (admin only)
         */
        post("/cleanup") {
            try {
                killSwitchService.cleanupOldEvents()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Old events cleaned up successfully"
                ))
                
            } catch (e: Exception) {
                call.application.environment.log.error("Error cleaning up old events", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to clean up old events")
                )
            }
        }
    }
}
