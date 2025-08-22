package com.myapp.backend.routes

import com.myapp.backend.config.ServerConfig
import com.myapp.backend.models.VPNConnectionRequest
import com.myapp.backend.models.VPNConnectionError
import com.myapp.backend.services.VPNService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Route.vpnRoutes() {
    val vpnService = VPNService.getInstance()
    
    route("/vpn") {
        /**
         * GET /vpn/servers
         * Get list of all available VPN servers
         */
        get("/servers") {
            try {
                val servers = ServerConfig.getAllServers()
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "servers" to servers
                ))
            } catch (e: Exception) {
                logger.error(e) { "Error getting server list" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    VPNConnectionError(
                        error = "server_list_error",
                        message = "Failed to get server list"
                    )
                )
            }
        }

        /**
         * GET /vpn/servers/regions
         * Get VPN servers grouped by region
         */
        get("/servers/regions") {
            try {
                val serversByRegion = ServerConfig.getServersByRegion()
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "regions" to serversByRegion
                ))
            } catch (e: Exception) {
                logger.error(e) { "Error getting servers by region" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    VPNConnectionError(
                        error = "region_list_error",
                        message = "Failed to get servers by region"
                    )
                )
            }
        }

        /**
         * POST /vpn/connect
         * Create a new VPN connection
         */
        post("/connect") {
            try {
                val request = call.receive<VPNConnectionRequest>()
                logger.info { "VPN connection request for location: ${request.location}" }
                
                val response = vpnService.createConnection(request)
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "connection" to response
                ))
                
            } catch (e: IllegalArgumentException) {
                logger.warn(e) { "Invalid VPN connection request" }
                call.respond(
                    HttpStatusCode.BadRequest,
                    VPNConnectionError(
                        error = "invalid_request",
                        message = e.message ?: "Invalid request parameters"
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Error creating VPN connection" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    VPNConnectionError(
                        error = "connection_error",
                        message = "Failed to create VPN connection"
                    )
                )
            }
        }
    }
}
