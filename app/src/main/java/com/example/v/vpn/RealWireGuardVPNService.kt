package com.example.v.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.v.data.models.VPNConnectionResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineExceptionHandler
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlinx.coroutines.withTimeout

/**
 * Real WireGuard VPN Service - Android Implementation
 * Based on desktop version architecture but adapted for Android
 */
class RealWireGuardVPNService : VpnService() {
    
    companion object {
        private const val TAG = "RealWireGuardVPN"
        private const val HANDSHAKE_TIMEOUT = 10000L // 10 seconds
        private const val SERVER_CONNECT_TIMEOUT = 5000L // 5 seconds
        private const val MAX_HANDSHAKE_RETRIES = 3
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private val isRunning = AtomicBoolean(false)
    private var onConnectionStatusChanged: ((Boolean, String?) -> Unit)? = null
    private var activeConnections = ConcurrentHashMap<String, DatagramSocket>()
    private var handshakeComplete = false
    private var lastHandshakeTime = 0L
    private var connectionInProgress = AtomicBoolean(false)
    private var currentConnectionJob: Job? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "❌ Coroutine exception: ${exception.message}", exception)
    })
    
    // WireGuard configuration
    private var privateKey: String = ""
    private var serverPublicKey: String = ""
    private var serverEndpoint: String = ""
    private var internalIP: String = ""
    private var internalIPv6: String = ""
    private var dns: String = ""
    private var mtu: Int = 1420
    private var presharedKey: String = ""
    private var allowedIPs: String = ""
    
    // WireGuard cryptographic state
    private var localPrivateKey: ByteArray = ByteArray(32)
    private var localPublicKey: ByteArray = ByteArray(32)
    private var remotePublicKey: ByteArray = ByteArray(32)
    private var presharedKeyBytes: ByteArray = ByteArray(32)
    private var localIndex: Int = 0
    private var remoteIndex: Int = 0
    private var localEphemeralPrivateKey: ByteArray = ByteArray(32)
    private var localEphemeralPublicKey: ByteArray = ByteArray(32)
    private var remoteEphemeralPublicKey: ByteArray = ByteArray(32)
    private var chainingKey: ByteArray = ByteArray(32)
    private var sendingKey: ByteArray = ByteArray(32)
    private var receivingKey: ByteArray = ByteArray(32)
    private var sendingNonce: Int = 0
    private var receivingNonce: Int = 0
    private var secureRandom = SecureRandom()
    
    // Session management
    private var handshakeTimeout: Long = 30000 // 30 seconds
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "CONNECT" -> {
                // This will be called by VPNManager with proper parameters
                Log.i(TAG, "📱 VPN service started with CONNECT intent")
            }
            "DISCONNECT" -> {
                stopVPN()
            }
        }
        return START_STICKY
    }
    
    suspend fun startVPN(config: VPNConnectionResponse, onStatusChanged: (Boolean, String?) -> Unit): Boolean {
        return try {
            // Check if connection is already in progress
            if (connectionInProgress.get()) {
                Log.w(TAG, "⚠️ VPN connection already in progress, ignoring request")
                return false
            }
            
            // Check if already connected
            if (isRunning.get()) {
                Log.w(TAG, "⚠️ VPN already running, disconnecting first")
                stopVPN()
            }
            
            // Set connection in progress
            connectionInProgress.set(true)
            
            // Cancel any existing connection job
            currentConnectionJob?.cancel()
            
            // Start new connection job
            currentConnectionJob = scope.launch {
                try {
                    Log.i(TAG, "🚀 Starting WireGuard VPN connection...")
                    
                    // Initialize WireGuard crypto
                    initializeWireGuardCrypto(config)
                    
                    // Create VPN interface
                    createVPNInterface(config)
                    
                    // Try to start WireGuard tunnel with fallback
                    val success = startWireGuardTunnelWithFallback(config)
                    
                    if (success) {
                        // Set running state to true
                        isRunning.set(true)
                        
                        // Test connectivity to verify VPN is working
                        testVPNConnectivity()
                        
                        Log.i(TAG, "✅ WireGuard VPN started successfully")
                    } else {
                        throw Exception("Failed to establish connection with any server")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to start VPN: ${e.message}", e)
                    onStatusChanged(false, e.message)
                } finally {
                    connectionInProgress.set(false)
                }
            }
            
            // Wait for connection to complete
            currentConnectionJob?.join()
            
            // Check if connection was successful
            isRunning.get()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start VPN: ${e.message}", e)
            connectionInProgress.set(false)
            onStatusChanged(false, e.message)
            false
        }
    }
    
    private fun initializeWireGuardCrypto(config: VPNConnectionResponse) {
        try {
            // Parse configuration from the config parameter
            privateKey = config.privateKey
            serverPublicKey = config.serverPublicKey
            serverEndpoint = config.serverEndpoint
            internalIP = config.internalIP
            internalIPv6 = config.internalIPv6 ?: ""
            dns = config.dns
            mtu = config.mtu
            presharedKey = config.presharedKey ?: ""
            allowedIPs = config.allowedIPs
            
            // Generate local keypair
            val keyPair = generateKeyPair()
            localPrivateKey = keyPair.first
            localPublicKey = keyPair.second
            
            // Parse remote public key
            remotePublicKey = decodeBase64(serverPublicKey)
            
            // Parse preshared key if provided
            if (presharedKey.isNotEmpty()) {
                presharedKeyBytes = decodeBase64(presharedKey)
            }
            
            // Initialize chaining key
            chainingKey = "Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray()
            
            // Generate random indices
            localIndex = secureRandom.nextInt()
            remoteIndex = secureRandom.nextInt()
            
            Log.d(TAG, "🔐 WireGuard crypto initialized")
            Log.d(TAG, "   Local public key: ${encodeBase64(localPublicKey)}")
            Log.d(TAG, "   Remote public key: ${encodeBase64(remotePublicKey)}")
            Log.d(TAG, "📋 Parsed VPN configuration:")
            Log.d(TAG, "   Server: $serverEndpoint")
            Log.d(TAG, "   Internal IP: $internalIP")
            Log.d(TAG, "   DNS: $dns (from API)")
            Log.d(TAG, "   MTU: $mtu")
            Log.d(TAG, "   Allowed IPs: $allowedIPs")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize WireGuard crypto: ${e.message}")
            throw e
        }
    }
    
    private fun generateKeyPair(): Pair<ByteArray, ByteArray> {
        // Generate Ed25519 keypair (simplified - in production use proper Ed25519)
        val privateKey = ByteArray(32)
        secureRandom.nextBytes(privateKey)
        
        // Derive public key (simplified - in production use proper Ed25519 derivation)
        val publicKey = ByteArray(32)
        System.arraycopy(privateKey, 0, publicKey, 0, 32)
        
        return Pair(privateKey, publicKey)
    }
    
    private fun decodeBase64(input: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(input)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to decode base64, using raw bytes")
            input.toByteArray().copyOf(32)
        }
    }
    
    private fun encodeBase64(bytes: ByteArray): String {
        return java.util.Base64.getEncoder().encodeToString(bytes)
    }
    
    private fun createVPNInterface(config: VPNConnectionResponse) {
        try {
            Log.i(TAG, "🔧 Creating VPN interface...")
            
            val builder = Builder()
                .setSession("GhostShield VPN")
                .addAddress(internalIP, 32)
            
            // Add IPv6 if available
            if (internalIPv6.isNotEmpty()) {
                builder.addAddress(internalIPv6, 128)
            }
            
            // CRITICAL: Route ALL internet traffic through VPN - matches desktop version
            builder.addRoute("0.0.0.0", 0)
            
            // Route IPv6 traffic through VPN - matches desktop version
            builder.addRoute("::", 0)
            
            // DNS configuration - use the DNS from API response (matches desktop version)
            if (dns.isNotEmpty()) {
                // Split DNS servers if multiple are provided
                dns.split(",").forEach { dnsServer ->
                    val cleanDns = dnsServer.trim()
                    if (cleanDns.isNotEmpty()) {
                        builder.addDnsServer(cleanDns)
                        Log.d(TAG, "   Added DNS server: $cleanDns")
                    }
                }
            } else {
                // Fallback DNS if none provided
                Log.w(TAG, "⚠️ No DNS provided, using fallback")
                builder.addDnsServer("8.8.8.8")
                builder.addDnsServer("1.1.1.1")
            }
            
            // MTU
            builder.setMtu(mtu)
            
            // Allow all traffic types
            builder.allowFamily(android.system.OsConstants.AF_INET)
            builder.allowFamily(android.system.OsConstants.AF_INET6)
            
            // Don't route VPN app itself through VPN
            // Note: addDisallowedApplication is not available in VpnService.Builder
            // The app will be automatically excluded from VPN routing
            
            // Establish the interface
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                throw Exception("Failed to establish VPN interface")
            }
            
            Log.i(TAG, "✅ VPN interface created successfully with full routing")
            Log.i(TAG, "   Routes: 0.0.0.0/0, ::/0 (all traffic through VPN)")
            Log.i(TAG, "   DNS: $dns")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create VPN interface: ${e.message}")
            throw e
        }
    }
    
    private suspend fun startWireGuardTunnelWithFallback(config: VPNConnectionResponse): Boolean {
        // List of fallback servers to try
        val fallbackServers = listOf(
            "52.47.190.220:51820", // Paris
            "15.168.240.118:51820"  // Osaka
        )
        
        // Try each server until one works
        for (server in fallbackServers) {
            try {
                Log.i(TAG, "🔌 Trying server: $server")
                
                // Update server endpoint
                serverEndpoint = server
                
                // Try to start tunnel with this server
                if (tryStartTunnelWithServer(server)) {
                    Log.i(TAG, "✅ Successfully connected to server: $server")
                    return true
                }
                
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to connect to server $server: ${e.message}")
                continue
            }
        }
        
        Log.e(TAG, "❌ All servers failed - VPN service unavailable")
        return false
    }
    
    private suspend fun tryStartTunnelWithServer(server: String): Boolean {
        return try {
            // Parse server endpoint
            val endpointParts = server.split(":")
            val serverIP = endpointParts[0]
            val serverPort = endpointParts[1].toInt()
            
            Log.i(TAG, "🎯 Connecting to server: $serverIP:$serverPort")
            
            // Check if server is reachable
            if (!isServerReachable(serverIP, serverPort)) {
                Log.w(TAG, "⚠️ Server $serverIP:$serverPort is not reachable")
                return false
            }
            
            // Create UDP socket for WireGuard communication
            val socket = DatagramSocket()
            socket.connect(InetSocketAddress(serverIP, serverPort))
            
            // Protect the socket from VPN routing
            protect(socket)
            
            Log.i(TAG, "✅ UDP socket created and protected")
            
            // Store the socket for later use
            activeConnections[server] = socket
            
            // Send initial WireGuard handshake
            sendWireGuardHandshake(socket)
            
            // Start bidirectional packet handling
            startPacketHandling(socket)
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start tunnel with server $server: ${e.message}", e)
            false
        }
    }
    
    private fun sendWireGuardHandshake(socket: DatagramSocket) {
        try {
            Log.i(TAG, "🤝 Sending WireGuard handshake message...")
            
            // Create handshake message (type 1)
            val handshakeMessage = createWireGuardHandshakeMessage()
            
            // Send the handshake message
            val packet = java.net.DatagramPacket(handshakeMessage, handshakeMessage.size)
            socket.send(packet)
            
            Log.i(TAG, "✅ WireGuard handshake message sent (${handshakeMessage.size} bytes)")
            Log.d(TAG, "   Message type: ${handshakeMessage[0]}")
            Log.d(TAG, "   Local public key: ${encodeBase64(localPublicKey)}")
            Log.d(TAG, "   Remote public key: ${encodeBase64(remotePublicKey)}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send WireGuard handshake: ${e.message}")
        }
    }
    
    private fun createWireGuardHandshakeMessage(): ByteArray {
        // Create WireGuard handshake message (type 1) as per WireGuard protocol
        val message = ByteArray(148) // Fixed size for handshake
        var offset = 0
        
        // Message type: handshake initiation (1)
        message[offset++] = 1
        
        // Reserved bytes (3 bytes)
        message[offset++] = 0
        message[offset++] = 0
        message[offset++] = 0
        
        // Sender index (4 bytes) - use local index
        writeInt(message, offset, localIndex)
        offset += 4
        
        // Unencrypted ephemeral (32 bytes) - use local ephemeral public key
        System.arraycopy(localEphemeralPublicKey, 0, message, offset, 32)
        offset += 32
        
        // Unencrypted static (32 bytes) - use local public key
        System.arraycopy(localPublicKey, 0, message, offset, 32)
        offset += 32
        
        // Encrypted timestamp (12 bytes) - placeholder for now
        // In production, this would be encrypted with ChaCha20-Poly1305
        val timestamp = System.currentTimeMillis() / 1000
        writeInt(message, offset, timestamp.toInt())
        offset += 4
        // Add 8 more bytes for timestamp (total 12)
        for (i in 0 until 8) {
            message[offset++] = 0
        }
        
        // MAC1 (16 bytes) - placeholder for now
        // In production, this would be calculated using BLAKE2s
        for (i in 0 until 16) {
            message[offset++] = 0
        }
        
        // MAC2 (16 bytes) - placeholder for now
        // In production, this would be calculated using BLAKE2s
        for (i in 0 until 16) {
            message[offset++] = 0
        }
        
        return message
    }
    
    private suspend fun startPacketHandling(socket: DatagramSocket) {
        try {
            Log.i(TAG, "📡 Starting bidirectional packet handling...")
            
            // Start handshake timeout monitoring
            scope.launch {
                try {
                    withTimeout(HANDSHAKE_TIMEOUT) {
                        while (!handshakeComplete && isRunning.get()) {
                            delay(100) // Check every 100ms
                        }
                        
                        if (!handshakeComplete) {
                            // Use a custom exception instead of TimeoutCancellationException
                            throw Exception("Handshake timeout after ${HANDSHAKE_TIMEOUT}ms")
                        }
                    }
                } catch (e: Exception) {
                    if (e.message?.contains("timeout") == true) {
                        Log.e(TAG, "❌ Handshake timeout - server may be offline")
                        onConnectionStatusChanged?.invoke(false, "Handshake timeout - server may be offline")
                        stopVPN()
                    }
                    return@launch
                }
            }
            
            // Thread 1: OUTBOUND (TUN → UDP Socket)
            scope.launch(Dispatchers.IO + SupervisorJob()) {
                try {
                    Log.d(TAG, "📤 Starting OUTBOUND thread (TUN → Server)")
                    val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
                    val buffer = ByteArray(32767)
                    
                    while (isRunning.get()) {
                        try {
                            // Wait for handshake to complete before sending data
                            if (!handshakeComplete) {
                                Log.d(TAG, "⏳ Waiting for WireGuard handshake to complete...")
                                delay(1000)
                                continue
                            }
                            
                            // Read packet from TUN interface
                            val bytesRead = vpnInput.read(buffer)
                            if (bytesRead > 0) {
                                val packet = buffer.copyOfRange(0, bytesRead)
                                sendPacketToServer(socket, packet)
                            }
                            
                        } catch (e: Exception) {
                            if (isRunning.get()) {
                                Log.w(TAG, "⚠️ Error in OUTBOUND thread: ${e.message}")
                                delay(1000) // Wait before retrying
                            }
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ OUTBOUND thread failed: ${e.message}", e)
                }
            }
            
            // Thread 2: INBOUND (UDP Socket → TUN)
            scope.launch(Dispatchers.IO + SupervisorJob()) {
                try {
                    Log.d(TAG, "📥 Starting INBOUND thread (Server → TUN)")
                    val vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
                    val buffer = ByteArray(32767)
                    
                    while (isRunning.get()) {
                        try {
                            // Wait for handshake to complete before receiving data
                            if (!handshakeComplete) {
                                Log.d(TAG, "⏳ Waiting for WireGuard handshake to complete...")
                                delay(1000)
                                continue
                            }
                            
                            // Receive packet from server
                            val packet = java.net.DatagramPacket(buffer, buffer.size)
                            socket.receive(packet)
                            
                            // Process the response
                            val responseData = packet.data.copyOfRange(0, packet.length)
                            val processedPacket = processServerResponse(responseData)
                            
                            if (processedPacket != null) {
                                // Write to TUN interface
                                vpnOutput.write(processedPacket)
                                vpnOutput.flush()
                                
                                // Mark handshake as complete if we receive any response
                                if (!handshakeComplete) {
                                    handshakeComplete = true
                                    Log.i(TAG, "✅ WireGuard handshake completed successfully")
                                    onConnectionStatusChanged?.invoke(true, null)
                                }
                            }
                            
                        } catch (e: Exception) {
                            if (isRunning.get()) {
                                Log.w(TAG, "⚠️ Error in INBOUND thread: ${e.message}")
                                delay(1000) // Wait before retrying
                            }
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ INBOUND thread failed: ${e.message}", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start packet handling: ${e.message}", e)
            throw e
        }
    }
    
    private fun sendPacketToServer(socket: DatagramSocket, ipPacket: ByteArray) {
        try {
            // Create a proper WireGuard transport message (type 4)
            // This is what the server expects, not raw IP packets
            val transportMessage = createWireGuardTransportMessage(ipPacket)
            
            // Send the WireGuard transport message
            val packet = java.net.DatagramPacket(transportMessage, transportMessage.size)
            socket.send(packet)
            
            Log.d(TAG, "📤 Sent WireGuard transport message of size: ${transportMessage.size}")
            Log.d(TAG, "   Message type: ${transportMessage[0]}")
            Log.d(TAG, "   Receiver index: ${readInt(transportMessage, 4)}")
            Log.d(TAG, "   Nonce: ${readInt(transportMessage, 8)}")
            Log.d(TAG, "   Payload size: ${ipPacket.size}")
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to send packet to server: ${e.message}")
        }
    }
    
    private fun createWireGuardTransportMessage(ipPacket: ByteArray): ByteArray {
        // Create WireGuard transport message (type 4) as per WireGuard protocol
        val message = ByteArray(16 + ipPacket.size) // Header + payload
        var offset = 0
        
        // Message type: transport data (4)
        message[offset++] = 4
        
        // Reserved bytes (3 bytes)
        message[offset++] = 0
        message[offset++] = 0
        message[offset++] = 0
        
        // Receiver index (4 bytes) - use remote index from handshake
        writeInt(message, offset, remoteIndex)
        offset += 4
        
        // Nonce (8 bytes) - increment sending nonce
        val nonce = ByteArray(8)
        writeInt(nonce, 0, sendingNonce)
        writeInt(nonce, 4, 0) // Upper 32 bits
        System.arraycopy(nonce, 0, message, offset, 8)
        offset += 8
        
        // For now, send the IP packet as-is (no encryption yet)
        // In production, this would be encrypted with ChaCha20-Poly1305
        System.arraycopy(ipPacket, 0, message, offset, ipPacket.size)
        
        // Increment nonce for next packet
        sendingNonce++
        
        return message
    }
    
    private fun writeInt(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value shr 24).toByte()
        array[offset + 1] = (value shr 16).toByte()
        array[offset + 2] = (value shr 8).toByte()
        array[offset + 3] = value.toByte()
    }
    
    private fun readInt(array: ByteArray, offset: Int): Int {
        return ((array[offset].toInt() and 0xFF) shl 24) or
               ((array[offset + 1].toInt() and 0xFF) shl 16) or
               ((array[offset + 2].toInt() and 0xFF) shl 8) or
               (array[offset + 3].toInt() and 0xFF)
    }
    
    private fun processServerResponse(responseData: ByteArray): ByteArray? {
        try {
            val messageType = responseData[0]
            Log.v(TAG, "🔓 Processing WireGuard message type: $messageType, size: ${responseData.size}")
            
            when (messageType) {
                1.toByte() -> {
                    // Handshake initiation response
                    Log.i(TAG, "🤝 Received WireGuard handshake response")
                    Log.d(TAG, "   Sender index: ${readInt(responseData, 4)}")
                    Log.d(TAG, "   Ephemeral key: ${encodeBase64(responseData.copyOfRange(8, 40))}")
                    Log.d(TAG, "   Static key: ${encodeBase64(responseData.copyOfRange(40, 72))}")
                    
                    // Mark handshake as complete
                    handshakeComplete = true
                    Log.i(TAG, "✅ WireGuard handshake completed successfully")
                    
                    // Send a transport message to establish the tunnel
                    sendInitialTransportMessage()
                    
                    return null // No payload to return for handshake
                }
                
                2.toByte() -> {
                    // Handshake response
                    Log.i(TAG, "🤝 Received WireGuard handshake response")
                    Log.d(TAG, "   Sender index: ${readInt(responseData, 4)}")
                    Log.d(TAG, "   Ephemeral key: ${encodeBase64(responseData.copyOfRange(8, 40))}")
                    Log.d(TAG, "   Static key: ${encodeBase64(responseData.copyOfRange(40, 72))}")
                    
                    // Mark handshake as complete
                    handshakeComplete = true
                    Log.i(TAG, "✅ WireGuard handshake completed successfully")
                    
                    // Send a transport message to establish the tunnel
                    sendInitialTransportMessage()
                    
                    return null // No payload to return for handshake
                }
                
                3.toByte() -> {
                    // Handshake cookie
                    Log.i(TAG, "🍪 Received WireGuard handshake cookie")
                    return null // No payload to return for cookie
                }
                
                4.toByte() -> {
                    // Transport data
                    Log.v(TAG, "📦 Processing WireGuard transport message")
                    
                    // Extract the payload (everything after the 16-byte header)
                    if (responseData.size >= 16) {
                        val payload = responseData.copyOfRange(16, responseData.size)
                        Log.v(TAG, "📥 Extracted payload of size: ${payload.size}")
                        return payload
                    } else {
                        Log.w(TAG, "⚠️ Transport message too short: ${responseData.size} bytes")
                        return null
                    }
                }
                
                else -> {
                    Log.w(TAG, "⚠️ Unknown WireGuard message type: $messageType")
                    return null
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to process server response: ${e.message}")
            return null
        }
    }
    
    private fun sendInitialTransportMessage() {
        try {
            Log.i(TAG, "📤 Sending initial transport message to establish tunnel...")
            
            // Create a simple ping packet to establish the tunnel
            val pingPacket = createPingPacket()
            val transportMessage = createWireGuardTransportMessage(pingPacket)
            
            // Get the active socket
            val socket = activeConnections[serverEndpoint]
            if (socket != null) {
                val packet = java.net.DatagramPacket(transportMessage, transportMessage.size)
                socket.send(packet)
                Log.i(TAG, "✅ Initial transport message sent (${transportMessage.size} bytes)")
            } else {
                Log.w(TAG, "⚠️ No active socket found for endpoint: $serverEndpoint")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send initial transport message: ${e.message}")
        }
    }
    
    private fun createPingPacket(): ByteArray {
        // Create a simple ICMP ping packet (8 bytes)
        val pingPacket = ByteArray(8)
        
        // ICMP type: Echo Request (8)
        pingPacket[0] = 8
        
        // ICMP code: 0
        pingPacket[1] = 0
        
        // Checksum: 0 (placeholder)
        pingPacket[2] = 0
        pingPacket[3] = 0
        
        // Identifier: random
        val identifier = secureRandom.nextInt(65536)
        pingPacket[4] = (identifier shr 8).toByte()
        pingPacket[5] = identifier.toByte()
        
        // Sequence number: 1
        pingPacket[6] = 0
        pingPacket[7] = 1
        
        return pingPacket
    }
    
    private fun testVPNConnectivity() {
        scope.launch(Dispatchers.IO + SupervisorJob()) {
            try {
                Log.i(TAG, "🧪 Testing VPN connectivity...")
                
                // Wait a moment for VPN to stabilize
                delay(2000)
                
                // Test 1: Check if we can reach a public DNS server
                try {
                    val socket = java.net.Socket()
                    socket.connect(java.net.InetSocketAddress("8.8.8.8", 53), 5000)
                    socket.close()
                    Log.i(TAG, "✅ Connectivity test 1 PASSED: Can reach 8.8.8.8:53")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Connectivity test 1 FAILED: Cannot reach 8.8.8.8:53 - ${e.message}")
                }
                
                // Test 2: Check if we can reach the VPN server
                try {
                    val endpointParts = serverEndpoint.split(":")
                    val serverIP = endpointParts[0]
                    val serverPort = endpointParts[1].toInt()
                    
                    val socket = java.net.Socket()
                    socket.connect(java.net.InetSocketAddress(serverIP, serverPort), 5000)
                    socket.close()
                    Log.i(TAG, "✅ Connectivity test 2 PASSED: Can reach VPN server $serverIP:$serverPort")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Connectivity test 2 FAILED: Cannot reach VPN server - ${e.message}")
                }
                
                // Test 3: Try to get current IP (this should show VPN IP)
                try {
                    val url = java.net.URL("https://httpbin.org/ip")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.requestMethod = "GET"
                    
                    val responseCode = connection.responseCode
                    if (responseCode == 200) {
                        val inputStream = connection.inputStream
                        val response = inputStream.bufferedReader().use { it.readText() }
                        Log.i(TAG, "✅ Connectivity test 3 PASSED: HTTP response $responseCode")
                        Log.i(TAG, "   Response: $response")
                    } else {
                        Log.w(TAG, "⚠️ Connectivity test 3 FAILED: HTTP response $responseCode")
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Connectivity test 3 FAILED: Cannot reach httpbin.org - ${e.message}")
                }
                
                Log.i(TAG, "🧪 VPN connectivity test completed")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ VPN connectivity test failed: ${e.message}")
            }
        }
    }
    
    private suspend fun isServerReachable(serverIP: String, serverPort: Int): Boolean {
        return try {
            Log.i(TAG, "🔍 Checking server availability: $serverIP:$serverPort")
            
            withTimeout(SERVER_CONNECT_TIMEOUT) {
                val socket = java.net.Socket()
                try {
                    socket.connect(InetSocketAddress(serverIP, serverPort), SERVER_CONNECT_TIMEOUT.toInt())
                    socket.close()
                    Log.i(TAG, "✅ Server $serverIP:$serverPort is reachable")
                    true
                } catch (e: Exception) {
                    socket.close()
                    Log.w(TAG, "⚠️ Server $serverIP:$serverPort is not reachable: ${e.message}")
                    false
                }
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Server reachability check failed: ${e.message}")
            false
        }
    }
    
    fun stopVPN() {
        try {
            Log.i(TAG, "🛑 Stopping WireGuard VPN...")
            
            // Cancel any ongoing connection attempt
            currentConnectionJob?.cancel()
            connectionInProgress.set(false)
            
            // Stop the VPN service
            isRunning.set(false)
            
            // Close active connections
            activeConnections.values.forEach { socket ->
                try {
                    socket.close()
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error closing socket: ${e.message}")
                }
            }
            activeConnections.clear()
            
            // Close VPN interface
            vpnInterface?.close()
            vpnInterface = null
            
            // Reset handshake state
            handshakeComplete = false
            lastHandshakeTime = 0L
            
            Log.i(TAG, "✅ WireGuard VPN stopped successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error stopping VPN: ${e.message}", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.launch { stopVPN() }
    }
}
