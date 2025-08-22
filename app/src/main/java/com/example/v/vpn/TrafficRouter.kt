package com.example.v.vpn

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.net.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Traffic Router - Intelligently routes VPN traffic through best available paths
 * Handles routing failures and provides automatic path switching
 */
class TrafficRouter private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "TrafficRouter"
        
        @Volatile
        private var INSTANCE: TrafficRouter? = null
        
        fun getInstance(context: Context): TrafficRouter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TrafficRouter(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Routing paths
    private val routingPaths = ConcurrentHashMap<String, RoutingPath>()
    private var currentPath: RoutingPath? = null
    
    // Traffic monitoring
    private val trafficStats = ConcurrentHashMap<String, TrafficStats>()
    private var isMonitoring = false
    
    // Coroutine scope
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Callbacks
    private var onPathChange: ((RoutingPath) -> Unit)? = null
    private var onRoutingFailure: ((String, String) -> Unit)? = null
    
    /**
     * Routing path information
     */
    data class RoutingPath(
        val id: String,
        val name: String,
        val serverIP: String,
        val serverPort: Int,
        val protocol: String,
        val priority: Int,
        val isActive: Boolean = true,
        val lastTested: Long = System.currentTimeMillis(),
        val latency: Long = 0L,
        val bandwidth: Long = 0L
    )
    
    /**
     * Traffic statistics
     */
    data class TrafficStats(
        val pathId: String,
        val bytesSent: Long = 0L,
        val bytesReceived: Long = 0L,
        val packetsSent: Long = 0L,
        val packetsReceived: Long = 0L,
        val errors: Long = 0L,
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    /**
     * Initialize the traffic router
     */
    fun initialize() {
        Log.i(TAG, "🚀 Initializing Traffic Router...")
        
        // Initialize default routing paths
        initializeDefaultPaths()
        
        // Start traffic monitoring
        startTrafficMonitoring()
        
        Log.i(TAG, "✅ Traffic Router initialized with ${routingPaths.size} paths")
    }
    
    /**
     * Initialize default routing paths
     */
    private fun initializeDefaultPaths() {
        // Primary path - Desktop API
        addRoutingPath(RoutingPath(
            id = "desktop_api",
            name = "Desktop API",
            serverIP = "vpn.richdalelab.com",
            serverPort = 443,
            protocol = "HTTPS",
            priority = 1
        ))
        
        // Secondary path - Proxy Service
        addRoutingPath(RoutingPath(
            id = "proxy_service",
            name = "Proxy Service",
            serverIP = "127.0.0.1",
            serverPort = 1080,
            protocol = "SOCKS5",
            priority = 2
        ))
        
        // Direct paths
        addRoutingPath(RoutingPath(
            id = "paris_direct",
            name = "Paris Direct",
            serverIP = "52.47.190.220",
            serverPort = 51820,
            protocol = "WireGuard",
            priority = 3
        ))
        
        addRoutingPath(RoutingPath(
            id = "osaka_direct",
            name = "Osaka Direct",
            serverIP = "15.168.240.118",
            serverPort = 51820,
            protocol = "WireGuard",
            priority = 4
        ))
    }
    
    /**
     * Add a routing path
     */
    private fun addRoutingPath(path: RoutingPath) {
        routingPaths[path.id] = path
        trafficStats[path.id] = TrafficStats(pathId = path.id)
        Log.d(TAG, "➕ Added routing path: ${path.name} (${path.serverIP}:${path.serverPort})")
    }
    
    /**
     * Start traffic monitoring
     */
    private fun startTrafficMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        scope.launch {
            while (isMonitoring) {
                try {
                    monitorAllPaths()
                    delay(10000) // Monitor every 10 seconds
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Traffic monitoring error: ${e.message}")
                    delay(5000) // Wait before retrying
                }
            }
        }
        Log.d(TAG, "🔍 Started traffic monitoring")
    }
    
    /**
     * Monitor all routing paths
     */
    private suspend fun monitorAllPaths() {
        routingPaths.values.forEach { path ->
            try {
                val health = checkPathHealth(path)
                updatePathHealth(path, health)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error monitoring path ${path.name}: ${e.message}")
            }
        }
        
        // Select best available path
        selectBestPath()
    }
    
    /**
     * Check path health
     */
    private suspend fun checkPathHealth(path: RoutingPath): PathHealth {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                when (path.protocol) {
                    "HTTPS" -> checkHTTPSHealth(path)
                    "SOCKS5" -> checkSOCKS5Health(path)
                    "WireGuard" -> checkWireGuardHealth(path)
                    else -> PathHealth(false, 0L, "Unknown protocol")
                }
                
            } catch (e: Exception) {
                PathHealth(false, 0L, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Check HTTPS path health
     */
    private suspend fun checkHTTPSHealth(path: RoutingPath): PathHealth {
        return try {
            val url = URL("https://${path.serverIP}")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            
            val startTime = System.currentTimeMillis()
            val responseCode = connection.responseCode
            val latency = System.currentTimeMillis() - startTime
            
            if (responseCode in 200..299) {
                PathHealth(true, latency, "HTTP $responseCode")
            } else {
                PathHealth(false, latency, "HTTP $responseCode")
            }
            
        } catch (e: Exception) {
            PathHealth(false, 0L, e.message ?: "Connection failed")
        }
    }
    
    /**
     * Check SOCKS5 path health
     */
    private suspend fun checkSOCKS5Health(path: RoutingPath): PathHealth {
        return try {
            val socket = Socket()
            val startTime = System.currentTimeMillis()
            
            socket.connect(InetSocketAddress(path.serverIP, path.serverPort), 5000)
            val latency = System.currentTimeMillis() - startTime
            
            socket.close()
            PathHealth(true, latency, "Connected")
            
        } catch (e: Exception) {
            PathHealth(false, 0L, e.message ?: "Connection failed")
        }
    }
    
    /**
     * Check WireGuard path health
     */
    private suspend fun checkWireGuardHealth(path: RoutingPath): PathHealth {
        return try {
            val socket = Socket()
            val startTime = System.currentTimeMillis()
            
            socket.connect(InetSocketAddress(path.serverIP, path.serverPort), 5000)
            val latency = System.currentTimeMillis() - startTime
            
            socket.close()
            PathHealth(true, latency, "Connected")
            
        } catch (e: Exception) {
            PathHealth(false, 0L, e.message ?: "Connection failed")
        }
    }
    
    /**
     * Update path health
     */
    private fun updatePathHealth(path: RoutingPath, health: PathHealth) {
        val updatedPath = path.copy(
            isActive = health.isHealthy,
            lastTested = System.currentTimeMillis(),
            latency = health.latency
        )
        
        routingPaths[path.id] = updatedPath
        
        if (health.isHealthy) {
            Log.v(TAG, "✅ Path ${path.name} is healthy (latency: ${health.latency}ms)")
        } else {
            Log.w(TAG, "❌ Path ${path.name} is unhealthy: ${health.reason}")
        }
    }
    
    /**
     * Select the best available path
     */
    private fun selectBestPath() {
        val availablePaths = routingPaths.values.filter { it.isActive }
        
        if (availablePaths.isEmpty()) {
            Log.w(TAG, "⚠️ No healthy routing paths available")
            return
        }
        
        // Select path with highest priority and lowest latency
        val bestPath = availablePaths.minByOrNull { path ->
            val priorityScore = path.priority * 1000
            val latencyScore = path.latency
            priorityScore + latencyScore
        }
        
        if (bestPath != null && bestPath != currentPath) {
            Log.i(TAG, "🔄 Switching to routing path: ${bestPath.name}")
            currentPath = bestPath
            onPathChange?.invoke(bestPath)
        }
    }
    
    /**
     * Route traffic through current path
     */
    suspend fun routeTraffic(data: ByteArray): RoutingResult {
        val path = currentPath
        if (path == null) {
            return RoutingResult(false, "No active routing path", null)
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            // Route data through the selected path
            val result = when (path.protocol) {
                "HTTPS" -> routeThroughHTTPS(path, data)
                "SOCKS5" -> routeThroughSOCKS5(path, data)
                "WireGuard" -> routeThroughWireGuard(path, data)
                else -> RoutingResult(false, "Unsupported protocol: ${path.protocol}", null)
            }
            
            // Update traffic statistics
            updateTrafficStats(path.id, data.size, result.success)
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Traffic routing error: ${e.message}")
            RoutingResult(false, e.message ?: "Unknown error", null)
        }
    }
    
    /**
     * Route through HTTPS
     */
    private suspend fun routeThroughHTTPS(path: RoutingPath, data: ByteArray): RoutingResult {
        // Simulate HTTPS routing
        delay(10) // Simulate processing time
        return RoutingResult(true, "HTTPS routing successful", data)
    }
    
    /**
     * Route through SOCKS5
     */
    private suspend fun routeThroughSOCKS5(path: RoutingPath, data: ByteArray): RoutingResult {
        // Simulate SOCKS5 routing
        delay(15) // Simulate processing time
        return RoutingResult(true, "SOCKS5 routing successful", data)
    }
    
    /**
     * Route through WireGuard
     */
    private suspend fun routeThroughWireGuard(path: RoutingPath, data: ByteArray): RoutingResult {
        // Simulate WireGuard routing
        delay(5) // Simulate processing time
        return RoutingResult(true, "WireGuard routing successful", data)
    }
    
    /**
     * Update traffic statistics
     */
    private fun updateTrafficStats(pathId: String, bytes: Int, success: Boolean) {
        val stats = trafficStats[pathId] ?: return
        
        val updatedStats = if (success) {
            stats.copy(
                bytesSent = stats.bytesSent + bytes,
                packetsSent = stats.packetsSent + 1,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            stats.copy(
                errors = stats.errors + 1,
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        trafficStats[pathId] = updatedStats
    }
    
    /**
     * Get current routing path
     */
    fun getCurrentPath(): RoutingPath? = currentPath
    
    /**
     * Get all routing paths
     */
    fun getAllPaths(): List<RoutingPath> = routingPaths.values.toList()
    
    /**
     * Get traffic statistics
     */
    fun getTrafficStats(): Map<String, TrafficStats> = trafficStats.toMap()
    
    /**
     * Get routing status
     */
    fun getRoutingStatus(): String {
        val current = currentPath?.name ?: "None"
        val totalPaths = routingPaths.size
        val activePaths = routingPaths.values.count { it.isActive }
        
        return "Current: $current | Active: $activePaths/$totalPaths paths"
    }
    
    /**
     * Force path switch
     */
    fun forcePathSwitch(pathId: String): Boolean {
        val path = routingPaths[pathId]
        if (path != null && path.isActive) {
            currentPath = path
            onPathChange?.invoke(path)
            Log.i(TAG, "🔄 Forced switch to path: ${path.name}")
            return true
        }
        return false
    }
    
    /**
     * Set callbacks
     */
    fun setCallbacks(
        onPathChange: (RoutingPath) -> Unit,
        onRoutingFailure: (String, String) -> Unit
    ) {
        this.onPathChange = onPathChange
        this.onRoutingFailure = onRoutingFailure
    }
    
    /**
     * Cleanup
     */
    fun cleanup() {
        isMonitoring = false
        scope.cancel()
        Log.i(TAG, "🧹 Traffic Router cleaned up")
    }
    
    /**
     * Path health information
     */
    data class PathHealth(
        val isHealthy: Boolean,
        val latency: Long,
        val reason: String
    )
    
    /**
     * Routing result
     */
    data class RoutingResult(
        val success: Boolean,
        val message: String,
        val data: ByteArray?
    )
}
