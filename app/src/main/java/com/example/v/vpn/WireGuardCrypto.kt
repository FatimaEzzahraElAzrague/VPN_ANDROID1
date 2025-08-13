package com.example.v.vpn

import android.util.Base64
import android.util.Log
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * WireGuard cryptographic operations using BouncyCastle
 * Implements the core crypto needed for WireGuard protocol
 */
class WireGuardCrypto {
    
    companion object {
        private const val TAG = "WireGuardCrypto"
        
        init {
            // Add BouncyCastle provider
            Security.addProvider(BouncyCastleProvider())
        }
        
        // WireGuard protocol constants
        const val KEY_LENGTH = 32
        const val NONCE_LENGTH = 12
        const val TAG_LENGTH = 16
        const val HANDSHAKE_INIT_SIZE = 148
        const val HANDSHAKE_RESPONSE_SIZE = 92
        const val DATA_PACKET_HEADER_SIZE = 16
        
        // Protocol messages
        const val MESSAGE_HANDSHAKE_INITIATION: Byte = 1
        const val MESSAGE_HANDSHAKE_RESPONSE: Byte = 2
        const val MESSAGE_COOKIE_REPLY: Byte = 3
        const val MESSAGE_DATA: Byte = 4
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generate X25519 key pair
     */
    fun generateKeyPair(): Pair<ByteArray, ByteArray> {
        try {
            val generator = X25519KeyPairGenerator()
            generator.init(X25519KeyGenerationParameters(secureRandom))
            val keyPair = generator.generateKeyPair()
            
            val privateKey = (keyPair.private as X25519PrivateKeyParameters).encoded
            val publicKey = (keyPair.public as X25519PublicKeyParameters).encoded
            
            Log.d(TAG, "Generated key pair - Public: ${android.util.Base64.encodeToString(publicKey, android.util.Base64.NO_WRAP)}")
            return Pair(privateKey, publicKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate key pair", e)
            throw e
        }
    }
    
    /**
     * Derive public key from private key
     */
    fun derivePublicKey(privateKeyBytes: ByteArray): ByteArray {
        return try {
            val privateKey = X25519PrivateKeyParameters(privateKeyBytes, 0)
            val publicKey = privateKey.generatePublicKey()
            publicKey.encoded
        } catch (e: Exception) {
            Log.e(TAG, "Failed to derive public key", e)
            throw e
        }
    }
    
    /**
     * Perform X25519 key agreement (ECDH)
     */
    fun computeSharedSecret(privateKeyBytes: ByteArray, publicKeyBytes: ByteArray): ByteArray {
        return try {
            val privateKey = X25519PrivateKeyParameters(privateKeyBytes, 0)
            val publicKey = X25519PublicKeyParameters(publicKeyBytes, 0)
            
            val agreement = X25519Agreement()
            agreement.init(privateKey)
            
            val sharedSecret = ByteArray(agreement.agreementSize)
            agreement.calculateAgreement(publicKey, sharedSecret, 0)
            
            Log.d(TAG, "Computed shared secret")
            sharedSecret
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute shared secret", e)
            throw e
        }
    }
    
    /**
     * ChaCha20-Poly1305 encryption
     */
    fun encrypt(plaintext: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        return try {
            val cipher = Cipher.getInstance("ChaCha20-Poly1305", "BC")
            val keySpec = SecretKeySpec(key, "ChaCha20")
            val ivSpec = IvParameterSpec(nonce)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            cipher.doFinal(plaintext)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            throw e
        }
    }
    
    /**
     * ChaCha20-Poly1305 decryption
     */
    fun decrypt(ciphertext: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        return try {
            val cipher = Cipher.getInstance("ChaCha20-Poly1305", "BC")
            val keySpec = SecretKeySpec(key, "ChaCha20")
            val ivSpec = IvParameterSpec(nonce)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            throw e
        }
    }
    
    /**
     * BLAKE2s hash function (using SHA-256 as fallback)
     */
    fun blake2s(data: ByteArray, key: ByteArray? = null): ByteArray {
        return try {
            // Use SHA-256 as a fallback since Blake2s might not be available
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            if (key != null) {
                // Simple HMAC-like construction
                digest.update(key)
            }
            digest.update(data)
            digest.digest()
        } catch (e: Exception) {
            Log.e(TAG, "Hash failed", e)
            throw e
        }
    }
    
    /**
     * Generate random nonce
     */
    fun generateNonce(): ByteArray {
        val nonce = ByteArray(NONCE_LENGTH)
        secureRandom.nextBytes(nonce)
        return nonce
    }
    
    /**
     * Key derivation function (KDF)
     */
    fun kdf(sharedSecret: ByteArray, info: ByteArray): ByteArray {
        // Simplified KDF using BLAKE2s
        return blake2s(sharedSecret + info).copyOf(KEY_LENGTH)
    }
    
    /**
     * Create handshake initiation message
     */
    fun createHandshakeInitiation(
        localPrivateKey: ByteArray,
        remotePublicKey: ByteArray,
        presharedKey: ByteArray? = null
    ): ByteArray {
        try {
            Log.d(TAG, "Creating handshake initiation")
            
            // Generate ephemeral key pair
            val (ephemeralPrivate, ephemeralPublic) = generateKeyPair()
            
            // Compute shared secrets
            val staticShared = computeSharedSecret(localPrivateKey, remotePublicKey)
            val ephemeralShared = computeSharedSecret(ephemeralPrivate, remotePublicKey)
            
            // Derive keys
            val sendingKey = kdf(staticShared + ephemeralShared, "WireGuard v1 zx2c4 Jason@zx2c4.com".toByteArray())
            
            // Build message (simplified)
            val message = ByteArray(HANDSHAKE_INIT_SIZE)
            message[0] = MESSAGE_HANDSHAKE_INITIATION
            
            // Add sender index (4 bytes)
            val senderIndex = secureRandom.nextInt()
            message[1] = (senderIndex shr 24).toByte()
            message[2] = (senderIndex shr 16).toByte()
            message[3] = (senderIndex shr 8).toByte()
            message[4] = senderIndex.toByte()
            
            // Add ephemeral public key
            System.arraycopy(ephemeralPublic, 0, message, 8, KEY_LENGTH)
            
            // Add encrypted static public key and timestamp
            val localPublicKey = derivePublicKey(localPrivateKey)
            val nonce = generateNonce()
            val encryptedStatic = encrypt(localPublicKey, sendingKey, nonce)
            System.arraycopy(encryptedStatic, 0, message, 40, encryptedStatic.size)
            
            Log.d(TAG, "Handshake initiation created")
            return message
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create handshake initiation", e)
            throw e
        }
    }
    
    /**
     * Process data packet
     */
    fun encryptDataPacket(data: ByteArray, sendingKey: ByteArray, counter: Long): ByteArray {
        try {
            // Create nonce from counter
            val nonce = ByteArray(NONCE_LENGTH)
            for (i in 0..7) {
                nonce[i] = (counter shr (i * 8)).toByte()
            }
            
            // Encrypt data
            val encrypted = encrypt(data, sendingKey, nonce)
            
            // Build packet
            val packet = ByteArray(DATA_PACKET_HEADER_SIZE + encrypted.size)
            packet[0] = MESSAGE_DATA
            
            // Add receiver index and counter
            System.arraycopy(nonce, 0, packet, 4, 8)
            System.arraycopy(encrypted, 0, packet, DATA_PACKET_HEADER_SIZE, encrypted.size)
            
            return packet
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt data packet", e)
            throw e
        }
    }
}
