package com.example.v.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

/**
 * Real-time speed test results for live UI updates
 */
data class RealTimeSpeedTestResult(
    val pingMs: Long,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val testPhase: TestPhase,
    val progress: Float,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toSpeedTestResults(): com.example.v.screens.SpeedTestResults {
        return com.example.v.screens.SpeedTestResults(
            downloadSpeed = downloadMbps.toFloat(),
            uploadSpeed = uploadMbps.toFloat(),
            ping = pingMs.toInt(),
            jitter = 0
        )
    }
}

enum class TestPhase {
    IDLE, PING, DOWNLOAD, UPLOAD, COMPLETED, ERROR
}

/**
 * Real-time speed test engine that provides live updates
 */
object RealTimeSpeedTest {
    
    private const val PING_TIMEOUT_MS = 10000L
    private const val DOWNLOAD_TIMEOUT_MS = 30000L
    private const val UPLOAD_TIMEOUT_MS = 30000L
    private const val CHUNK_SIZE = 8192
    private const val DOWNLOAD_TARGET_BYTES = 10_000_000 // 10MB
    private const val UPLOAD_SIZE_BYTES = 5_000_000 // 5MB
    
    /**
     * Run a complete speed test with real-time updates
     * @param testServer The test server URL
     * @return Flow of real-time results
     */
    fun runRealTimeSpeedTest(testServer: String): Flow<RealTimeSpeedTestResult> = flow {
        var currentPing = 0L
        var currentDownload = 0.0
        var currentUpload = 0.0
        
        try {
            // Phase 1: Ping Test
            emit(RealTimeSpeedTestResult(
                pingMs = 0,
                downloadMbps = 0.0,
                uploadMbps = 0.0,
                testPhase = TestPhase.PING,
                progress = 0.1f,
                message = "Measuring ping..."
            ))
            
            currentPing = measurePingRealTime(testServer) { progress ->
                emit(RealTimeSpeedTestResult(
                    pingMs = currentPing,
                    downloadMbps = currentDownload,
                    uploadMbps = currentUpload,
                    testPhase = TestPhase.PING,
                    progress = 0.1f + (progress * 0.2f),
                    message = "Measuring ping... ${(progress * 100).toInt()}%"
                ))
            }
            
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = currentUpload,
                testPhase = TestPhase.PING,
                progress = 0.3f,
                message = "Ping: ${currentPing}ms"
            ))
            
