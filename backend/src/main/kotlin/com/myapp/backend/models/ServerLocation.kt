package com.myapp.backend.models

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
