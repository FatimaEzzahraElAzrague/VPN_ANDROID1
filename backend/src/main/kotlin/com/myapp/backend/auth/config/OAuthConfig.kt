package com.myapp.backend.auth.config

import com.myapp.backend.config.Env
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object OAuthConfig {
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    val googleConfig = GoogleOAuthConfig(
        clientIdWeb = Env.googleClientIdWeb,
        clientIdAndroid = Env.googleClientIdAndroid
    )
}

data class GoogleOAuthConfig(
    val clientIdWeb: String,
    val clientIdAndroid: String
) {
    companion object {
        const val GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo"
        const val GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
    }
}
