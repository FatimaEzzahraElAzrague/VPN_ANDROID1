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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailActivationScreen(
    email: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onActivationSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var activationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isResending by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableStateOf(0) }

    // Resend cooldown timer
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }

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
            // Email icon
            Card(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = OrangeCrayola.copy(alpha = 0.9f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(64.dp),
                        tint = LightWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            TitleText(
                text = "Verify Your Email",
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "We've sent a verification code to",
                style = MaterialTheme.typography.titleMedium,
                color = getSecondaryTextColor(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium,
                color = OrangeCrayola,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Activation code field
            OutlinedTextField(
                value = activationCode,
                onValueChange = {
                    if (it.length <= 6) {
                        activationCode = it
                        errorMessage = ""
                    }
                },
                label = { Text("Enter 6-digit code") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Verify Button
            PrimaryButton(
                onClick = {
                    if (activationCode.length != 6) {
                        errorMessage = "Please enter a 6-digit code"
                        return@PrimaryButton
                    }
                    isLoading = true
                    // Simulate API call
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(2000)
                        isLoading = false
                        onActivationSuccess()
                    }
                },
                text = "Verify Email",
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resend code section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Didn't receive the code?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getSecondaryTextColor(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (resendCooldown > 0) {
                    Text(
                        text = "Resend in ${resendCooldown}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSecondaryTextColor(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Resend Code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrangeCrayola,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable {
                            if (!isResending) {
                                isResending = true
                                resendCooldown = 60
                                // Simulate resend
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    isResending = false
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Help text
            Text(
                text = "Check your spam folder if you don't see the email",
                style = MaterialTheme.typography.bodySmall,
                color = getSecondaryTextColor(),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}