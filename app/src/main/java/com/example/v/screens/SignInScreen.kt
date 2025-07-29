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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSignInSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                text = "Welcome Back",
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            SubtitleText(
                text = "Sign in to your account",
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(48.dp))

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

            // Forgot password
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = OrangeCrayola,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { /* Handle forgot password */ }
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

            // Sign In Button
            PrimaryButton(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@PrimaryButton
                    }
                    isLoading = true
                    // Simulate API call
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(2000)
                        isLoading = false
                        onSignInSuccess()
                    }
                },
                text = "Sign In",
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

            // Google Sign In
            GoogleSignInButton(
                onClick = {
                    isLoading = true
                    // Simulate Google sign in
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(2000)
                        isLoading = false
                        onSignInSuccess()
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign up link
            Row {
                Text(
                    text = "Don't have an account? ",
                    color = getSecondaryTextColor(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Sign Up",
                    color = OrangeCrayola,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}