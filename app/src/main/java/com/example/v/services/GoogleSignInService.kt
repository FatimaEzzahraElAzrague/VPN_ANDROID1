package com.example.v.services

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.example.v.data.ApiClient
import com.example.v.data.GoogleAuthRequest
import com.example.v.data.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleSignInService(private val context: Context) {
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("910763762703-bitot6ks5e9bvn98v7rutk6it6t7brp2.apps.googleusercontent.com") // Your Android client ID
            .requestEmail()
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }
    
    fun getSignInIntent() = googleSignInClient.signInIntent
    
    suspend fun handleSignInResult(data: android.content.Intent?): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                
                // Get the ID token
                val idToken = account.idToken
                if (idToken == null) {
                    throw Exception("Failed to get ID token from Google")
                }
                
                // Send ID token to backend
                val request = GoogleAuthRequest(idToken = idToken)
                val response = ApiClient.googleAuth(request)
                
                response
            } catch (e: ApiException) {
                throw Exception("Google sign-in failed: ${e.statusCode}")
            } catch (e: Exception) {
                throw Exception("Google sign-in failed: ${e.message}")
            }
        }
    }
    
    fun signOut() {
        googleSignInClient.signOut()
    }
    
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
