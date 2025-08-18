package com.example.v.data

import android.annotation.SuppressLint
import com.example.v.config.VPNConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class VPNConnectionRequest(
    val location: String,
    val ad_block_enabled: Boolean = false,
    val anti_malware_enabled: Boolean = false,
    val family_safe_mode_enabled: Boolean = false
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class VPNConnectionResponse(
    val private_key: String,
    val public_key: String,
    val server_public_key: String,
    val server_endpoint: String,
    val allowed_ips: String,
    val internal_ip: String,
    val dns: String,
    val mtu: Int,
    val preshared_key: String?,
    val internal_ipv6: String?,
    val client_config: String?
)

class VPNApiService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    suspend fun connectVPN(
        location: String,
        adBlockEnabled: Boolean = false,
        malwareBlockEnabled: Boolean = false,
        familyModeEnabled: Boolean = false
    ): VPNConnectionResponse {
        val request = VPNConnectionRequest(
            location = location,
            ad_block_enabled = adBlockEnabled,
            anti_malware_enabled = malwareBlockEnabled,
            family_safe_mode_enabled = familyModeEnabled
        )
        
        return client.post("${VPNConfig.VPNApiConfig.BASE_URL}/vpn/connect") {
            header("Authorization", "Bearer ${VPNConfig.VPNApiConfig.AGENT_TOKEN}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
