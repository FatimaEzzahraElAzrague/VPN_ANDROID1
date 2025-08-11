package com.myapp.backend

import com.myapp.backend.config.Env
import com.myapp.backend.config.jwtConfig
import com.myapp.backend.db.DatabaseFactory
import com.myapp.backend.routes.authRoutes
import com.myapp.backend.routes.profileRoutes
import com.myapp.backend.routes.killSwitchRoutes
import com.myapp.backend.routes.vpnFeaturesRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    // Load environment first
    Env.load()

    embeddedServer(Netty, host = "0.0.0.0", port = Env.port) {
        module()
    }.start(wait = true)
    
    println("🚀 Backend started successfully!")
    println("📡 Server is accessible at:")
    println("   - Local: http://localhost:${Env.port}")
    println("   - Network: http://0.0.0.0:${Env.port}")
    println("📊 Debug endpoint: http://localhost:${Env.port}/debug/users")
}

fun Application.module() {
    Env.load()
    DatabaseFactory.init()
    jwtConfig()

    install(CallLogging)
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "internal_error"))
        }
    }

    routing {
        get("/") { call.respond(mapOf("status" to "ok")) }
        authRoutes()
        profileRoutes()
        killSwitchRoutes()
        vpnFeaturesRoutes()
    }
}


