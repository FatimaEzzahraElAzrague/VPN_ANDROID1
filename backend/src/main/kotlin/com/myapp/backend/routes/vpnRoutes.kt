package com.myapp.backend.routes

import com.myapp.backend.models.*
import com.myapp.backend.services.VPNService
import com.myapp.backend.config.JwtProvider
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.vpnRoutes() {
    val vpnService = VPNService.getInstance()
    val logger = LoggerFactory.getLogger("VPNRoutes")
    
    // VPN Routes
    route("/vpn") {
        
        /**
         * GET /vpn/servers
         * Get available VPN servers
         * Public endpoint (no authentication required)
         */
        get("/servers") {
            try {
                val servers = vpnService.getAvailableVPNServers()
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "servers" to servers
                ))
            } catch (e: Exception) {
                logger.error("Error getting VPN servers", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get VPN servers")
                )
            }
        }
        
        /**
         * GET /vpn/config/{userId}
         * Get VPN configuration for a user (Osaka and Paris servers)
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
                    JwtProvider.getUserIdFromToken(token)
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
                
                // Get VPN configurations for both Osaka and Paris servers
                val response = vpnService.getVPNConfigurationsForUser(userId)
                
                if (response.success) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.BadRequest, response)
                }
                
            } catch (e: Exception) {
                logger.error("Error getting VPN configuration", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get VPN configuration")
                )
            }
        }
        
        /**
         * GET /vpn/config/{userId}/minimal
         * Get minimal VPN configuration for Android app (exact format needed)
         * Requires JWT authentication
         */
        get("/config/{userId}/minimal") {
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
                
                // Get minimal VPN configuration for Android app
                val response = vpnService.getMinimalVPNConfigForUser(userId)
                
                if (response.success) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.BadRequest, response)
                }
                
            } catch (e: Exception) {
                logger.error("Error getting minimal VPN configuration", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get VPN configuration")
                )
            }
        }
        
        /**
         * POST /vpn/config/{userId}/{serverId}
         * Request VPN configuration with custom parameters
         * Requires JWT authentication
         */
        post("/config/{userId}/{serverId}") {
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
                
                val serverId = call.parameters["serverId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Server ID is required")
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
                
                // Parse request body
                val request = call.receive<VPNConfigRequest>()
                
                // Get VPN configuration
                val response = vpnService.getVPNConfiguration(userId, serverId)
                
                if (response.success) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.BadRequest, response)
                }
                
            } catch (e: Exception) {
                logger.error("Error requesting VPN configuration", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to request VPN configuration")
                )
            }
        }
        
        /**
         * POST /vpn/connection/{userId}/{serverId}
         * Update VPN connection status
         * Requires JWT authentication
         */
        post("/connection/{userId}/{serverId}") {
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
                
                val serverId = call.parameters["serverId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Server ID is required")
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
                
                // Parse request body
                val request = call.receive<VPNConnectionRequest>()
                
                // Update connection status
                vpnService.updateConnectionStatus(
                    userId = userId,
                    serverId = serverId,
                    status = request.action,
                    error = null,
                    bytesReceived = 0,
                    bytesSent = 0
                )
                
                // Get updated connection status
                val status = vpnService.getConnectionStatus(userId, serverId)
                
                val response = VPNConnectionResponse(
                    success = true,
                    message = "Connection status updated successfully",
                    status = status
                )
                
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                logger.error("Error updating VPN connection status", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update connection status")
                )
            }
        }
        
        /**
         * GET /vpn/connection/{userId}/{serverId}
         * Get VPN connection status
         * Requires JWT authentication
         */
        get("/connection/{userId}/{serverId}") {
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
                
                val serverId = call.parameters["serverId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Server ID is required")
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
                
                // Get connection status
                val status = vpnService.getConnectionStatus(userId, serverId)
                
                if (status != null) {
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "status" to status
                    ))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf(
                        "success" to false,
                        "error" to "Connection status not found"
                    ))
                }
                
            } catch (e: Exception) {
                logger.error("Error getting VPN connection status", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get connection status")
                )
            }
        }
        
        /**
         * GET /vpn/statistics
         * Get VPN server statistics
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
                
                val statistics = vpnService.getVPNServerStatistics()
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "statistics" to statistics
                ))
                
            } catch (e: Exception) {
                logger.error("Error getting VPN statistics", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to get VPN statistics")
                )
            }
        }
    }
}