            // Phase 2: Download Test
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = 0.0,
                uploadMbps = 0.0,
                testPhase = TestPhase.DOWNLOAD,
                progress = 0.3f,
                message = "Testing download speed..."
            ))
            
            currentDownload = measureDownloadSpeedRealTime(testServer) { progress, currentSpeed ->
                currentDownload = currentSpeed
                emit(RealTimeSpeedTestResult(
                    pingMs = currentPing,
                    downloadMbps = currentSpeed,
                    uploadMbps = currentUpload,
                    testPhase = TestPhase.DOWNLOAD,
                    progress = 0.3f + (progress * 0.4f),
                    message = "Download: ${String.format("%.1f", currentSpeed)} Mbps"
                ))
            }
            
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = currentUpload,
                testPhase = TestPhase.DOWNLOAD,
                progress = 0.7f,
                message = "Download: ${String.format("%.1f", currentDownload)} Mbps"
            ))
            
            // Phase 3: Upload Test
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = 0.0,
                testPhase = TestPhase.UPLOAD,
                progress = 0.7f,
                message = "Testing upload speed..."
            ))
            
            currentUpload = measureUploadSpeedRealTime(testServer) { progress, currentSpeed ->
                currentUpload = currentSpeed
                emit(RealTimeSpeedTestResult(
                    pingMs = currentPing,
                    downloadMbps = currentDownload,
                    uploadMbps = currentSpeed,
                    testPhase = TestPhase.UPLOAD,
                    progress = 0.7f + (progress * 0.3f),
                    message = "Upload: ${String.format("%.1f", currentSpeed)} Mbps"
                ))
            }
            
            // Final result
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = currentUpload,
                testPhase = TestPhase.COMPLETED,
                progress = 1.0f,
                message = "Test completed!"
            ))
            
        } catch (e: Exception) {
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = currentUpload,
                testPhase = TestPhase.ERROR,
                progress = 0.0f,
                message = "Error: ${e.message}"
            ))
        }
    }
    
    /**
     * Measure ping with real-time updates
     */
    private suspend fun measurePingRealTime(
        url: String,
        onProgress: suspend (Float) -> Unit
    ): Long = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = PING_TIMEOUT_MS
            }
        }
        
        try {
            onProgress(0.5f)
            
            val pingTime = measureTimeMillis {
                withTimeoutOrNull(PING_TIMEOUT_MS) {
                    client.get(url) {
                        url {
                            takeFrom(url)
                            parameters.append("ping", System.currentTimeMillis().toString())
                        }
                    }
                }
            }
            
            onProgress(1.0f)
            
            if (pingTime >= PING_TIMEOUT_MS) {
                PING_TIMEOUT_MS
            } else {
                pingTime
            }
        } finally {
            client.close()
        }
    }
    
    /**
     * Measure download speed with real-time updates
     */
    private suspend fun measureDownloadSpeedRealTime(
        url: String,
        onProgress: suspend (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = DOWNLOAD_TIMEOUT_MS
            }
        }
        
        try {
            val startTime = System.currentTimeMillis()
            val bytesReceived = AtomicLong(0)
            var lastUpdateTime = startTime
            
            withTimeoutOrNull(DOWNLOAD_TIMEOUT_MS) {
                val channel = client.get(url) {}.bodyAsChannel()
                try {
                    val buffer = ByteArray(CHUNK_SIZE)
                    
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead == -1) break
                        
                        bytesReceived.addAndGet(bytesRead.toLong())
                        
                        val currentTime = System.currentTimeMillis()
                        val totalBytes = bytesReceived.get()
                        
                        // Update progress every 100ms
                        if (currentTime - lastUpdateTime > 100) {
                            val progress = (totalBytes.toFloat() / DOWNLOAD_TARGET_BYTES).coerceAtMost(1.0f)
                            val durationMs = (currentTime - startTime).coerceAtLeast(1)
                            val currentSpeed = (totalBytes * 8.0) / (durationMs / 1000.0) / 1_000_000.0
                            
                            onProgress(progress, currentSpeed)
                            lastUpdateTime = currentTime
                        }
                        
                        // Stop if we've received enough data
                        if (totalBytes >= DOWNLOAD_TARGET_BYTES) break
                    }
                } finally {
                    channel.cancel()
                }
            }
            
            val endTime = System.currentTimeMillis()
            val durationMs = (endTime - startTime).coerceAtLeast(1)
            val totalBytes = bytesReceived.get()
            
            val mbps = (totalBytes * 8.0) / (durationMs / 1000.0) / 1_000_000.0
            (mbps * 100).roundToInt() / 100.0
        } finally {
            client.close()
        }
    }
    
    /**
     * Measure upload speed with real-time updates
     */
    private suspend fun measureUploadSpeedRealTime(
        url: String,
        onProgress: suspend (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = UPLOAD_TIMEOUT_MS
            }
        }
        
        try {
            val startTime = System.currentTimeMillis()
            val uploadData = generateRandomData(UPLOAD_SIZE_BYTES)
            
            withTimeoutOrNull(UPLOAD_TIMEOUT_MS) {
                client.post(url) {
                    setBody(uploadData)
                    contentType(ContentType.Application.OctetStream)
                }
            }
            
            val endTime = System.currentTimeMillis()
            val durationMs = (endTime - startTime).coerceAtLeast(1)
            
            val mbps = (UPLOAD_SIZE_BYTES * 8.0) / (durationMs / 1000.0) / 1_000_000.0
            val finalSpeed = (mbps * 100).roundToInt() / 100.0
            
            // Simulate progress updates for upload (since it's a single request)
            onProgress(0.5f, finalSpeed * 0.5)
            onProgress(1.0f, finalSpeed)
            
            finalSpeed
        } finally {
            client.close()
        }
    }
    
    /**
     * Generate random binary data for upload testing
     */
    private fun generateRandomData(sizeBytes: Int): ByteArray {
        val data = ByteArray(sizeBytes)
        val random = java.util.Random()
        random.nextBytes(data)
        return data
    }
    
    /**
     * Get recommended test servers optimized for speed testing
     */
    fun getOptimizedTestServers(): List<String> {
        return listOf(
            "https://speed.cloudflare.com/__down",
            "https://speedtest.selectel.ru/1000MB.bin",
            "https://speed.hetzner.de/1GB.bin",
            "https://speedtest.ftp.otenet.gr/files/test1Gb.db",
            "https://speedtest.tele2.net/1GB.zip"
        )
    }
    
    /**
     * Get upload test endpoints
     */
    fun getUploadEndpoints(): List<String> {
        return listOf(
            "https://httpbin.org/post",
            "https://postman-echo.com/post",
            "https://httpbin.org/delay/1"
        )
    }
}
