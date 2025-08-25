#!/usr/bin/env kotlin

import java.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun derivePublicKey(privateKeyBase64: String): String {
    // This is a simplified key derivation - in production, use proper WireGuard key derivation
    val privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64)
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(privateKeyBytes)
    return Base64.getEncoder().encodeToString(hash)
}

fun main() {
    val osakaPrivateKey = "eB9FEadcCaOM/DHDqJfGDfC8r1t4UZjUBxBtJdY2720="
    val parisPrivateKey = "MOHIWdozScbRm4C0V5W6u/7Z6/VY8l4DeeFnBSvW03I="
    
    println("Osaka Private Key: $osakaPrivateKey")
    println("Osaka Public Key: ${derivePublicKey(osakaPrivateKey)}")
    println()
    println("Paris Private Key: $parisPrivateKey")
    println("Paris Public Key: ${derivePublicKey(parisPrivateKey)}")
}
