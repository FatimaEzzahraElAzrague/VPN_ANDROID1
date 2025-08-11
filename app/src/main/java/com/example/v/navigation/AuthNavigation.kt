package com.example.v.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.v.screens.EmailActivationScreen
import com.example.v.screens.SignInScreen
import com.example.v.screens.SignUpScreen
import com.example.v.screens.WelcomeScreen

@Composable
fun AuthNavigation(
    onAuthSuccess: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onGoogleSignInRequest: () -> Unit = {},
    exposeNavigateToSignIn: ((() -> Unit) -> Unit) = {}
) {
    val navController = rememberNavController()

    // Expose a function that navigates to the sign-in screen
    LaunchedEffect(Unit) {
        exposeNavigateToSignIn {
            navController.navigate("signin")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onSignInClick = { navController.navigate("signin") },
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("signin") {
            SignInScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onSignInSuccess = onAuthSuccess,
                onSignUpClick = { navController.navigate("signup") },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("signup") {
            SignUpScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onSignUpSuccess = { email ->
                    navController.navigate("email_activation/$email")
                },
                onSignInClick = { navController.navigate("signin") },
                onBackClick = { navController.popBackStack() },
                onGoogleSignInRequest = onGoogleSignInRequest
            )
        }
        composable("email_activation/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailActivationScreen(
                email = email,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onActivationSuccess = onAuthSuccess,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}