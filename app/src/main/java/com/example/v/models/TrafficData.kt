package com.example.v.models

data class TrafficData(
    val timestamp: Long,
    val downloadSpeed: Float,
    val uploadSpeed: Float,
    val bytesReceived: Long,
    val bytesSent: Long
)

data class TrafficHealth(
    val status: HealthStatus,
    val score: Int, // 0-100
    val description: String
)

enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}

