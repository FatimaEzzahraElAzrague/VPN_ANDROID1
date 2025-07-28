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

val mockTrafficData = listOf(
    TrafficData(System.currentTimeMillis() - 300000, 45.2f, 12.1f, 1024000, 512000),
    TrafficData(System.currentTimeMillis() - 240000, 52.8f, 15.3f, 1536000, 768000),
    TrafficData(System.currentTimeMillis() - 180000, 38.9f, 9.7f, 896000, 448000),
    TrafficData(System.currentTimeMillis() - 120000, 61.4f, 18.2f, 1792000, 896000),
    TrafficData(System.currentTimeMillis() - 60000, 47.6f, 13.8f, 1280000, 640000),
    TrafficData(System.currentTimeMillis(), 55.3f, 16.5f, 1600000, 800000)
)