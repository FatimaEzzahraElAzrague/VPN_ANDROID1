#!/usr/bin/env kotlin

@file:DependsOn("io.ktor:ktor-client-core:2.3.7")
@file:DependsOn("io.ktor:ktor-client-cio:2.3.7")
@file:DependsOn("io.ktor:ktor-client-content-negotiation:2.3.7")
@file:DependsOn("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

suspend fun testEndpoint(url: String, method: String = "GET", body: String? = null): String {
    return try {
        val response = when (method.uppercase()) {
            "GET" -> client.get(url)
            "POST" -> client.post(url) { 
                if (body != null) setBody(body)
            }
            "PUT" -> client.put(url) { 
                if (body != null) setBody(body)
            }
            else -> throw IllegalArgumentException("Unsupported method: $method")
        }
        
        val responseBody = response.body<String>()
        "✅ $method $url\nStatus: ${response.status}\nResponse: $responseBody\n"
    } catch (e: Exception) {
        "❌ $method $url\nError: ${e.message}\n"
    }
}

suspend fun main() {
    val baseUrl = "http://localhost:8080"
    val testUserId = "test123"
    
    println("🧪 Testing VPN Features Backend API")
    println("=====================================\n")
    
    // Test Speed Test endpoints
    println("🚀 Testing Speed Test Endpoints:")
    println("--------------------------------")
    
    println(testEndpoint("$baseUrl/speedtest/servers"))
    println(testEndpoint("$baseUrl/speedtest/config/$testUserId"))
    println(testEndpoint("$baseUrl/speedtest/statistics"))
    
    // Test updating speed test config
    val speedTestConfig = """
        {
            "preferredServer": "https://speed.cloudflare.com/__down",
            "uploadSizeBytes": 10000000,
            "autoTestEnabled": true
        }
    """.trimIndent()
    
    println(testEndpoint("$baseUrl/speedtest/config/$testUserId", "PUT", speedTestConfig))
    
    // Test saving speed test result
    val speedTestResult = """
        {
            "pingMs": 25,
            "downloadMbps": 150.5,
            "uploadMbps": 45.2,
            "testServer": "https://speed.cloudflare.com/__down",
            "networkType": "WiFi"
        }
    """.trimIndent()
    
    println(testEndpoint("$baseUrl/speedtest/result/$testUserId", "POST", speedTestResult))
    
    // Test getting results and analytics
    println(testEndpoint("$baseUrl/speedtest/results/$testUserId?limit=5"))
    println(testEndpoint("$baseUrl/speedtest/analytics/$testUserId?period=DAY"))
    
    println("\n🔒 Testing Split Tunneling Endpoints:")
    println("-------------------------------------")
    
    println(testEndpoint("$baseUrl/splittunneling/config/$testUserId"))
    println(testEndpoint("$baseUrl/splittunneling/presets"))
    
    // Test updating split tunneling config
    val splitTunnelingConfig = """
        {
            "isEnabled": true,
            "mode": "EXCLUDE",
            "appPackages": ["com.netflix.mediaclient", "com.spotify.music"]
        }
    """.trimIndent()
    
    println(testEndpoint("$baseUrl/splittunneling/config/$testUserId", "PUT", splitTunnelingConfig))
    
    println("\n⚡ Testing Kill Switch Endpoints:")
    println("---------------------------------")
    
    println(testEndpoint("$baseUrl/killswitch/config/$testUserId"))
    println(testEndpoint("$baseUrl/killswitch/statistics"))
    
    // Test updating kill switch config
    val killSwitchConfig = """
        {
            "isEnabled": true,
            "autoReconnectEnabled": true,
            "maxReconnectionAttempts": 5
        }
    """.trimIndent()
    
    println(testEndpoint("$baseUrl/killswitch/config/$testUserId", "PUT", killSwitchConfig))
    
    println("\n🎯 Testing Complete!")
    client.close()
}
