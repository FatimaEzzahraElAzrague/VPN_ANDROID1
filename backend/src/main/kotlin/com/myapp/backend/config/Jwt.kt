package com.myapp.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtProvider {
    private val algorithm by lazy { Algorithm.HMAC256(Env.jwtSecret) }

    fun createAccessToken(userId: Int): String {
        val now = System.currentTimeMillis()
        val exp = Date(now + Env.jwtExpSeconds * 1000)
        return JWT.create()
            .withIssuer(Env.jwtIssuer)
            .withAudience(Env.jwtAudience)
            .withSubject(userId.toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(exp)
            .sign(algorithm)
    }
    
    fun getUserIdFromToken(token: String): Int {
        val verifier = JWT.require(algorithm)
            .withIssuer(Env.jwtIssuer)
            .withAudience(Env.jwtAudience)
            .build()
        
        val decodedJWT = verifier.verify(token)
        return decodedJWT.subject.toInt()
    }

    fun algorithm(): Algorithm = algorithm
}

fun Application.jwtConfig() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(JwtProvider.algorithm())
                    .withIssuer(Env.jwtIssuer)
                    .withAudience(Env.jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.subject?.isNotBlank() == true) JWTPrincipal(credential.payload) else null
            }
        }
    }
}


