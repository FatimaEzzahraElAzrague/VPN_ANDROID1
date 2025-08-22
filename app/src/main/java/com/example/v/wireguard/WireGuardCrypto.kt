package com.example.v.wireguard

import android.util.Log
import org.bouncycastle.crypto.engines.ChaCha7539Engine
import org.bouncycastle.crypto.macs.Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer
import java.security.SecureRandom
import kotlin.experimental.xor

/**
 * WireGuard cryptographic operations using BouncyCastle
 * Implements the core crypto primitives needed for WireGuard protocol
 */
object WireGuardCrypto {
    private const val TAG = "WireGuardCrypto"
    
    // WireGuard constants
    private const val KEY_SIZE = 32
    private const val NONCE_SIZE = 12
    private const val TAG_SIZE = 16
    private const val PUBLIC_KEY_SIZE = 32
    private const val PRIVATE_KEY_SIZE = 32
    
    // WireGuard labels for HKDF
    private val LABEL_MAC1 = "mac1----".toByteArray()
    private val LABEL_COOKIE = "cookie--".toByteArray()
    private val CONSTRUCTION = "Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray()
    private val IDENTIFIER = "WireGuard v1 zx2c4 Jason@zx2c4.com".toByteArray()
    
    /**
     * Generate a new X25519 private key
     */
    fun generatePrivateKey(): ByteArray {
        val random = SecureRandom()
        val privateKey = ByteArray(PRIVATE_KEY_SIZE)
        random.nextBytes(privateKey)
        
        // Clamp the private key as per X25519 spec
        privateKey[0] = (privateKey[0].toInt() and 248).toByte()
        privateKey[31] = (privateKey[31].toInt() and 127).toByte()
        privateKey[31] = (privateKey[31].toInt() or 64).toByte()
        
        return privateKey
    }
    
    /**
     * Derive public key from private key using X25519
     */
    fun getPublicKey(privateKey: ByteArray): ByteArray {
        val privateKeyParams = org.bouncycastle.crypto.params.X25519PrivateKeyParameters(privateKey)
        val publicKeyParams = privateKeyParams.generatePublicKey()
        return publicKeyParams.encoded
    }
    
    /**
     * Perform X25519 key exchange
     */
    fun performKeyExchange(privateKey: ByteArray, publicKey: ByteArray): ByteArray {
        val agreement = X25519Agreement()
        agreement.init(org.bouncycastle.crypto.params.X25519PrivateKeyParameters(privateKey))
        
        val sharedSecret = ByteArray(32)
        agreement.calculateAgreement(
            org.bouncycastle.crypto.params.X25519PublicKeyParameters(publicKey),
            sharedSecret,
            0
        )
        return sharedSecret
    }
    
    /**
     * HKDF key derivation for WireGuard
     */
    fun hkdf(inputKeyMaterial: ByteArray, salt: ByteArray, info: ByteArray, outputLength: Int): ByteArray {
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(inputKeyMaterial, salt, info))
        
        val output = ByteArray(outputLength)
        hkdf.generateBytes(output, 0, outputLength)
        
