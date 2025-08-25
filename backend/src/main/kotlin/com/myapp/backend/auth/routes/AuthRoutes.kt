package com.myapp.backend.auth.routes

import com.myapp.backend.auth.services.GoogleAuthService
import com.myapp.backend.auth.services.RedisService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.random.Random

fun Route.authRoutes(
    googleAuthService: GoogleAuthService,
    redisService: RedisService
) {
    route("/auth") {
        // Google OAuth sign in
        post("/google") {
            try {
                val request = call.receive<GoogleSignInRequest>()
                val userInfo = googleAuthService.verifyAndGetUserInfo(
                    request.idToken,
                    request.isAndroid
                )
                val user = googleAuthService.findOrCreateUser(userInfo)
                
                call.respond(HttpStatusCode.OK, AuthResponse(
                    user = user.toDTO(),
                    message = "Successfully authenticated with Google"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to (e.message ?: "Failed to authenticate with Google")
                ))
            }
        }

        // Request email verification code
        post("/email/request-code") {
            try {
                val request = call.receive<RequestVerificationCodeRequest>()
                val code = generateVerificationCode()
                
                redisService.setVerificationCode(
                    request.email,
                    code,
                    RedisService.DEFAULT_TTL_SECONDS
                )
                
                // TODO: Implement actual email sending
                // For now, just return the code in development
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Verification code sent",
                    "code" to code // Remove this in production
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to (e.message ?: "Failed to send verification code")
                ))
            }
        }

        // Verify email code
        post("/email/verify") {
            try {
                val request = call.receive<VerifyCodeRequest>()
                val storedCode = redisService.getVerificationCode(request.email)
                
                if (storedCode == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "Verification code expired or not found"
                    ))
                    return@post
                }
                
                if (storedCode != request.code) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "Invalid verification code"
                    ))
                    return@post
                }
                
                // Code is valid, delete it
                redisService.deleteVerificationCode(request.email)
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Email verified successfully"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to (e.message ?: "Failed to verify code")
                ))
            }
        }
    }
}

private fun generateVerificationCode(): String {
    return Random.nextInt(100000, 999999).toString()
}

@Serializable
data class GoogleSignInRequest(
    val idToken: String,
    val isAndroid: Boolean = false
)

@Serializable
data class RequestVerificationCodeRequest(
    val email: String
)

@Serializable
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

@Serializable
data class AuthResponse(
    val user: UserDTO,
    val message: String
)
