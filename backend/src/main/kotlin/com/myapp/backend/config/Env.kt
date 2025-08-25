package com.myapp.backend.config

object Env {
    // Database
    val databaseUrl: String = System.getenv("DATABASE_URL")
        ?: "postgresql://neondb_owner:npg_ZAqFk4UE8arp@ep-withered-wind-adhutu0i-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"

    // Redis
    val redisUrl: String? = System.getenv("REDIS_URL")

    // JWT
    val jwtSecret: String = System.getenv("JWT_SECRET")
        ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    val jwtIssuer: String = System.getenv("JWT_ISSUER") ?: "myapp.backend"
    val jwtAudience: String = System.getenv("JWT_AUDIENCE") ?: "myapp.client"
    val jwtExpSeconds: Long = System.getenv("JWT_EXP_SECONDS")?.toLongOrNull() ?: 3600L

    // Google OAuth
    val googleClientIdAndroid: String = System.getenv("GOOGLE_CLIENT_ID_ANDROID")
        ?: throw IllegalStateException("GOOGLE_CLIENT_ID_ANDROID environment variable is required")
    val googleClientIdWeb: String = System.getenv("GOOGLE_CLIENT_ID_WEB")
        ?: throw IllegalStateException("GOOGLE_CLIENT_ID_WEB environment variable is required")

    // SMTP Configuration
    val smtpHost: String = System.getenv("SMTP_HOST") ?: "smtp.gmail.com"
    val smtpPort: Int = System.getenv("SMTP_PORT")?.toIntOrNull() ?: 587
    val smtpUser: String = System.getenv("SMTP_USER")
        ?: throw IllegalStateException("SMTP_USER environment variable is required")
    val smtpPass: String = System.getenv("SMTP_PASS")
        ?: throw IllegalStateException("SMTP_PASS environment variable is required")
    val emailFrom: String = System.getenv("EMAIL_FROM") ?: "no-reply@myapp.com"

    // OTP Configuration
    val otpTtlSeconds: Long = System.getenv("OTP_TTL_SECONDS")?.toLongOrNull() ?: 600L
    val otpLength: Int = System.getenv("OTP_LENGTH")?.toIntOrNull() ?: 6
    val otpCooldownSeconds: Long = System.getenv("OTP_COOLDOWN_SECONDS")?.toLongOrNull() ?: 60L

    // VPN Configuration
    val osakaServerIp: String = System.getenv("OSAKA_SERVER_IP") ?: "56.155.92.31"
    val osakaServerPort: Int = System.getenv("OSAKA_SERVER_PORT")?.toIntOrNull() ?: 51820
    val osakaServerPrivateKey: String = System.getenv("OSAKA_SERVER_PRIVATE_KEY")
        ?: "eB9FEadcCaOM/DHDqJfGDfC8r1t4UZjUBxBtJdY2720="
    val osakaServerPublicKey: String = System.getenv("OSAKA_SERVER_PUBLIC_KEY")
        ?: "eB9FEadcCaOM/DHDqJfGDfC8r1t4UZjUBxBtJdY2720="
    val osakaServerSubnet: String = System.getenv("OSAKA_SERVER_SUBNET") ?: "10.77.25.0/24"

    val parisServerIp: String = System.getenv("PARIS_SERVER_IP") ?: "52.47.190.220"
    val parisServerPort: Int = System.getenv("PARIS_SERVER_PORT")?.toIntOrNull() ?: 51820
    val parisServerPrivateKey: String = System.getenv("PARIS_SERVER_PRIVATE_KEY")
        ?: "MOHIWdozScbRm4C0V5W6u/7Z6/VY8l4DeeFnBSvW03I="
    val parisServerPublicKey: String = System.getenv("PARIS_SERVER_PUBLIC_KEY")
        ?: "MOHIWdozScbRm4C0V5W6u/7Z6/VY8l4DeeFnBSvW03I="
    val parisServerSubnet: String = System.getenv("PARIS_SERVER_SUBNET") ?: "10.77.26.0/24"
}