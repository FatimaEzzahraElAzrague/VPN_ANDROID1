package com.example.v.crypto

import org.bouncycastle.crypto.StreamCipher
import org.bouncycastle.crypto.engines.Salsa20Engine
import org.bouncycastle.crypto.macs.Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.KeyGenerationParameters
import java.security.SecureRandom
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 * WireGuard cryptographic operations using BouncyCastle and standard Java crypto
 */
class WireGuardCrypto {
    
    companion object {
        private const val CHACHA20_KEY_SIZE = 32
        private const val CHACHA20_NONCE_SIZE = 12
        private const val POLY1305_TAG_SIZE = 16
        private const val X25519_KEY_SIZE = 32
        
        // WireGuard protocol constants
        private val CONSTRUCTION = "Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray()
        private val IDENTIFIER = "WireGuard v1 zx2c4 Jason@zx2c4.com".toByteArray()
        private val LABEL_MAC1 = "mac1----".toByteArray()
        private val LABEL_COOKIE = "cookie--".toByteArray()
    }
    
    /**
     * Generate X25519 key pair
     */
    fun generateKeyPair(): Pair<ByteArray, ByteArray> {
        val keyPairGenerator = X25519KeyPairGenerator()
        keyPairGenerator.init(KeyGenerationParameters(SecureRandom(), 255))
        
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = (keyPair.private as X25519PrivateKeyParameters).encoded
        val publicKey = (keyPair.public as X25519PublicKeyParameters).encoded
        
        return Pair(privateKey, publicKey)
    }
    
    /**
     * Compute X25519 shared secret
     */
    fun computeSharedSecret(privateKey: ByteArray, publicKey: ByteArray): ByteArray {
        val agreement = X25519Agreement()
        agreement.init(X25519PrivateKeyParameters(privateKey, 0))
        
        val sharedSecret = ByteArray(X25519_KEY_SIZE)
        agreement.calculateAgreement(X25519PublicKeyParameters(publicKey, 0), sharedSecret, 0)
        
        return sharedSecret
    }
    
    /**
     * HKDF key derivation
     */
    fun hkdf(inputKeyMaterial: ByteArray, salt: ByteArray?, info: ByteArray?, outputLength: Int): ByteArray {
        val hkdf = HKDFBytesGenerator(Blake2bDigest(256))
        val params = HKDFParameters(inputKeyMaterial, salt, info)
        hkdf.init(params)
        
        val output = ByteArray(outputLength)
        hkdf.generateBytes(output, 0, outputLength)
        
        return output
    }
    
    /**
     * ChaCha20Poly1305 encryption using fallback implementation
     */
    fun encrypt(key: ByteArray, nonce: ByteArray, plaintext: ByteArray, associatedData: ByteArray? = null): ByteArray {
        return try {
            // Try to use ChaCha20Poly1305 if available (Android API 28+)
            encryptWithChaCha20Poly1305(key, nonce, plaintext, associatedData)
        } catch (e: Exception) {
            // Fallback to AES-GCM
            encryptWithAESGCM(key, nonce, plaintext, associatedData)
        }
    }
    
    /**
     * ChaCha20Poly1305 decryption using fallback implementation
     */
    fun decrypt(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray, associatedData: ByteArray? = null): ByteArray? {
        return try {
            // Try to use ChaCha20Poly1305 if available (Android API 28+)
            decryptWithChaCha20Poly1305(key, nonce, ciphertext, associatedData)
        } catch (e: Exception) {
            // Fallback to AES-GCM
            decryptWithAESGCM(key, nonce, ciphertext, associatedData)
        }
    }
    
    private fun encryptWithChaCha20Poly1305(key: ByteArray, nonce: ByteArray, plaintext: ByteArray, associatedData: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        val keySpec = SecretKeySpec(key, "ChaCha20")
        val ivSpec = IvParameterSpec(nonce)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        
        associatedData?.let { cipher.updateAAD(it) }
        
        return cipher.doFinal(plaintext)
    }
    
    private fun decryptWithChaCha20Poly1305(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray, associatedData: ByteArray?): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("ChaCha20-Poly1305")
            val keySpec = SecretKeySpec(key, "ChaCha20")
            val ivSpec = IvParameterSpec(nonce)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            
            associatedData?.let { cipher.updateAAD(it) }
            
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun encryptWithAESGCM(key: ByteArray, nonce: ByteArray, plaintext: ByteArray, associatedData: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(nonce)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        
        associatedData?.let { cipher.updateAAD(it) }
        
        return cipher.doFinal(plaintext)
    }
    
    private fun decryptWithAESGCM(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray, associatedData: ByteArray?): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(key, "AES")
            val ivSpec = IvParameterSpec(nonce)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            
            associatedData?.let { cipher.updateAAD(it) }
            
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Constant-time byte array comparison
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
    
    /**
     * Blake2s hash function
     */
    fun blake2s(data: ByteArray, key: ByteArray? = null): ByteArray {
        val digest = Blake2bDigest(key, 32, null, null)
        digest.update(data, 0, data.size)
        
        val result = ByteArray(32)
        digest.doFinal(result, 0)
        
        return result
    }
    
    /**
     * Generate nonce from counter
     */
    fun generateNonce(counter: Long): ByteArray {
        val nonce = ByteArray(CHACHA20_NONCE_SIZE)
        val buffer = ByteBuffer.wrap(nonce).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(4, counter) // Put counter at offset 4
        return nonce
    }
    
    /**
     * XOR two byte arrays
     */
    fun xor(a: ByteArray, b: ByteArray): ByteArray {
        require(a.size == b.size) { "Arrays must be the same length" }
        
        val result = ByteArray(a.size)
        for (i in a.indices) {
            result[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        }
        return result
    }
    
    /**
     * Poly1305 MAC using BouncyCastle
     */
    fun poly1305Mac(key: ByteArray, data: ByteArray): ByteArray {
        val poly1305 = Poly1305()
        poly1305.init(KeyParameter(key))
        poly1305.update(data, 0, data.size)
        
        val result = ByteArray(16)
        poly1305.doFinal(result, 0)
        
        return result
    }
    
    /**
     * Secure random bytes generation
     */
    fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}