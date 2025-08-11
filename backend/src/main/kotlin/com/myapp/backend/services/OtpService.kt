package com.myapp.backend.services

import com.myapp.backend.config.Env
import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.SecureRandom

class OtpService {
    private val random = SecureRandom()

    private fun otpKey(email: String) = "otp:signup:$email"
    private fun attemptsKey(email: String) = "otp:signup:attempts:$email"
    private fun lockKey(email: String) = "otp:signup:lock:$email"
    private fun throttleKey(email: String) = "otp:signup:throttle:$email"

    fun generateAndStoreOtp(email: String): String {
        // throttle OTP generation
        if (RedisClient.client.exists(throttleKey(email))) {
            throw IllegalStateException("throttled")
        }
        val code = buildString {
            repeat(Env.otpLength) { append(random.nextInt(10)) }
        }
        val key = otpKey(email)
        // Hash the OTP with BCrypt before storing
        val hashedOtp = BCrypt.withDefaults().hashToString(12, code.toCharArray())
        RedisClient.client.setex(key, Env.otpTtlSeconds, hashedOtp)
        // reset attempts when generating new OTP
        RedisClient.client.del(attemptsKey(email))
        RedisClient.client.del(lockKey(email))
        RedisClient.client.setex(throttleKey(email), Env.otpCooldownSeconds, "1")
        return code
    }

    fun isLocked(email: String): Boolean = RedisClient.client.exists(lockKey(email))

    fun verifyOtp(email: String, otp: String): Boolean {
        if (isLocked(email)) return false
        val key = otpKey(email)
        val storedHash = RedisClient.client.get(key) ?: return false.also { registerFailedAttempt(email) }
        
        // Verify the OTP using BCrypt
        return try {
            val result = BCrypt.verifyer().verify(otp.toCharArray(), storedHash)
            if (result.verified) {
                RedisClient.client.del(key)
                RedisClient.client.del(attemptsKey(email))
                RedisClient.client.del(lockKey(email))
                true
            } else {
                registerFailedAttempt(email)
                false
            }
        } catch (e: Exception) {
            registerFailedAttempt(email)
            false
        }
    }

    private fun registerFailedAttempt(email: String) {
        val aKey = attemptsKey(email)
        val attempts = RedisClient.client.incr(aKey)
        if (attempts == 1L) {
            RedisClient.client.expire(aKey, Env.otpTtlSeconds)
        }
        if (attempts >= 5) {
            RedisClient.client.setex(lockKey(email), Env.otpTtlSeconds, "1")
        }
    }
}


