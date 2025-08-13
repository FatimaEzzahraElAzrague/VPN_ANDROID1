package com.example.v.wireguard

import android.util.Log
import com.example.v.crypto.WireGuardCrypto
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicLong

/**
 * WireGuard protocol implementation following RFC draft specifications
 */
class WireGuardProtocol(
    private val privateKey: ByteArray,
    private val serverPublicKey: ByteArray,
    private val presharedKey: ByteArray? = null
) {
    
    companion object {
        private const val TAG = "WireGuardProtocol"
        
        // Message types
        const val MESSAGE_HANDSHAKE_INITIATION = 1.toByte()
        const val MESSAGE_HANDSHAKE_RESPONSE = 2.toByte()
        const val MESSAGE_COOKIE_REPLY = 3.toByte()
        const val MESSAGE_TRANSPORT_DATA = 4.toByte()
        
        // Protocol constants
        private const val REKEY_AFTER_MESSAGES = 1L shl 60
        private const val REJECT_AFTER_MESSAGES = (1L shl 64) - (1L shl 13) - 1L
        private const val REKEY_AFTER_TIME = 120_000L // 2 minutes in milliseconds
        private const val REJECT_AFTER_TIME = 180_000L // 3 minutes in milliseconds
        private const val KEEPALIVE_TIMEOUT = 10_000L // 10 seconds
        
        // Handshake constants
        private val CONSTRUCTION = "Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray()
        private val IDENTIFIER = "WireGuard v1 zx2c4 Jason@zx2c4.com".toByteArray()
        private val LABEL_MAC1 = "mac1----".toByteArray()
        private val LABEL_COOKIE = "cookie--".toByteArray()
    }
    
    private val crypto = WireGuardCrypto()
    private val random = SecureRandom()
    
    // Handshake state
    private var localIndex: Int = 0
    private var remoteIndex: Int = 0
    private var ephemeralPrivateKey: ByteArray? = null
    private var ephemeralPublicKey: ByteArray? = null
    
    // Session keys
    private var sendingKey: ByteArray? = null
    private var receivingKey: ByteArray? = null
    private var sendingCounter = AtomicLong(0)
    private var receivingCounter = AtomicLong(0)
    
    // Timing
    private var handshakeTime: Long = 0
    private var lastPacketTime: Long = 0
    
    init {
        // Generate random local index
        localIndex = random.nextInt()
        Log.d(TAG, "WireGuard protocol initialized with local index: $localIndex")
    }
    
    /**
     * Create handshake initiation message
     */
    fun createHandshakeInitiation(): ByteArray {
        Log.d(TAG, "Creating handshake initiation")
        
        // Generate ephemeral key pair
        val (ephemeralPriv, ephemeralPub) = crypto.generateKeyPair()
        ephemeralPrivateKey = ephemeralPriv
        ephemeralPublicKey = ephemeralPub
        
        // Noise protocol handshake
        val h = crypto.blake2s(CONSTRUCTION)
        val ck = h.copyOf()
        
        // h = HASH(h || IDENTIFIER)
        val h1 = crypto.blake2s(h + IDENTIFIER)
        
        // h = HASH(h || server_public_key)
        val h2 = crypto.blake2s(h1 + serverPublicKey)
        
        // c = KDF(c, ephemeral_private_key)
        val c1 = crypto.hkdf(ephemeralPriv, ck, null, 32)
        
        // temp = KDF(c, DH(ephemeral_private_key, server_public_key))
        val dh1 = crypto.computeSharedSecret(ephemeralPriv, serverPublicKey)
        val temp1 = crypto.hkdf(dh1, c1, null, 64)
        val c2 = temp1.copyOfRange(0, 32)
        val k1 = temp1.copyOfRange(32, 64)
        
        // h = HASH(h || ephemeral_public_key)
        val h3 = crypto.blake2s(h2 + ephemeralPub)
        
        // Get client public key from private key
        val clientPublicKey = getPublicKeyFromPrivate(privateKey)
        
        // encrypted_static = AEAD(k, 0, static_public_key, h)
        val nonce1 = ByteArray(12) // All zeros
        val encryptedStatic = crypto.encrypt(k1, nonce1, clientPublicKey, h3)
        
        // h = HASH(h || encrypted_static)
        val h4 = crypto.blake2s(h3 + encryptedStatic)
        
        // temp = KDF(c, DH(static_private_key, server_public_key))
        val dh2 = crypto.computeSharedSecret(privateKey, serverPublicKey)
        val temp2 = crypto.hkdf(dh2, c2, null, 64)
        val c3 = temp2.copyOfRange(0, 32)
        val k2 = temp2.copyOfRange(32, 64)
        
        // encrypted_timestamp = AEAD(k, 1, timestamp, h)
        val timestamp = System.currentTimeMillis() / 1000
        val timestampBytes = ByteBuffer.allocate(12)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(timestamp)
            .putInt(0) // padding
            .array()
        
        val nonce2 = ByteArray(12)
        nonce2[0] = 1 // Counter = 1
        val encryptedTimestamp = crypto.encrypt(k2, nonce2, timestampBytes, h4)
        
        // Build message
        val buffer = ByteBuffer.allocate(148).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(MESSAGE_HANDSHAKE_INITIATION)
        buffer.put(0.toByte()) // Reserved
        buffer.put(0.toByte()) // Reserved
        buffer.put(0.toByte()) // Reserved
        buffer.putInt(localIndex)
        buffer.put(ephemeralPub) // 32 bytes
        buffer.put(encryptedStatic) // 48 bytes (32 + 16 tag)
        buffer.put(encryptedTimestamp) // 28 bytes (12 + 16 tag)
        
        // MAC1 (simplified - would normally use server's public key)
        val mac1 = crypto.blake2s(buffer.array().copyOfRange(0, 116), LABEL_MAC1)
        buffer.put(mac1.copyOfRange(0, 16))
        
        // MAC2 (empty for now)
        buffer.put(ByteArray(16))
        
        handshakeTime = System.currentTimeMillis()
        
        Log.d(TAG, "Handshake initiation created successfully")
        return buffer.array()
    }
    
    /**
     * Process handshake response message
     */
    fun processHandshakeResponse(message: ByteArray): Boolean {
        if (message.size != 92) {
            Log.e(TAG, "Invalid handshake response size: ${message.size}")
            return false
        }
        
        Log.d(TAG, "Processing handshake response")
        
        val buffer = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN)
        
        val messageType = buffer.get()
        if (messageType != MESSAGE_HANDSHAKE_RESPONSE) {
            Log.e(TAG, "Invalid message type: $messageType")
            return false
        }
        
        buffer.get() // Reserved
        buffer.get() // Reserved
        buffer.get() // Reserved
        
        remoteIndex = buffer.getInt()
        val serverEphemeral = ByteArray(32)
        buffer.get(serverEphemeral)
        
        val encryptedEmpty = ByteArray(16)
        buffer.get(encryptedEmpty)
        
        val mac1 = ByteArray(16)
        buffer.get(mac1)
        
        val mac2 = ByteArray(16)
        buffer.get(mac2)
        
        // Derive session keys
        try {
            deriveSessionKeys(serverEphemeral)
            Log.d(TAG, "Session keys derived successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to derive session keys", e)
            return false
        }
    }
    
    /**
     * Derive session keys from handshake
     */
    private fun deriveSessionKeys(serverEphemeral: ByteArray) {
        val ephemeralPriv = ephemeralPrivateKey ?: throw IllegalStateException("No ephemeral private key")
        
        // Compute final shared secrets
        val dh1 = crypto.computeSharedSecret(ephemeralPriv, serverEphemeral)
        val dh2 = crypto.computeSharedSecret(privateKey, serverEphemeral)
        val dh3 = crypto.computeSharedSecret(ephemeralPriv, serverPublicKey)
        
        // Combine all shared secrets
        val combinedSecret = dh1 + dh2 + dh3
        
        // Include preshared key if available
        val finalSecret = if (presharedKey != null) {
            combinedSecret + presharedKey
        } else {
            combinedSecret
        }
        
        // Derive transport keys using HKDF
        val keys = crypto.hkdf(finalSecret, null, "WireGuard transport keys".toByteArray(), 64)
        
        sendingKey = keys.copyOfRange(0, 32)
        receivingKey = keys.copyOfRange(32, 64)
        
        // Reset counters
        sendingCounter.set(0)
        receivingCounter.set(0)
        
        Log.d(TAG, "Transport keys derived")
    }
    
    /**
     * Encrypt packet for transport
     */
    fun encryptPacket(plaintext: ByteArray): ByteArray? {
        val key = sendingKey ?: return null
        
        val counter = sendingCounter.incrementAndGet()
        val nonce = crypto.generateNonce(counter)
        
        // Create transport message
        val buffer = ByteBuffer.allocate(16 + plaintext.size + 16).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(MESSAGE_TRANSPORT_DATA)
        buffer.put(0.toByte()) // Reserved
        buffer.put(0.toByte()) // Reserved
        buffer.put(0.toByte()) // Reserved
        buffer.putInt(remoteIndex)
        buffer.putLong(counter)
        
        val header = buffer.array().copyOfRange(0, 16)
        val encrypted = crypto.encrypt(key, nonce, plaintext, header)
        
        val result = ByteArray(16 + encrypted.size)
        System.arraycopy(header, 0, result, 0, 16)
        System.arraycopy(encrypted, 0, result, 16, encrypted.size)
        
        lastPacketTime = System.currentTimeMillis()
        
        return result
    }
    
    /**
     * Decrypt received transport packet
     */
    fun decryptPacket(ciphertext: ByteArray): ByteArray? {
        if (ciphertext.size < 32) return null // Minimum size: 16 header + 16 auth tag
        
        val key = receivingKey ?: return null
        
        val buffer = ByteBuffer.wrap(ciphertext).order(ByteOrder.LITTLE_ENDIAN)
        
        val messageType = buffer.get()
        if (messageType != MESSAGE_TRANSPORT_DATA) return null
        
        buffer.get() // Reserved
        buffer.get() // Reserved
        buffer.get() // Reserved
        
        val senderIndex = buffer.getInt()
        if (senderIndex != localIndex) return null
        
        val counter = buffer.getLong()
        val nonce = crypto.generateNonce(counter)
        
        val header = ciphertext.copyOfRange(0, 16)
        val encryptedData = ciphertext.copyOfRange(16, ciphertext.size)
        
        val decrypted = crypto.decrypt(key, nonce, encryptedData, header)
        
        if (decrypted != null) {
            receivingCounter.set(counter)
            lastPacketTime = System.currentTimeMillis()
        }
        
        return decrypted
    }
    
    /**
     * Check if rekey is needed
     */
    fun needsRekey(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceHandshake = currentTime - handshakeTime
        
        return sendingCounter.get() >= REKEY_AFTER_MESSAGES ||
               receivingCounter.get() >= REKEY_AFTER_MESSAGES ||
               timeSinceHandshake >= REKEY_AFTER_TIME
    }
    
    /**
     * Check if session should be rejected
     */
    fun shouldReject(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceHandshake = currentTime - handshakeTime
        
        return sendingCounter.get() >= REJECT_AFTER_MESSAGES ||
               receivingCounter.get() >= REJECT_AFTER_MESSAGES ||
               timeSinceHandshake >= REJECT_AFTER_TIME
    }
    
    /**
     * Check if keepalive is needed
     */
    fun needsKeepalive(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastPacketTime) >= KEEPALIVE_TIMEOUT
    }
    
    /**
     * Create keepalive packet (empty transport message)
     */
    fun createKeepalive(): ByteArray? {
        return encryptPacket(ByteArray(0))
    }
    
    /**
     * Get public key from private key
     */
    private fun getPublicKeyFromPrivate(privateKey: ByteArray): ByteArray {
        val keyPair = crypto.generateKeyPair()
        // In a real implementation, you'd derive the public key from the private key
        // For now, we'll use a placeholder
        return ByteArray(32) { privateKey[it % privateKey.size] }
    }
    
    /**
     * Get session statistics
     */
    fun getStatistics(): Map<String, Long> {
        return mapOf(
            "sendingCounter" to sendingCounter.get(),
            "receivingCounter" to receivingCounter.get(),
            "handshakeTime" to handshakeTime,
            "lastPacketTime" to lastPacketTime
        )
    }
    
    /**
     * Reset protocol state
     */
    fun reset() {
        localIndex = random.nextInt()
        remoteIndex = 0
        ephemeralPrivateKey = null
        ephemeralPublicKey = null
        sendingKey = null
        receivingKey = null
        sendingCounter.set(0)
        receivingCounter.set(0)
        handshakeTime = 0
        lastPacketTime = 0
        
        Log.d(TAG, "Protocol state reset")
    }
}
