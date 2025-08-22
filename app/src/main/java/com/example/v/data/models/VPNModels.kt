package com.example.v.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerLocation(
    val id: String,
    val name: String,
    val country: String,
    val ip: String,
    val subnet: String,
    val agentPort: Int,
    val serverPublicKey: String,
    val listenPort: String,
    val ipv6Base: String,
    val region: String
)

@Serializable
data class VPNConnectionRequest(
    val location: String,
    val adBlockEnabled: Boolean = false,
    val antiMalwareEnabled: Boolean = false,
    val familySafeModeEnabled: Boolean = false
)

@Serializable
data class VPNConnectionResponse(
    val privateKey: String,
    val publicKey: String,
    val serverPublicKey: String,
    val serverEndpoint: String,
    val allowedIPs: String,
    val internalIP: String,
    val dns: String,
    val mtu: Int = 1420,
    val presharedKey: String? = null,
    val internalIPv6: String? = null,
    val clientConfig: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val message: String,
    val user: Map<String, String>
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

enum class VPNConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}
