package com.myapp.backend.services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.myapp.backend.config.Env

class GoogleAuthService {
    private val transport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    fun verifyIdToken(idTokenString: String): GoogleIdToken? {
        val clientIds = buildList {
            add(Env.googleClientIdAndroid)
            Env.googleClientIdWeb?.let { add(it) }
        }
        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(clientIds)
            .build()
        val idToken = verifier.verify(idTokenString) ?: return null
        val issuer = idToken.payload.issuer
        if (issuer != "accounts.google.com" && issuer != "https://accounts.google.com") return null
        return idToken
    }
}