package com.example.v.utils

import android.util.Log
import com.example.v.models.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream
import java.net.Socket
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * FastAPI Speed Test Service for VPN speed testing
 * Integrates with Osaka (15.168.240.118:8000) and Paris (52.47.190.220:8000) servers
 */
object FastAPISpeedTestService {
    
    // Production VPN server configuration
    private const val OSAKA_SERVER = "https://vpn.richdalelab.com/osaka"
    private const val PARIS_SERVER = "https://vpn.richdalelab.com/paris"
    
    // Speed test endpoints for production
    // private const val OSAKA_SERVER = "https://osaka.vpn.richdalelab.com"
    // private const val PARIS_SERVER = "https://paris.vpn.richdalelab.com"
    
    private const val TIMEOUT_SECONDS = 30L
    private const val DOWNLOAD_CHUNK_SIZE = 1024 * 1024 // 1MB chunks
    
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
     * Get available speed test servers - SELF-CONTAINED VERSION
     * Uses public speed test endpoints for Play Store compatibility
     */
    fun getSpeedTestServers(): List<SpeedTestServer> = listOf(
        SpeedTestServer("Cloudflare", "https://speed.cloudflare.com", "Global CDN", "1.1.1.1"),
        SpeedTestServer("Fast.com", "https://fast.com", "Netflix CDN", "fast.com"),
        SpeedTestServer("Osaka VPN", OSAKA_SERVER, "Osaka, Japan", "15.168.240.118"),
        SpeedTestServer("Paris VPN", PARIS_SERVER, "Paris, France", "52.47.190.220")
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
            
            currentDownload = measureDownloadSpeed(server.url, DOWNLOAD_CHUNK_SIZE) { progress, currentSpeed ->
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
            
            currentUpload = measureUploadSpeed(server.url, DOWNLOAD_CHUNK_SIZE) { progress, currentSpeed ->
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
                requestTimeout = TIMEOUT_SECONDS * 1000L
            }
        }
        
        try {
            val pingTime = measureTimeMillis {
                withTimeoutOrNull(TIMEOUT_SECONDS * 1000L) {
                    client.get("$serverUrl/ping")
                }
            }
            
            if (pingTime >= TIMEOUT_SECONDS * 1000L) {
                TIMEOUT_SECONDS * 1000L
            } else {
                pingTime
            }
        } finally {
            client.close()
        }
    }
    
    private suspend fun measureDownloadSpeed(
        serverUrl: String,
        chunkSize: Int,
        onProgress: suspend (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = TIMEOUT_SECONDS * 1000L
            }
        }
        
        try {
            val sizeBytes = chunkSize
            val startTime = System.currentTimeMillis()
            val bytesReceived = AtomicLong(0)
            var lastUpdateTime = startTime
            
            withTimeoutOrNull(TIMEOUT_SECONDS * 1000L) {
                val response = client.get("$serverUrl/download?size=$sizeBytes")
                val channel = response.bodyAsChannel()
                
                try {
                    val buffer = ByteArray(chunkSize)
                    
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead == -1) break
                        
                        bytesReceived.addAndGet(bytesRead.toLong())
                        
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= 100) { // Update every 100ms
                            val progress = (bytesReceived.get().toFloat() / sizeBytes) * 100
                            val currentSpeed = calculateSpeed(bytesReceived.get(), startTime, currentTime)
                            onProgress(progress, currentSpeed)
                            lastUpdateTime = currentTime
                        }
                    }
                } finally {
                    channel.cancel()
                }
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            if (totalTime > 0) {
                (bytesReceived.get() * 8.0) / (totalTime * 1000.0) // Convert to Mbps
            } else {
                0.0
            }
        } finally {
            client.close()
        }
    }
    
    private suspend fun measureUploadSpeed(
        serverUrl: String,
        chunkSize: Int,
        onProgress: suspend (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = TIMEOUT_SECONDS * 1000L
            }
        }
        
        try {
            val sizeBytes = chunkSize
            val startTime = System.currentTimeMillis()
            
            // Create a temporary file with random data
            val tempFile = File.createTempFile("speedtest", ".tmp")
            tempFile.deleteOnExit()
            
            try {
                val random = Random()
                val fileOutputStream = FileOutputStream(tempFile)
                val buffer = ByteArray(chunkSize)
                
                var bytesWritten = 0L
                while (bytesWritten < sizeBytes) {
                    val remainingBytes = (sizeBytes - bytesWritten).toInt()
                    val currentChunkSize = minOf(chunkSize, remainingBytes)
                    
                    random.nextBytes(buffer)
                    fileOutputStream.write(buffer, 0, currentChunkSize)
                    bytesWritten += currentChunkSize
                }
                fileOutputStream.close()
                
                // Upload the file
                withTimeoutOrNull(TIMEOUT_SECONDS * 1000L) {
                    val response = client.post("$serverUrl/upload?expected_size=$sizeBytes") {
                        setBody(tempFile.readBytes())
                    }
                    
                    if (response.status.value in 200..299) {
                        val totalTime = System.currentTimeMillis() - startTime
                        if (totalTime > 0) {
                            (sizeBytes * 8.0) / (totalTime * 1000.0) // Convert to Mbps
                        } else {
                            0.0
                        }
                    } else {
                        0.0
                    }
                } ?: 0.0
            } finally {
                tempFile.delete()
            }
        } finally {
            client.close()
        }
    }
    
    /**
     * Calculate speed in Mbps
     */
    private fun calculateSpeed(bytes: Long, startTime: Long, currentTime: Long): Double {
        val durationMs = currentTime - startTime
        return if (durationMs > 0) {
            (bytes * 8.0) / (durationMs * 1000.0) // Convert to Mbps
        } else {
            0.0
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

    private suspend fun testMultipleEndpoints(): Boolean {
        val endpoints = listOf(
            "8.8.8.8" to 53,        // Google DNS (Global)
            "1.1.1.1" to 53,        // Cloudflare DNS (Global)
            "208.67.222.222" to 53, // OpenDNS (Global)
            "9.9.9.9" to 53,        // Quad9 DNS (Global)
            "8.8.4.4" to 53         // Google DNS Secondary (Global)
        )
        
        for ((ip, port) in endpoints) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 8000) // Increased timeout for global compatibility
                socket.close()
                return true // If any endpoint works, we have connectivity
            } catch (e: Exception) {
                Log.d("FastAPISpeedTestService", "Endpoint $ip:$port failed: ${e.message}")
            }
        }
        return false
    }
    
    private suspend fun tryRestoreConnectivity(): Boolean {
        Log.d("FastAPISpeedTestService", "🔄 Attempting to restore connectivity...")
        
        // Wait a bit before testing again
        delay(2000)
        
        // Test connectivity again
        return testMultipleEndpoints()
    }
}
