package com.myapp.backend.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.myapp.backend.config.Env
import com.myapp.backend.config.JwtProvider
import com.myapp.backend.models.*
import com.myapp.backend.repositories.UserRepository
import com.myapp.backend.services.EmailService
import com.myapp.backend.services.GoogleAuthService
import com.myapp.backend.services.OtpService
import com.myapp.backend.util.Validation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.format.DateTimeFormatter
import mu.KotlinLogging
import de.mkammerer.argon2.Argon2Factory

private val logger = KotlinLogging.logger {}

fun Route.authRoutes() {
    val users = UserRepository()
    val otpService = OtpService()
    val emailService = EmailService()
    val googleService = GoogleAuthService()
    val dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    post("/signup") {
        val body = runCatching { call.receive<SignupRequest>() }.getOrElse {
            logger.warn { "❌ Invalid JSON in signup request" }
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_json")); return@post
        }
        
        logger.info { "📝 Signup attempt: Email=${body.email}, Username=${body.username}" }
        
        if (!Validation.isValidEmail(body.email)) { 
            logger.warn { "❌ Invalid email: ${body.email}" }
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_email")); return@post 
        }
        if (!Validation.isStrongPassword(body.password)) { 
            logger.warn { "❌ Weak password for: ${body.email}" }
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "weak_password")); return@post 
        }
        if (users.findByEmail(body.email) != null) { 
            logger.warn { "❌ Email already exists: ${body.email}" }
            call.respond(HttpStatusCode.Conflict, mapOf("error" to "email_exists")); return@post 
        }
        if (users.findByUsername(body.username) != null) { 
            logger.warn { "❌ Username already exists: ${body.username}" }
            call.respond(HttpStatusCode.Conflict, mapOf("error" to "username_exists")); return@post 
        }

        // Argon2id for email signup
        val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
        val hash = argon2.hash(2, 65536, 1, body.password.toCharArray())
        val userId = users.insertInactive(body.email, hash, body.username, body.full_name)

        val otp = try { otpService.generateAndStoreOtp(body.email) } catch (_: IllegalStateException) {
            logger.warn { "❌ OTP throttled for: ${body.email}" }
            call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "otp_throttled")); return@post
        }
        runCatching { emailService.sendOtpEmail(body.email, otp) }
            .onFailure { 
                logger.error { "❌ Failed to send email to: ${body.email}" }
                call.application.environment.log.error("Failed to send email") 
            }

        logger.info { "✅ Signup successful: UserID=$userId, Email=${body.email}" }
        call.respond(HttpStatusCode.Accepted, mapOf("message" to "OTP sent"))
    }

    post("/verify-otp") {
        val body = runCatching { call.receive<VerifyOtpRequest>() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_json")); return@post
        }
        if (otpService.isLocked(body.email)) { call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "too_many_attempts")); return@post }
        val ok = otpService.verifyOtp(body.email, body.otp)
        if (!ok) { call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_otp")); return@post }
        val activated = users.activateUser(body.email)
        if (!activated) { call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found")); return@post }
        val user = users.findByEmail(body.email)!!
        val token = JwtProvider.createAccessToken(user.id)
        call.respond(
            HttpStatusCode.OK,
            TokenResponse(access_token = token, expires_in = Env.jwtExpSeconds)
        )
    }

    post("/login") {
        val body = runCatching { call.receive<LoginRequest>() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_json")); return@post
        }
        val user = users.findByEmail(body.email)
        if (user == null || user.isDeleted) { call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_credentials")); return@post }
        if (!user.isActive) { call.respond(HttpStatusCode.Forbidden, mapOf("error" to "inactive_user")); return@post }

        // Try Argon2 first, then BCrypt (for Google or legacy)
        val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
        val argonOk = runCatching { argon2.verify(user.passwordHash, body.password.toCharArray()) }.getOrDefault(false)
        val bcryptOk = if (!argonOk) BCrypt.verifyer().verify(body.password.toCharArray(), user.passwordHash).verified else true

        if (!argonOk && !bcryptOk) { call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_credentials")); return@post }

        users.updateLastLogin(user.id)
        val token = JwtProvider.createAccessToken(user.id)
        val profile = ProfileResponse(
            id = user.id, email = user.email, username = user.username, full_name = user.fullName,
            created_at = user.createdAt.format(dtf), updated_at = user.updatedAt.format(dtf), last_login = user.lastLogin?.format(dtf)
        )
        call.respond(HttpStatusCode.OK, mapOf("token" to TokenResponse(token, expires_in = Env.jwtExpSeconds), "profile" to profile))
    }

    // Google route unchanged below...
    post("/google-auth") {
        val body = runCatching { call.receive<GoogleAuthRequest>() }.getOrElse {
            logger.warn { "❌ Invalid JSON in Google auth request" }
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_json")); return@post
        }
        
        logger.info { "🔐 Google auth attempt with ID token" }
        
        val idToken = googleService.verifyIdToken(body.id_token)
        if (idToken == null) { 
            logger.warn { "❌ Invalid Google ID token" }
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_id_token")); return@post 
        }
        
        val payload = idToken.payload
        val email = payload.email
        val name = payload["name"] as String?
        
        logger.info { "🔍 Checking for existing user: Email=$email, Name=$name" }
        
        var user = users.findByEmail(email)
        if (user == null) {
            logger.info { "🆕 Creating new user from Google auth: Email=$email" }
            
            val suggestedUsername = email.substringBefore('@')
            val base = suggestedUsername.take(20)
            var candidate = base
            var suffix = 1
            while (users.findByUsername(candidate) != null) {
                candidate = "$base$suffix"
                suffix++
            }
            
            val randomPasswordHash = BCrypt.withDefaults().hashToString(12, (payload.subject + System.nanoTime()).toCharArray())
            val id = users.insertInactive(email, randomPasswordHash, candidate, name)
            // Don't activate user automatically - require email verification
            user = users.findById(id)
            
            logger.info { "✅ New user created from Google (inactive): ID=$id, Email=$email, Username=$candidate" }
            call.respond(HttpStatusCode.Created, mapOf(
                "message" to "Account created successfully. Please verify your email to activate your account.",
                "email" to email,
                "requires_verification" to true
            ))
            return@post
        } else {
            logger.info { "✅ Existing user found: ID=${user.id}, Email=$email" }
        }
        
        if (user == null) { 
            logger.error { "❌ Failed to create/find user: Email=$email" }
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "user_create_failed")); return@post 
        }
        if (user.isDeleted) { 
            logger.warn { "❌ Deleted user trying to login: Email=$email" }
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "user_deleted")); return@post 
        }
        
        // For existing users, check if they're active
        if (!user.isActive) {
            logger.warn { "❌ Inactive user trying to login: Email=$email" }
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "inactive_user")); return@post
        }
        
        val token = JwtProvider.createAccessToken(user.id)
        logger.info { "✅ Google auth successful: UserID=${user.id}, Email=$email" }
        call.respond(HttpStatusCode.OK, TokenResponse(access_token = token, expires_in = Env.jwtExpSeconds))
    }

    // Debug endpoint to view all users (remove in production)
    get("/debug/users") {
        val allUsers = users.getAllUsers()
        logger.info { "📊 Debug: Found ${allUsers.size} users in database" }
        call.respond(HttpStatusCode.OK, mapOf("users" to allUsers))
    }
}


