package com.example.v.data

/**
 * Server status from backend
 */
data class ServerStatus(
    val serverId: String,
    val status: String, // "online", "offline", "maintenance"
    val uptime: Long,
    val load: Double,
    val connections: Int,
    val lastChecked: Long
)

/**
 * Server ping result from backend
 */
data class PingResult(
    val serverId: String,
    val latency: Int, // in milliseconds
    val timestamp: Long,
    val method: String // "udp", "http", "ping"
)
