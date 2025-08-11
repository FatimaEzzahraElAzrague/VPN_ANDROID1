package com.example.v.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.v.components.GoogleSignInButton
import com.example.v.ui.theme.*
import com.example.v.data.ApiClient
import com.example.v.data.SignupRequest
import com.example.v.services.GoogleSignInService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSignUpSuccess: (String) -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit,
    onGoogleSignInRequest: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getGradientBackground(isDarkTheme))
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = getPrimaryTextColor(isDarkTheme)
            )
        }

        // Theme toggle button
        ThemeToggleButton(
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            TitleText(
                text = "Create Account",
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            SubtitleText(
                text = "Join SecureLine VPN today",
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = ""
                },
                label = { Text("Full Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeCrayola,
                    unfocusedBorderColor = getSecondaryTextColor(),
                    focusedLabelColor = OrangeCrayola,
                    unfocusedLabelColor = getSecondaryTextColor(),
                    cursorColor = OrangeCrayola
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeCrayola,
                    unfocusedBorderColor = getSecondaryTextColor(),
                    focusedLabelColor = OrangeCrayola,
                    unfocusedLabelColor = getSecondaryTextColor(),
                    cursorColor = OrangeCrayola
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeCrayola,
                    unfocusedBorderColor = getSecondaryTextColor(),
                    focusedLabelColor = OrangeCrayola,
                    unfocusedLabelColor = getSecondaryTextColor(),
                    cursorColor = OrangeCrayola
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = ""
                },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeCrayola,
                    unfocusedBorderColor = getSecondaryTextColor(),
                    focusedLabelColor = OrangeCrayola,
                    unfocusedLabelColor = getSecondaryTextColor(),
                    cursorColor = OrangeCrayola
                )
            )

            // Terms and conditions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptTerms,
                    onCheckedChange = { acceptTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = OrangeCrayola,
                        uncheckedColor = getSecondaryTextColor()
                    )
                )
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = getPrimaryTextColor(isDarkTheme),
                    modifier = Modifier.weight(1f)
                )
            }

            // Error message
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            PrimaryButton(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@PrimaryButton
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@PrimaryButton
                    }
                    if (!acceptTerms) {
                        errorMessage = "Please accept the terms and conditions"
                        return@PrimaryButton
                    }
                    isLoading = true
                    // Real API call
                    kotlinx.coroutines.GlobalScope.launch {
                        try {
                            val request = SignupRequest(
                                email = email,
                                password = password,
                                username = email.split("@")[0], // Use email prefix as username
                                fullName = name
                            )
                            val response = ApiClient.signup(request)
                            isLoading = false
                            onSignUpSuccess(email)
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Signup failed: ${e.message}"
                        }
                    }
                },
                text = "Create Account",
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = getSecondaryTextColor()
                )
                Text(
                    text = "  OR  ",
                    color = getSecondaryTextColor(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = getSecondaryTextColor()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign Up
            GoogleSignInButton(
                onClick = {
                    isLoading = true
                    onGoogleSignInRequest()
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in link
            Row {
                Text(
                    text = "Already have an account? ",
                    color = getSecondaryTextColor(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Sign In",
                    color = OrangeCrayola,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignInClick() }
                )
            }
        }
    }
}