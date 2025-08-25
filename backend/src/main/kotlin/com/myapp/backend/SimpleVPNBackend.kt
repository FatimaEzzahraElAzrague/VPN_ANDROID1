package com.myapp.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.content.*
import java.util.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/health") {
                call.respondText("VPN Backend is running!")
            }
            
            // Simple authentication endpoints for testing
            post("/signup") {
                call.respond(mapOf("message" to "OTP sent"))
            }
            
            post("/verify-otp") {
                call.respond(mapOf(
                    "access_token" to "test_token_${UUID.randomUUID()}",
                    "expires_in" to 3600
                ))
            }
            
            post("/login") {
                call.respond(mapOf(
                    "access_token" to "test_token_${UUID.randomUUID()}",
                    "expires_in" to 3600
                ))
            }
            
            // VPN configuration endpoint (no JWT validation for quick testing)
            get("/vpn/config/{userId}/minimal") {
                val userId = call.parameters["userId"] ?: "1"
                
                // Generate simple configs for testing
                val osakaConfig = mapOf(
                    "server_ip" to "56.155.92.31",
                    "server_port" to 51820,
                    "server_public_key" to "eB9FEadcCaOM/DHDqJfGDfC8r1t4UZjUBxBtJdY2720=",
                    "client_private_key" to generateRandomKey(),
                    "client_public_key" to generateRandomKey(),
                    "allowed_ips" to "0.0.0.0/0",
                    "dns" to "8.8.8.8"
                )
                
                val parisConfig = mapOf(
                    "server_ip" to "52.47.190.220",
                    "server_port" to 51820,
                    "server_public_key" to "MOHIWdozScbRm4C0V5W6u/7Z6/VY8l4DeeFnBSvW03I=",
                    "client_private_key" to generateRandomKey(),
                    "client_public_key" to generateRandomKey(),
                    "allowed_ips" to "0.0.0.0/0",
                    "dns" to "8.8.8.8"
                )
                
                val response = mapOf(
                    "success" to true,
                    "message" to "Minimal VPN configurations generated successfully",
                    "configs" to mapOf(
                        "osaka" to osakaConfig,
                        "paris" to parisConfig
                    )
                )
                
                call.respond(response)
            }
            
            // Add a simple VPN connection endpoint
            post("/vpn/connection/{userId}/{serverId}") {
                val userId = call.parameters["userId"] ?: "1"
                val serverId = call.parameters["serverId"] ?: "osaka"
                
                call.respond(mapOf(
                    "success" to true,
                    "message" to "Connection status updated successfully",
                    "status" to "connected"
                ))
            }
        }
    }.start(wait = true)
}

private fun generateRandomKey(): String {
    val bytes = ByteArray(32)
    Random().nextBytes(bytes)
    return Base64.getEncoder().encodeToString(bytes)
}
