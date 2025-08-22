package com.example.v.wireguard

import android.util.Log
import java.security.SecureRandom
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.xor

/**
 * WireGuard protocol implementation
 * Handles the Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s handshake and transport
 */
class WireGuardProtocol(
    private val localPrivateKey: ByteArray,
    private val remotePublicKey: ByteArray,
    private val presharedKey: ByteArray? = null
) {
    companion object {
        private const val TAG = "WireGuardProtocol"
        private val CONSTRUCTION = "Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray()
        private val IDENTIFIER = "WireGuard v1 zx2c4 Jason@zx2c4.com".toByteArray()
        private val LABEL_MAC1 = "mac1----".toByteArray()
        private val LABEL_COOKIE = "cookie--".toByteArray()
    }
    
    // Protocol state
    private var state = HandshakeState.INITIAL
    private var localIndex: Int = 0
    private var remoteIndex: Int = 0
    private var sendingKey: ByteArray? = null
    private var receivingKey: ByteArray? = null
    private var sendingCounter: Long = 0
    private var receivingCounter: Long = 0
    
    // Ephemeral keys for current handshake
    private var localEphemeralPrivate: ByteArray? = null
    private var localEphemeralPublic: ByteArray? = null
    private var remoteEphemeralPublic: ByteArray? = null
    
    // Noise protocol state
    private var chainingKey: ByteArray? = null
    private var hash: ByteArray? = null
    
    enum class HandshakeState {
        INITIAL,
        INITIATION_SENT,
        RESPONSE_RECEIVED,
        ESTABLISHED
    }
    
    init {
        // Generate random local index
        localIndex = SecureRandom().nextInt()
        initializeProtocol()
    }
    
    private fun initializeProtocol() {
        // Initialize Noise protocol
        chainingKey = WireGuardCrypto.hkdf(
            CONSTRUCTION,
            ByteArray(0),
            IDENTIFIER,
            32
        )
        
        hash = mixHash(chainingKey!!, IDENTIFIER)
        hash = mixHash(hash!!, remotePublicKey)
        
        Log.d(TAG, "WireGuard protocol initialized")
    }
    
    /**
     * Create handshake initiation message
     */
    fun createHandshakeInitiation(): WireGuardMessages.HandshakeInitiation {
        if (state != HandshakeState.INITIAL) {
            throw IllegalStateException("Cannot create initiation in state: $state")
        }
        
        // Generate ephemeral key pair
        localEphemeralPrivate = WireGuardCrypto.generatePrivateKey()
        localEphemeralPublic = WireGuardCrypto.getPublicKey(localEphemeralPrivate!!)
        
        // Mix ephemeral public key into hash
        hash = mixHash(hash!!, localEphemeralPublic!!)
        
        // Perform DH with remote static key
        val dh1 = WireGuardCrypto.performKeyExchange(localEphemeralPrivate!!, remotePublicKey)
        chainingKey = mixKey(chainingKey!!, dh1)
        
        // Encrypt static public key
        val localPublicKey = WireGuardCrypto.getPublicKey(localPrivateKey)
        val (encryptedStatic, newHash1) = encryptAndHash(localPublicKey)
        hash = newHash1
        
        // Perform second DH
        val dh2 = WireGuardCrypto.performKeyExchange(localPrivateKey, remotePublicKey)
        chainingKey = mixKey(chainingKey!!, dh2)
        
        // Mix preshared key if present
        if (presharedKey != null) {
            chainingKey = mixKey(chainingKey!!, presharedKey)
        }
        
        // Encrypt timestamp
        val timestamp = createTimestamp()
        val (encryptedTimestamp, newHash2) = encryptAndHash(timestamp)
        hash = newHash2
        
        // Calculate MAC1 (simplified - should use proper MAC calculation)
        val mac1 = calculateMac1(localEphemeralPublic!! + encryptedStatic + encryptedTimestamp)
        val mac2 = ByteArray(16) // Cookie MAC (empty for now)
        
        state = HandshakeState.INITIATION_SENT
        
        Log.d(TAG, "Created handshake initiation for index $localIndex")
        
        return WireGuardMessages.HandshakeInitiation(
            senderIndex = localIndex,
            unencryptedEphemeral = localEphemeralPublic!!,
            encryptedStatic = encryptedStatic,
            encryptedTimestamp = encryptedTimestamp,
            mac1 = mac1,
            mac2 = mac2
        )
    }
    
    /**
     * Process handshake response message
     */
    fun processHandshakeResponse(response: WireGuardMessages.HandshakeResponse): Boolean {
        if (state != HandshakeState.INITIATION_SENT) {
            Log.w(TAG, "Received response in wrong state: $state")
            return false
        }
        
        try {
            remoteIndex = response.senderIndex
            remoteEphemeralPublic = response.unencryptedEphemeral
            
            // Derive transport keys
            deriveTransportKeys()
            
            state = HandshakeState.ESTABLISHED
            
            Log.i(TAG, "✅ Handshake completed! Remote index: $remoteIndex")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process handshake response: ${e.message}")
            return false
        }
    }
    
    /**
     * Encrypt transport data
     */
    fun encryptTransportData(plaintext: ByteArray): WireGuardMessages.TransportData {
        if (state != HandshakeState.ESTABLISHED) {
            throw IllegalStateException("Cannot encrypt data - handshake not established")
        }
        
        if (sendingKey == null) {
            throw IllegalStateException("Sending key not available")
        }
        
        // Create nonce from counter
        val nonce = ByteArray(12)
        ByteBuffer.wrap(nonce).order(ByteOrder.LITTLE_ENDIAN).putLong(4, sendingCounter)
        
        // Encrypt data
        val encryptedData = WireGuardCrypto.encryptChaCha20Poly1305(
            sendingKey!!,
            nonce,
            plaintext
        )
        
        val message = WireGuardMessages.TransportData(
            receiverIndex = remoteIndex,
            counter = sendingCounter,
            encryptedData = encryptedData
        )
        
        sendingCounter++
        
        Log.v(TAG, "Encrypted ${plaintext.size} bytes -> ${encryptedData.size} bytes")
        
        return message
    }
    
    /**
     * Decrypt transport data
     */
    fun decryptTransportData(transportData: WireGuardMessages.TransportData): ByteArray {
        if (state != HandshakeState.ESTABLISHED) {
            throw IllegalStateException("Cannot decrypt data - handshake not established")
        }
        
        if (receivingKey == null) {
            throw IllegalStateException("Receiving key not available")
        }
        
        // Create nonce from counter
        val nonce = ByteArray(12)
        ByteBuffer.wrap(nonce).order(ByteOrder.LITTLE_ENDIAN).putLong(4, transportData.counter)
        
        // Decrypt data
        val plaintext = WireGuardCrypto.decryptChaCha20Poly1305(
            receivingKey!!,
            nonce,
            transportData.encryptedData
        )
        
        receivingCounter = transportData.counter + 1
        
        Log.v(TAG, "Decrypted ${transportData.encryptedData.size} bytes -> ${plaintext.size} bytes")
        
        return plaintext
    }
    
    // Helper functions
    
    private fun mixHash(hash: ByteArray, data: ByteArray): ByteArray {
        // Simplified hash mixing - should use BLAKE2s
        return WireGuardCrypto.hkdf(hash + data, ByteArray(0), ByteArray(0), 32)
    }
    
    private fun mixKey(chainingKey: ByteArray, inputKeyMaterial: ByteArray): ByteArray {
        return WireGuardCrypto.hkdf(inputKeyMaterial, chainingKey, ByteArray(0), 32)
    }
    
    private fun encryptAndHash(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        // Derive temporary key
        val tempKey = WireGuardCrypto.hkdf(chainingKey!!, ByteArray(0), ByteArray(0), 32)
        
        // Encrypt with ChaCha20-Poly1305
        val nonce = ByteArray(12) // Zero nonce for handshake
        val ciphertext = WireGuardCrypto.encryptChaCha20Poly1305(tempKey, nonce, plaintext, hash!!)
        
        // Update hash
        val newHash = mixHash(hash!!, ciphertext)
        
        return Pair(ciphertext, newHash)
    }
    
    private fun deriveTransportKeys() {
        if (remoteEphemeralPublic == null) {
            throw IllegalStateException("Remote ephemeral public key not available")
        }
        
        // Final DH
        val dh3 = WireGuardCrypto.performKeyExchange(localEphemeralPrivate!!, remoteEphemeralPublic!!)
        chainingKey = mixKey(chainingKey!!, dh3)
        
        // Derive sending and receiving keys
        val keyMaterial = WireGuardCrypto.hkdf(chainingKey!!, ByteArray(0), "".toByteArray(), 64)
        
        sendingKey = keyMaterial.sliceArray(0..31)
        receivingKey = keyMaterial.sliceArray(32..63)
        
        sendingCounter = 0
        receivingCounter = 0
        
        Log.d(TAG, "Transport keys derived")
    }
    
    private fun createTimestamp(): ByteArray {
        // Create 12-byte timestamp (simplified)
        val timestamp = System.currentTimeMillis() / 1000
        return ByteBuffer.allocate(12).putLong(4, timestamp).array()
    }
    
    private fun calculateMac1(data: ByteArray): ByteArray {
        // Simplified MAC1 calculation
        return WireGuardCrypto.hkdf(remotePublicKey, LABEL_MAC1, data, 16)
    }
    
    fun isEstablished(): Boolean = state == HandshakeState.ESTABLISHED
    fun getLocalIndex(): Int = localIndex
    fun getRemoteIndex(): Int = remoteIndex
}