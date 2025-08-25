// app/src/main/java/com/example/v/screens/WelcomeScreen.kt
package com.example.v.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.media3.common.util.UnstableApi
import com.example.v.components.VideoPlayer
import com.example.v.ui.theme.*
import com.example.v.auth.AuthManager

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val context = LocalContext.current
    val videoUri = remember { Uri.parse("android.resource://${context.packageName}/raw/background_video") }
    
    // Check if user is already logged in
    LaunchedEffect(Unit) {
        if (AuthManager.isLoggedIn()) {
            // User is already logged in, proceed to main app
            onSignInClick() // This will navigate to the main app
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            uri = videoUri.toString()
        )

        // Dark overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
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
                        painter = painterResource(id = com.example.v.R.drawable.ic_logo),
                        contentDescription = "VPN Logo",
                        modifier = Modifier.size(64.dp),
                        tint = LightWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Title
            Text(
                text = "Ghostshield VPN",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = LightWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Secure, Fast & Private\nInternet Connection",
                style = MaterialTheme.typography.titleMedium,
                color = LightWhite.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(130.dp))

            // Sign Up Button
            PrimaryButton(
                onClick = onSignUpClick,
                text = "Get Started"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In Button
            SecondaryButton(
                onClick = onSignInClick,
                text = "I Already Have an Account"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Terms and Privacy
            Text(
                text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = LightWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 16.dp),
            color = LightWhite
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = LightWhite.copy(alpha = 0.9f)
        )
    }
}