        return output
    }
    
    /**
     * ChaCha20-Poly1305 encryption
     */
    fun encryptChaCha20Poly1305(
        key: ByteArray,
        nonce: ByteArray,
        plaintext: ByteArray,
        additionalData: ByteArray = ByteArray(0)
    ): ByteArray {
        try {
            // ChaCha20 encryption
            val chacha = ChaCha7539Engine()
            chacha.init(true, ParametersWithIV(KeyParameter(key), nonce))
            
            val ciphertext = ByteArray(plaintext.size)
            chacha.processBytes(plaintext, 0, plaintext.size, ciphertext, 0)
            
            // Poly1305 authentication
            val poly1305 = Poly1305()
            val authKey = ByteArray(32)
            // Generate auth key by encrypting zeros
            val zeroKey = ByteArray(32)
            chacha.reset()
            chacha.init(true, ParametersWithIV(KeyParameter(key), nonce))
            chacha.processBytes(zeroKey, 0, 32, authKey, 0)
            
            poly1305.init(KeyParameter(authKey))
            
            // Authenticate additional data
            if (additionalData.isNotEmpty()) {
                poly1305.update(additionalData, 0, additionalData.size)
                val adPadding = 16 - (additionalData.size % 16)
                if (adPadding != 16) {
                    poly1305.update(ByteArray(adPadding), 0, adPadding)
                }
            }
            
            // Authenticate ciphertext
            poly1305.update(ciphertext, 0, ciphertext.size)
            val ctPadding = 16 - (ciphertext.size % 16)
            if (ctPadding != 16) {
                poly1305.update(ByteArray(ctPadding), 0, ctPadding)
            }
            
            // Add lengths
            val lengths = ByteBuffer.allocate(16)
                .putLong(additionalData.size.toLong())
                .putLong(ciphertext.size.toLong())
                .array()
            poly1305.update(lengths, 0, 16)
            
            val tag = ByteArray(TAG_SIZE)
            poly1305.doFinal(tag, 0)
            
            // Combine ciphertext + tag
            return ciphertext + tag
            
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}")
            throw e
        }
    }
    
    /**
     * ChaCha20-Poly1305 decryption
     */
    fun decryptChaCha20Poly1305(
        key: ByteArray,
        nonce: ByteArray,
        ciphertextWithTag: ByteArray,
        additionalData: ByteArray = ByteArray(0)
    ): ByteArray {
        try {
            if (ciphertextWithTag.size < TAG_SIZE) {
                throw IllegalArgumentException("Ciphertext too short")
            }
            
            val ciphertext = ciphertextWithTag.sliceArray(0 until ciphertextWithTag.size - TAG_SIZE)
            val receivedTag = ciphertextWithTag.sliceArray(ciphertextWithTag.size - TAG_SIZE until ciphertextWithTag.size)
            
            // Verify tag first
            val chacha = ChaCha7539Engine()
            chacha.init(true, ParametersWithIV(KeyParameter(key), nonce))
            
            val authKey = ByteArray(32)
            val zeroKey = ByteArray(32)
            chacha.processBytes(zeroKey, 0, 32, authKey, 0)
            
            val poly1305 = Poly1305()
            poly1305.init(KeyParameter(authKey))
            
            // Authenticate additional data
            if (additionalData.isNotEmpty()) {
                poly1305.update(additionalData, 0, additionalData.size)
                val adPadding = 16 - (additionalData.size % 16)
                if (adPadding != 16) {
                    poly1305.update(ByteArray(adPadding), 0, adPadding)
                }
            }
            
            // Authenticate ciphertext
            poly1305.update(ciphertext, 0, ciphertext.size)
            val ctPadding = 16 - (ciphertext.size % 16)
            if (ctPadding != 16) {
                poly1305.update(ByteArray(ctPadding), 0, ctPadding)
            }
            
            // Add lengths
            val lengths = ByteBuffer.allocate(16)
                .putLong(additionalData.size.toLong())
                .putLong(ciphertext.size.toLong())
                .array()
            poly1305.update(lengths, 0, 16)
            
            val computedTag = ByteArray(TAG_SIZE)
            poly1305.doFinal(computedTag, 0)
            
            // Constant-time tag comparison
            if (!computedTag.contentEquals(receivedTag)) {
                throw SecurityException("Authentication tag verification failed")
            }
            
            // Decrypt
            chacha.reset()
            chacha.init(false, ParametersWithIV(KeyParameter(key), nonce))
            
            val plaintext = ByteArray(ciphertext.size)
            chacha.processBytes(ciphertext, 0, ciphertext.size, plaintext, 0)
            
            return plaintext
            
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}")
            throw e
        }
    }
    
    /**
     * Generate a secure random nonce
     */
    fun generateNonce(): ByteArray {
        val nonce = ByteArray(NONCE_SIZE)
        SecureRandom().nextBytes(nonce)
        return nonce
    }
    
    /**
     * Convert Base64 key to bytes
     */
    fun base64ToKey(base64Key: String): ByteArray {
        return Base64.decode(base64Key.trim())
    }
    
    /**
     * Convert key bytes to Base64
     */
    fun keyToBase64(key: ByteArray): String {
        return Base64.toBase64String(key)
    }
}
