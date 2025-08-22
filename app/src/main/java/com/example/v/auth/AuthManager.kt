package com.example.v.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.v.data.UserResponse
import com.google.gson.Gson

object AuthManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_DATA = "user_data"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun storeToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
    
    fun storeUser(user: UserResponse) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putString(KEY_USER_DATA, userJson)
            .apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getUser(): UserResponse? {
        val userJson = prefs.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, UserResponse::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null
    }
    
    fun logout() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_DATA)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    fun clearUserData() {
        prefs.edit()
            .remove(KEY_USER_DATA)
            .apply()
    }
}
