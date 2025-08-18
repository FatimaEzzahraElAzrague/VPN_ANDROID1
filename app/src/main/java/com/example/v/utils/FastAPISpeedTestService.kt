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
import java.io.File
import java.io.FileOutputStream
import java.util.Random

/**
 * FastAPI Speed Test Service for VPN speed testing
 * Integrates with Osaka (56.155.92.31:8000) and Paris (52.47.190.220:8000) servers
 */
object FastAPISpeedTestService {
    
    // Server configurations
    private const val OSAKA_SERVER = "http://56.155.92.31:8000"
    private const val PARIS_SERVER = "http://52.47.190.220:8000"
    
    // Test configurations
    private const val PING_TIMEOUT_MS = 10000L
    private const val DOWNLOAD_TIMEOUT_MS = 60000L
    private const val UPLOAD_TIMEOUT_MS = 60000L
    private const val CHUNK_SIZE = 8192
    private const val DOWNLOAD_SIZE_MB = 10 // 10MB for faster testing
    private const val UPLOAD_SIZE_MB = 5    // 5MB for faster testing
    
    /**
     * Server information for speed testing
     */
    data class SpeedTestServer(
        val name: String,
        val url: String,
        val location: String,
        val ip: String
    )
    
    /**
     * Get available speed test servers
     */
    fun getSpeedTestServers(): List<SpeedTestServer> = listOf(
        SpeedTestServer("Osaka", OSAKA_SERVER, "Osaka, Japan", "56.155.92.31"),
        SpeedTestServer("Paris", PARIS_SERVER, "Paris, France", "52.47.190.220")
    )
    
