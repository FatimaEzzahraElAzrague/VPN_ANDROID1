package com.example.v.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

/**
 * Data class to hold speed test results
 */
data class SpeedTestResult(
    val pingMs: Long,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toSpeedTestResults(): com.example.v.screens.SpeedTestResults {
        return com.example.v.screens.SpeedTestResults(
            downloadSpeed = downloadMbps.toFloat(),
            uploadSpeed = uploadMbps.toFloat(),
            ping = pingMs.toInt(),
            jitter = 0 // Will be calculated separately if needed
        )
    }
}

/**
 * Speed test engine using Ktor client with CIO engine
 */
object SpeedTestEngine {
    
    private const val PING_TIMEOUT_MS = 10000L
    private const val DOWNLOAD_TIMEOUT_MS = 30000L
    private const val UPLOAD_TIMEOUT_MS = 30000L
    private const val CHUNK_SIZE = 8192 // 8KB chunks for download
    private const val DEFAULT_UPLOAD_SIZE_BYTES = 5_000_000 // 5MB default
    
    /**
     * Measure ping to a server using a small GET request
     * @param url The server URL to ping
     * @return Ping time in milliseconds
     */
    suspend fun measurePing(url: String): Long = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = PING_TIMEOUT_MS
            }
        }
        
        try {
            val pingTime = measureTimeMillis {
                withTimeoutOrNull(PING_TIMEOUT_MS) {
                    client.get(url) {
                        // Use a small endpoint for ping test
                        url {
                            takeFrom(url)
                            // Add a small parameter to avoid caching
                            parameters.append("ping", System.currentTimeMillis().toString())
                        }
                    }
                }
            }
            
            // If timeout occurred, return a high value
            if (pingTime >= PING_TIMEOUT_MS) {
                return@withContext PING_TIMEOUT_MS
            }
            
            pingTime
        } finally {
            client.close()
        }
    }
    
    /**
     * Measure download speed by downloading a file in chunks
     * @param url The URL of the file to download
     * @return Download speed in Mbps
     */
    suspend fun measureDownloadSpeed(url: String): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = DOWNLOAD_TIMEOUT_MS
            }
        }
        
        try {
            val startTime = System.currentTimeMillis()
            val bytesReceived = AtomicLong(0)
            
            withTimeoutOrNull(DOWNLOAD_TIMEOUT_MS) {
                val channel = client.get(url) {}.bodyAsChannel()
                try {
                    val buffer = ByteArray(CHUNK_SIZE)
                    
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead == -1) break
                        
                        bytesReceived.addAndGet(bytesRead.toLong())
                        
                        // Stop if we've received enough data (e.g., 10MB)
                        if (bytesReceived.get() >= 10_000_000) break
                    }
                } finally {
                    channel.cancel()
                }
            }
            
            val endTime = System.currentTimeMillis()
            val durationMs = (endTime - startTime).coerceAtLeast(1)
            val totalBytes = bytesReceived.get()
            
            // Calculate Mbps: (bytes * 8) / (duration_ms / 1000) / 1_000_000
            val mbps = (totalBytes * 8.0) / (durationMs / 1000.0) / 1_000_000.0
            
            // Round to 2 decimal places
            (mbps * 100).roundToInt() / 100.0
        } finally {
            client.close()
        }
    }
    
    /**
     * Measure upload speed by uploading random data
     * @param url The URL to upload to
     * @param sizeBytes Size of data to upload in bytes
     * @return Upload speed in Mbps
     */
    suspend fun measureUploadSpeed(url: String, sizeBytes: Int = DEFAULT_UPLOAD_SIZE_BYTES): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = UPLOAD_TIMEOUT_MS
            }
        }
        
        try {
            val startTime = System.currentTimeMillis()
            
            withTimeoutOrNull(UPLOAD_TIMEOUT_MS) {
                client.post(url) {
                    setBody(generateRandomData(sizeBytes))
                    contentType(ContentType.Application.OctetStream)
                }
            }
            
            val endTime = System.currentTimeMillis()
            val durationMs = (endTime - startTime).coerceAtLeast(1)
            
            // Calculate Mbps: (bytes * 8) / (duration_ms / 1000) / 1_000_000
            val mbps = (sizeBytes * 8.0) / (durationMs / 1000.0) / 1_000_000.0
            
            // Round to 2 decimal places
            (mbps * 100).roundToInt() / 100.0
        } finally {
            client.close()
        }
    }
    
    /**
     * Run a complete speed test with all measurements
     * @param testServer The test server URL
     * @param uploadSizeBytes Size of data to upload (default 5MB)
     * @return Complete speed test results
     */
    suspend fun runSpeedTest(
        testServer: String,
        uploadSizeBytes: Int = DEFAULT_UPLOAD_SIZE_BYTES
    ): SpeedTestResult {
        return withContext(Dispatchers.IO) {
            // Measure ping first
            val pingMs = measurePing(testServer)
            
            // Measure download speed
            val downloadMbps = measureDownloadSpeed(testServer)
            
            // Measure upload speed
            val uploadMbps = measureUploadSpeed(testServer, uploadSizeBytes)
            
            SpeedTestResult(
                pingMs = pingMs,
                downloadMbps = downloadMbps,
                uploadMbps = uploadMbps
            )
        }
    }
    
    /**
     * Run speed test with progress updates
     * @param testServer The test server URL
     * @param onProgress Callback for progress updates
     * @return Complete speed test results
     */
    suspend fun runSpeedTestWithProgress(
        testServer: String,
        onProgress: (String, Float) -> Unit
    ): SpeedTestResult {
        return withContext(Dispatchers.IO) {
            onProgress("Measuring ping...", 0.1f)
            val pingMs = measurePing(testServer)
            
            onProgress("Testing download speed...", 0.4f)
            val downloadMbps = measureDownloadSpeed(testServer)
            
            onProgress("Testing upload speed...", 0.7f)
            val uploadMbps = measureUploadSpeed(testServer)
            
            onProgress("Test completed!", 1.0f)
            
            SpeedTestResult(
                pingMs = pingMs,
                downloadMbps = downloadMbps,
                uploadMbps = uploadMbps
            )
        }
    }
    
    /**
     * Generate random binary data for upload testing
     * @param sizeBytes Size of data to generate
     * @return ByteArray of random data
     */
    private fun generateRandomData(sizeBytes: Int): ByteArray {
        val data = ByteArray(sizeBytes)
        val random = java.util.Random()
        random.nextBytes(data)
        return data
    }
    
    /**
     * Get recommended test servers
     */
    fun getTestServers(): List<String> {
        return listOf(
            "https://speed.cloudflare.com/__down",
            "https://speedtest.selectel.ru/1000MB.bin",
            "https://speed.hetzner.de/1GB.bin",
            "https://speedtest.ftp.otenet.gr/files/test1Gb.db"
        )
    }
    
    /**
     * Get upload test endpoints
     */
    fun getUploadTestEndpoints(): List<String> {
        return listOf(
            "https://httpbin.org/post",
            "https://postman-echo.com/post",
            "https://httpbin.org/delay/1"
        )
    }
}
