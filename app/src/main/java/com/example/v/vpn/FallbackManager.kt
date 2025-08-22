package com.example.v.vpn

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Fallback Manager - Handles VPN connection failures automatically
 * Provides intelligent fallback between different connection methods
 */
class FallbackManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "FallbackManager"
        
        @Volatile
        private var INSTANCE: FallbackManager? = null
        
        fun getInstance(context: Context): FallbackManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FallbackManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Connection methods with priority
    private val connectionMethods = listOf(
        ConnectionMethod.DESKTOP_API,
        ConnectionMethod.PROXY_SERVICE,
        ConnectionMethod.DIRECT_CONNECTION,
        ConnectionMethod.ALTERNATIVE_SERVER
    )
    
    // Current method being used
    private var currentMethod: ConnectionMethod? = null
    
    // Failure tracking
    private val methodFailures = ConcurrentHashMap<ConnectionMethod, Int>()
    private val methodLatencies = ConcurrentHashMap<ConnectionMethod, Long>()
    
    // Coroutine scope
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Callbacks
    private var onMethodChange: ((ConnectionMethod) -> Unit)? = null
    private var onFallbackTriggered: ((ConnectionMethod, String) -> Unit)? = null
    
    /**
     * Connection methods enum
     */
    enum class ConnectionMethod(val displayName: String, val description: String) {
        DESKTOP_API("Desktop API", "Connect through central VPN API"),
        PROXY_SERVICE("Proxy Service", "Connect through local proxy"),
        DIRECT_CONNECTION("Direct Connection", "Connect directly to server"),
        ALTERNATIVE_SERVER("Alternative Server", "Try different server location")
    }
    
    /**
     * Connection attempt result
     */
    data class ConnectionResult(
        val success: Boolean,
        val method: ConnectionMethod,
        val latency: Long,
        val error: String? = null
    )
    
    /**
     * Initialize the fallback manager
     */
    fun initialize() {
        Log.i(TAG, "🚀 Initializing Fallback Manager...")
        
        // Reset failure counts
        connectionMethods.forEach { method ->
            methodFailures[method] = 0
            methodLatencies[method] = 0L
        }
        
        Log.i(TAG, "✅ Fallback Manager initialized with ${connectionMethods.size} methods")
    }
    
    /**
     * Try to connect using fallback strategy
     */
    suspend fun connectWithFallback(
        location: String,
        onMethodChange: (ConnectionMethod) -> Unit,
        onFallbackTriggered: (ConnectionMethod, String) -> Unit
    ): ConnectionResult? {
        this.onMethodChange = onMethodChange
        this.onFallbackTriggered = onFallbackTriggered
        
        Log.i(TAG, "🔄 Starting fallback connection to $location...")
        
        // Try each method in priority order
        for (method in connectionMethods) {
            try {
                Log.i(TAG, "🔄 Trying method: ${method.displayName}")
                
                val startTime = System.currentTimeMillis()
                val result = tryConnectionMethod(method, location)
                val latency = System.currentTimeMillis() - startTime
                
                if (result) {
                    // Success! Update current method
                    currentMethod = method
                    methodLatencies[method] = latency
                    methodFailures[method] = 0
                    
                    Log.i(TAG, "✅ Method ${method.displayName} succeeded in ${latency}ms")
                    
                    onMethodChange(method)
                    
                    return ConnectionResult(
                        success = true,
                        method = method,
                        latency = latency
                    )
                } else {
                    // Method failed
                    val failureCount = methodFailures[method] ?: 0
                    methodFailures[method] = failureCount + 1
                    
                    Log.w(TAG, "❌ Method ${method.displayName} failed (failures: ${failureCount + 1})")
                    
                    // Trigger fallback callback
                    onFallbackTriggered(method, "Connection failed")
                    
                    // Wait before trying next method
                    delay(1000)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error with method ${method.displayName}: ${e.message}")
                
                val failureCount = methodFailures[method] ?: 0
                methodFailures[method] = failureCount + 1
                
                onFallbackTriggered(method, e.message ?: "Unknown error")
                delay(1000)
            }
        }
        
        Log.e(TAG, "❌ All connection methods failed")
        return null
    }
    
    /**
     * Try a specific connection method
     */
    private suspend fun tryConnectionMethod(method: ConnectionMethod, location: String): Boolean {
        return when (method) {
            ConnectionMethod.DESKTOP_API -> tryDesktopAPI(location)
            ConnectionMethod.PROXY_SERVICE -> tryProxyService(location)
            ConnectionMethod.DIRECT_CONNECTION -> tryDirectConnection(location)
            ConnectionMethod.ALTERNATIVE_SERVER -> tryAlternativeServer(location)
        }
    }
    
    /**
     * Try desktop API connection
     */
    private suspend fun tryDesktopAPI(location: String): Boolean {
        return try {
            Log.d(TAG, "📡 Testing desktop API for $location...")
            
            // This would call your actual desktop API
            // For now, simulate success/failure
            val success = simulateConnection(location, "desktop_api")
            
            if (success) {
                Log.d(TAG, "✅ Desktop API test successful")
                true
            } else {
                Log.d(TAG, "❌ Desktop API test failed")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Desktop API error: ${e.message}")
            false
        }
    }
    
    /**
     * Try proxy service connection
     */
    private suspend fun tryProxyService(location: String): Boolean {
        return try {
            Log.d(TAG, "🌐 Testing proxy service for $location...")
            
            // This would test your proxy service
            val success = simulateConnection(location, "proxy_service")
            
            if (success) {
                Log.d(TAG, "✅ Proxy service test successful")
                true
            } else {
                Log.d(TAG, "❌ Proxy service test failed")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Proxy service error: ${e.message}")
            false
        }
    }
    
    /**
     * Try direct connection
     */
    private suspend fun tryDirectConnection(location: String): Boolean {
        return try {
            Log.d(TAG, "🔌 Testing direct connection to $location...")
            
            // This would test direct server connectivity
            val success = simulateConnection(location, "direct_connection")
            
            if (success) {
                Log.d(TAG, "✅ Direct connection test successful")
                true
            } else {
                Log.d(TAG, "❌ Direct connection test failed")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Direct connection error: ${e.message}")
            false
        }
    }
    
    /**
     * Try alternative server
     */
    private suspend fun tryAlternativeServer(location: String): Boolean {
        return try {
            Log.d(TAG, "🔄 Testing alternative server for $location...")
            
            // Get alternative location
            val alternativeLocation = when (location) {
                "paris" -> "osaka"
                "osaka" -> "paris"
                else -> return false
            }
            
            Log.d(TAG, "🔄 Trying alternative location: $alternativeLocation")
            
            // Test the alternative location
            val success = simulateConnection(alternativeLocation, "alternative_server")
            
            if (success) {
                Log.d(TAG, "✅ Alternative server test successful")
                true
            } else {
                Log.d(TAG, "❌ Alternative server test failed")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Alternative server error: ${e.message}")
            false
        }
    }
    
    /**
     * Simulate connection for testing (replace with real implementation)
     */
    private suspend fun simulateConnection(location: String, method: String): Boolean {
        delay(500) // Simulate network delay
        
        // Simulate different success rates for different methods
        val successRate = when (method) {
            "desktop_api" -> 0.8f      // 80% success rate
            "proxy_service" -> 0.9f     // 90% success rate
            "direct_connection" -> 0.3f // 30% success rate (often blocked)
            "alternative_server" -> 0.7f // 70% success rate
            else -> 0.5f
        }
        
        val random = kotlin.random.Random.nextFloat()
        val success = random < successRate
        
        Log.d(TAG, "🎲 Simulated connection: $method to $location - ${if (success) "SUCCESS" else "FAILED"}")
        
        return success
    }
    
    /**
     * Get current connection method
     */
    fun getCurrentMethod(): ConnectionMethod? = currentMethod
    
    /**
     * Get method statistics
     */
    fun getMethodStats(): Map<ConnectionMethod, MethodStats> {
        return connectionMethods.associateWith { method ->
            MethodStats(
                method = method,
                failureCount = methodFailures[method] ?: 0,
                averageLatency = methodLatencies[method] ?: 0L,
                isCurrentMethod = method == currentMethod
            )
        }
    }
    
    /**
     * Get recommended method
     */
    fun getRecommendedMethod(): ConnectionMethod {
        // Return method with lowest failure count and latency
        return connectionMethods.minByOrNull { method ->
            val failures = methodFailures[method] ?: 0
            val latency = methodLatencies[method] ?: Long.MAX_VALUE
            
            // Weight failures more heavily than latency
            failures * 1000 + latency
        } ?: ConnectionMethod.DESKTOP_API
    }
    
    /**
     * Reset failure counts for a method
     */
    fun resetMethodFailures(method: ConnectionMethod) {
        methodFailures[method] = 0
        Log.d(TAG, "🔄 Reset failure count for ${method.displayName}")
    }
    
    /**
     * Get fallback status
     */
    fun getFallbackStatus(): String {
        val current = currentMethod?.displayName ?: "None"
        val recommended = getRecommendedMethod().displayName
        val totalFailures = methodFailures.values.sum()
        
        return "Current: $current | Recommended: $recommended | Total Failures: $totalFailures"
    }
    
    /**
     * Method statistics
     */
    data class MethodStats(
        val method: ConnectionMethod,
        val failureCount: Int,
        val averageLatency: Long,
        val isCurrentMethod: Boolean
    )
}
