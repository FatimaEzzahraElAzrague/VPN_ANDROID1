package com.myapp.backend.config

import io.github.cdimascio.dotenv.Dotenv

object Env {
    private var dotenv: Dotenv? = null

    val databaseUrl: String get() = getRequired("DATABASE_URL")
    val redisUrl: String get() = getRequired("REDIS_URL")
    val jwtSecret: String get() = getRequired("JWT_SECRET")
    val jwtIssuer: String get() = getRequired("JWT_ISSUER")
    val jwtAudience: String get() = getRequired("JWT_AUDIENCE")
    val jwtExpSeconds: Long get() = getRequired("JWT_EXP_SECONDS").toLong()
    val googleClientIdAndroid: String get() = getRequired("GOOGLE_CLIENT_ID_ANDROID")
    val googleClientIdWeb: String? get() = getOptional("GOOGLE_CLIENT_ID_WEB")
    val smtpHost: String get() = getRequired("SMTP_HOST")
    val smtpPort: Int get() = getRequired("SMTP_PORT").toInt()
    val smtpUser: String get() = getRequired("SMTP_USER")
    val smtpPass: String get() = getRequired("SMTP_PASS")
    val emailFrom: String get() = getRequired("EMAIL_FROM")
    val otpTtlSeconds: Long get() = getOptional("OTP_TTL_SECONDS")?.toLong() ?: 600L
    val otpLength: Int get() = getOptional("OTP_LENGTH")?.toInt() ?: 6
    val otpCooldownSeconds: Long get() = getOptional("OTP_COOLDOWN_SECONDS")?.toLong() ?: 60
    val appBaseUrl: String get() = getRequired("APP_BASE_URL")
    val port: Int get() = getOptional("PORT")?.toInt() ?: 8080
    val agentToken: String get() = getRequired("AGENT_TOKEN")

    fun load() {
        if (dotenv != null) return
        dotenv = try {
            Dotenv.configure().ignoreIfMissing().load()
        } catch (e: Exception) {
            null
        }
    }

    private fun getRequired(key: String): String =
        System.getenv(key) ?: dotenv?.get(key) ?: error("Missing env var: $key")

    private fun getOptional(key: String): String? =
        System.getenv(key) ?: dotenv?.get(key)
}


