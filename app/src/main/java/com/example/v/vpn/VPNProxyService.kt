package com.example.v.vpn

import android.content.Context
import android.util.Log
import com.example.v.data.models.VPNConnectionResponse
import kotlinx.coroutines.*
import java.net.*
import java.io.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * VPN Proxy Service - Handles VPN connections through working servers
 * This solves the "Network unreachable" problem by routing traffic properly
 */
class VPNProxyService private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "VPNProxy"
        
        @Volatile
        private var INSTANCE: VPNProxyService? = null
        
        fun getInstance(context: Context): VPNProxyService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VPNProxyService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Proxy configuration
    private var proxyPort = 1080
    private var isRunning = false
    private var proxyServer: ServerSocket? = null
    
    // VPN server management
    private val workingServers = ConcurrentHashMap<String, ServerInfo>()
    private val serverHealth = ConcurrentHashMap<String, ServerHealth>()
    
    // Connection management
    private var currentConnection: VPNConnectionResponse? = null
    private var activeConnections = ConcurrentHashMap<String, Socket>()
    
    // Coroutine scope
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Server information
     */
    data class ServerInfo(
        val id: String,
        val name: String,
        val ip: String,
        val port: Int,
        val publicKey: String,
        val subnet: String,
        val isWorking: Boolean = true
    )
    
    /**
     * Server health status
     */
    data class ServerHealth(
        val lastCheck: Long = System.currentTimeMillis(),
        val isReachable: Boolean = true,
        val latency: Long = 0L,
        val failureCount: Int = 0
    )
    
    /**
     * Initialize the proxy service with available servers
     */
    fun initialize() {
        Log.i(TAG, "🚀 Initializing VPN Proxy Service...")
        
        // Add known servers
        addServer(ServerInfo(
            id = "paris",
            name = "Paris",
            ip = "52.47.190.220",
            port = 51820,
            publicKey = "yvB7acu9ncFFEyzw5n8L7kpLazTgQonML1PuhoStjjg=",
            subnet = "10.77.26.0/24"
        ))
        
        addServer(ServerInfo(
            id = "osaka",
            name = "Osaka", 
            ip = "15.168.240.118",
            port = 51820,
            publicKey = "Hr1B3sNsDSxFpR+zO34qLGxutUK3wgaPwrsWoY2ViAM=",
            subnet = "10.77.27.0/24"
        ))
        
        // Start health monitoring
        startHealthMonitoring()
        
        Log.i(TAG, "✅ VPN Proxy Service initialized with ${workingServers.size} servers")
    }
    
    /**
     * Add a server to the proxy
     */
    private fun addServer(server: ServerInfo) {
        workingServers[server.id] = server
        serverHealth[server.id] = ServerHealth()
        Log.d(TAG, "➕ Added server: ${server.name} (${server.ip}:${server.port})")
    }
    
    /**
     * Start health monitoring for all servers
     */
    private fun startHealthMonitoring() {
        scope.launch {
            while (isRunning) {
                checkAllServers()
                delay(30000) // Check every 30 seconds
            }
        }
        Log.d(TAG, "🔍 Started server health monitoring")
    }
    
    /**
     * Check health of all servers
     */
    private suspend fun checkAllServers() {
        workingServers.forEach { (id, server) ->
            val health = checkServerHealth(server)
            serverHealth[id] = health
            
            if (health.isReachable) {
                Log.v(TAG, "✅ ${server.name} is healthy (latency: ${health.latency}ms)")
            } else {
                Log.w(TAG, "❌ ${server.name} is unreachable (failures: ${health.failureCount})")
            }
        }
    }
    
    /**
     * Check individual server health
     */
    private suspend fun checkServerHealth(server: ServerInfo): ServerHealth {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                
                socket.connect(InetSocketAddress(server.ip, server.port), 5000)
                val latency = System.currentTimeMillis() - startTime
                socket.close()
                
                ServerHealth(
                    lastCheck = System.currentTimeMillis(),
                    isReachable = true,
                    latency = latency,
                    failureCount = 0
                )
            } catch (e: Exception) {
                val currentHealth = serverHealth[server.id] ?: ServerHealth()
                ServerHealth(
                    lastCheck = System.currentTimeMillis(),
                    isReachable = false,
                    latency = 0L,
                    failureCount = currentHealth.failureCount + 1
                )
            }
        }
    }
    
    /**
     * Get the best available server
     */
    fun getBestServer(): ServerInfo? {
        return workingServers.values
            .filter { serverHealth[it.id]?.isReachable == true }
            .minByOrNull { serverHealth[it.id]?.latency ?: Long.MAX_VALUE }
    }
    
    /**
     * Start the proxy server
     */
    fun startProxy(): Boolean {
        return try {
            if (isRunning) {
                Log.w(TAG, "⚠️ Proxy already running")
                return true
            }
            
            Log.i(TAG, "🚀 Starting VPN Proxy Server on port $proxyPort...")
            
            proxyServer = ServerSocket(proxyPort)
            isRunning = true
            
            // Start accepting connections
            scope.launch {
                acceptConnections()
            }
            
            Log.i(TAG, "✅ VPN Proxy Server started successfully on port $proxyPort")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start proxy: ${e.message}")
            false
        }
    }
    
    /**
     * Accept incoming proxy connections
     */
    private suspend fun acceptConnections() {
        while (isRunning) {
            try {
                val clientSocket = proxyServer?.accept() ?: continue
                Log.d(TAG, "🔌 New proxy connection from ${clientSocket.inetAddress}")
                
                // Handle connection in separate coroutine
                scope.launch {
                    handleProxyConnection(clientSocket)
                }
                
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "❌ Error accepting connection: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Handle individual proxy connection
     */
    private suspend fun handleProxyConnection(clientSocket: Socket) {
        try {
            // Get best available server
            val server = getBestServer()
            if (server == null) {
                Log.e(TAG, "❌ No working servers available")
                clientSocket.close()
                return
            }
            
            Log.d(TAG, "🌐 Routing connection through ${server.name} (${server.ip}:${server.port})")
            
            // Create connection to VPN server
            val serverSocket = Socket()
            serverSocket.connect(InetSocketAddress(server.ip, server.port), 10000)
            
            // Start bidirectional data forwarding
            val clientToServer = scope.launch { forwardData(clientSocket.inputStream, serverSocket.outputStream, "Client→Server") }
            val serverToClient = scope.launch { forwardData(serverSocket.inputStream, clientSocket.outputStream, "Server→Client") }
            
            // Wait for either direction to complete
            withTimeoutOrNull(300000) { // 5 minutes timeout
                clientToServer.join()
                serverToClient.join()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling proxy connection: ${e.message}")
        } finally {
            try {
                clientSocket.close()
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error closing client socket: ${e.message}")
            }
        }
    }
    
    /**
     * Forward data between streams
     */
    private suspend fun forwardData(input: InputStream, output: OutputStream, direction: String) {
        val buffer = ByteArray(8192)
        try {
            while (isRunning) {
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) break
                
                output.write(buffer, 0, bytesRead)
                output.flush()
                
                Log.v(TAG, "📡 $direction: $bytesRead bytes")
            }
        } catch (e: Exception) {
            Log.d(TAG, "🔌 $direction connection closed: ${e.message}")
        }
    }
    
    /**
     * Stop the proxy server
     */
    fun stopProxy() {
        Log.i(TAG, "🛑 Stopping VPN Proxy Server...")
        
        isRunning = false
        
        try {
            proxyServer?.close()
            proxyServer = null
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error closing proxy server: ${e.message}")
        }
        
        // Close all active connections
        activeConnections.values.forEach { socket ->
            try {
                socket.close()
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error closing connection: ${e.message}")
            }
        }
        activeConnections.clear()
        
        Log.i(TAG, "✅ VPN Proxy Server stopped")
    }
    
    /**
     * Get proxy status
     */
    fun getStatus(): String {
        val workingCount = workingServers.values.count { serverHealth[it.id]?.isReachable == true }
        val totalCount = workingServers.size
        
        return "Proxy: ${if (isRunning) "Running" else "Stopped"} | " +
               "Servers: $workingCount/$totalCount working | " +
               "Port: $proxyPort"
    }
    
    /**
     * Get working server list
     */
    fun getWorkingServers(): List<ServerInfo> {
        return workingServers.values.filter { serverHealth[it.id]?.isReachable == true }
    }
    
    /**
     * Test server connectivity
     */
    suspend fun testServer(serverId: String): Boolean {
        val server = workingServers[serverId] ?: return false
        val health = checkServerHealth(server)
        serverHealth[serverId] = health
        return health.isReachable
    }
}
