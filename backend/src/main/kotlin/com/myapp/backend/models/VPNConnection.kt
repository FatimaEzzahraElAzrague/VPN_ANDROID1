package com.myapp.backend.models

import kotlinx.serialization.Serializable

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
data class VPNConnectionError(
    val error: String,
    val message: String
)
