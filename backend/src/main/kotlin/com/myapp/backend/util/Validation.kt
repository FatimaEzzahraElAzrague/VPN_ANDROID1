package com.myapp.backend.util

object Validation {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun isValidEmail(email: String) = emailRegex.matches(email)

    fun isStrongPassword(password: String): Boolean {
        return isCustomPasswordStrong(password)
    }
    
    private fun isCustomPasswordStrong(password: String): Boolean {
        // Custom configurable password strength rules
        val minLength = 6  // Configurable minimum length
        val requireUppercase = false  // Configurable: require uppercase
        val requireLowercase = true   // Configurable: require lowercase
        val requireDigit = true       // Configurable: require digit
        val requireSpecialChar = false // Configurable: require special character
        
        val hasUpperCase = if (requireUppercase) password.any { it.isUpperCase() } else true
        val hasLowerCase = if (requireLowercase) password.any { it.isLowerCase() } else true
        val hasDigit = if (requireDigit) password.any { it.isDigit() } else true
        val hasSpecialChar = if (requireSpecialChar) password.any { !it.isLetterOrDigit() } else true
        val isLongEnough = password.length >= minLength
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar && isLongEnough
    }
}