    /**
     * Run a complete speed test with real-time updates
     * @param server The test server to use
     * @return Flow of real-time results
     */
    fun runSpeedTest(server: SpeedTestServer): Flow<RealTimeSpeedTestResult> = flow {
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
                message = "Measuring ping to ${server.location}..."
            ))
            
            currentPing = measurePing(server.url)
            
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = 0.0,
                uploadMbps = 0.0,
                testPhase = TestPhase.PING,
                progress = 0.3f,
                message = "Ping: ${currentPing}ms to ${server.location}"
            ))
            
            // Phase 2: Download Test
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = 0.0,
                uploadMbps = 0.0,
                testPhase = TestPhase.DOWNLOAD,
                progress = 0.3f,
                message = "Testing download speed from ${server.location}..."
            ))
            
            currentDownload = measureDownloadSpeed(server.url, DOWNLOAD_SIZE_MB) { progress, currentSpeed ->
                currentDownload = currentSpeed
                emit(RealTimeSpeedTestResult(
                    pingMs = currentPing,
                    downloadMbps = currentSpeed,
                    uploadMbps = currentUpload,
                    testPhase = TestPhase.DOWNLOAD,
                    progress = 0.3f + (progress * 0.4f),
                    message = "Download: ${String.format("%.1f", currentSpeed)} Mbps from ${server.location}"
                ))
            }
            
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = 0.0,
                testPhase = TestPhase.DOWNLOAD,
                progress = 0.7f,
                message = "Download: ${String.format("%.1f", currentDownload)} Mbps from ${server.location}"
            ))
            
            // Phase 3: Upload Test
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = 0.0,
                testPhase = TestPhase.UPLOAD,
                progress = 0.7f,
                message = "Testing upload speed to ${server.location}..."
            ))
            
            currentUpload = measureUploadSpeed(server.url, UPLOAD_SIZE_MB) { progress, currentSpeed ->
                currentUpload = currentSpeed
                emit(RealTimeSpeedTestResult(
                    pingMs = currentPing,
                    downloadMbps = currentDownload,
                    uploadMbps = currentSpeed,
                    testPhase = TestPhase.UPLOAD,
                    progress = 0.7f + (progress * 0.3f),
                    message = "Upload: ${String.format("%.1f", currentSpeed)} Mbps to ${server.location}"
                ))
            }
            
            // Final result
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = currentUpload,
                testPhase = TestPhase.COMPLETED,
                progress = 1.0f,
                message = "Speed test completed for ${server.location}!"
            ))
            
        } catch (e: Exception) {
            emit(RealTimeSpeedTestResult(
                pingMs = currentPing,
                downloadMbps = currentDownload,
                uploadMbps = currentUpload,
                testPhase = TestPhase.ERROR,
                progress = 0.0f,
                message = "Error testing ${server.location}: ${e.message}"
            ))
        }
    }
    
    /**
     * Measure ping using FastAPI /ping endpoint
     */
    private suspend fun measurePing(serverUrl: String): Long = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = PING_TIMEOUT_MS
            }
        }
        
        try {
            val pingTime = measureTimeMillis {
                withTimeoutOrNull(PING_TIMEOUT_MS) {
                    client.get("$serverUrl/ping")
                }
            }
            
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
     * Measure download speed using FastAPI /download endpoint
     */
    private suspend fun measureDownloadSpeed(
        serverUrl: String,
        sizeMB: Int,
        onProgress: suspend (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = DOWNLOAD_TIMEOUT_MS
            }
        }
        
        try {
            val sizeBytes = sizeMB * 1024 * 1024
            val startTime = System.currentTimeMillis()
            val bytesReceived = AtomicLong(0)
            var lastUpdateTime = startTime
            
            withTimeoutOrNull(DOWNLOAD_TIMEOUT_MS) {
                val response = client.get("$serverUrl/download?size=$sizeBytes")
                val channel = response.bodyAsChannel()
                
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
                            val progress = (totalBytes.toFloat() / sizeBytes).coerceAtMost(1.0f)
                            val durationMs = (currentTime - startTime).coerceAtLeast(1)
                            val currentSpeed = (totalBytes * 8.0) / (durationMs / 1000.0) / 1_000_000.0
                            
                            onProgress(progress, currentSpeed)
                            lastUpdateTime = currentTime
                        }
                        
                        // Stop if we've received enough data
                        if (totalBytes >= sizeBytes) break
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
     * Measure upload speed using FastAPI /upload endpoint
     */
    private suspend fun measureUploadSpeed(
        serverUrl: String,
        sizeMB: Int,
        onProgress: suspend (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = UPLOAD_TIMEOUT_MS
            }
        }
        
        try {
            val sizeBytes = sizeMB * 1024 * 1024
            val startTime = System.currentTimeMillis()
            
            // Create temporary test file
            val tempFile = createTempFile("speedtest", ".bin")
            try {
                // Fill file with random data
                val random = Random()
                val fileOutputStream = FileOutputStream(tempFile)
                val buffer = ByteArray(CHUNK_SIZE)
                
                var bytesWritten = 0L
                while (bytesWritten < sizeBytes) {
                    val remainingBytes = (sizeBytes - bytesWritten).toInt()
                    val currentChunkSize = minOf(CHUNK_SIZE, remainingBytes)
                    
                    random.nextBytes(buffer)
                    fileOutputStream.write(buffer, 0, currentChunkSize)
                    bytesWritten += currentChunkSize
                    
                    // Update progress
                    val progress = (bytesWritten.toFloat() / sizeBytes).coerceAtMost(1.0f)
                    onProgress(progress, 0.0) // Speed not known until upload completes
                }
                fileOutputStream.close()
                
                // Upload the file
                withTimeoutOrNull(UPLOAD_TIMEOUT_MS) {
                    val response = client.post("$serverUrl/upload?expected_size=$sizeBytes") {
                        setBody(tempFile.readBytes())
                        contentType(ContentType.Application.OctetStream)
                    }
                    
                    // Parse response for actual upload speed
                    val responseText = response.bodyAsText()
                    // Extract speed from response if available
                }
                
                val endTime = System.currentTimeMillis()
                val durationMs = (endTime - startTime).coerceAtLeast(1)
                
                val mbps = (sizeBytes * 8.0) / (durationMs / 1000.0) / 1_000_000.0
                val finalSpeed = (mbps * 100).roundToInt() / 100.0
                
                // Final progress update
                onProgress(1.0f, finalSpeed)
                
                finalSpeed
            } finally {
                tempFile.delete()
            }
        } finally {
            client.close()
        }
    }
    
    /**
     * Test server connectivity
     */
    suspend fun testServerConnectivity(server: SpeedTestServer): Boolean {
        return try {
            val client = HttpClient(CIO) {
                engine {
                    requestTimeout = 5000L
                }
            }
            
            try {
                val response = client.get("${server.url}/ping")
                response.status.isSuccess()
            } finally {
                client.close()
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get server with best ping for testing
     */
    suspend fun getBestServer(): SpeedTestServer? {
        val servers = getSpeedTestServers()
        val serverPings = mutableListOf<Pair<SpeedTestServer, Long>>()
        
        for (server in servers) {
            try {
                val ping = measurePing(server.url)
                serverPings.add(server to ping)
            } catch (e: Exception) {
                // Skip servers that fail
                continue
            }
        }
        
        return serverPings.minByOrNull { it.second }?.first
    }
    
    /**
     * Get optimized test servers (alias for getSpeedTestServers for compatibility)
     */
    fun getOptimizedTestServers(): List<SpeedTestServer> {
        return getSpeedTestServers()
    }
    
    /**
     * Run real-time speed test (alias for runSpeedTest for compatibility)
     */
    fun runRealTimeSpeedTest(server: SpeedTestServer): Flow<RealTimeSpeedTestResult> {
        return runSpeedTest(server)
    }
}
