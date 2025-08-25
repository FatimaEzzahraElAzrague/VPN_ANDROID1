#!/usr/bin/env kotlin

import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.util.Base64

fun deriveWireGuardPublicKey(privateKeyBase64: String): String {
    try {
        val privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64)
        val privateKeyParams = X25519PrivateKeyParameters(privateKeyBytes, 0)
        val publicKeyParams = privateKeyParams.generatePublicKey()
        val publicKeyBytes = publicKeyParams.encoded
        return Base64.getEncoder().encodeToString(publicKeyBytes)
    } catch (e: Exception) {
        println("Error deriving public key: ${e.message}")
        return "ERROR"
    }
}

fun main() {
    val osakaPrivateKey = "eB9FEadcCaOM/DHDqJfGDfC8r1t4UZjUBxBtJdY2720="
    val parisPrivateKey = "MOHIWdozScbRm4C0V5W6u/7Z6/VY8l4DeeFnBSvW03I="
    
    println("=== WireGuard Key Derivation ===")
    println()
    println("Osaka Private Key: $osakaPrivateKey")
    println("Osaka Public Key: ${deriveWireGuardPublicKey(osakaPrivateKey)}")
    println()
    println("Paris Private Key: $parisPrivateKey")
    println("Paris Public Key: ${deriveWireGuardPublicKey(parisPrivateKey)}")
}
