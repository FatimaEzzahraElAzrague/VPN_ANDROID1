package com.example.v.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SubscriptionStatus(val displayName: String) {
    Free("Free"),
    Premium("Premium"),
    Trial("Trial")
}

data class User(
    val id: String,
    val email: String,
    val name: String
)

data class AccountUiState(
    val user: User? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Premium,
    val isSigningOut: Boolean = false,
    val error: String? = null
)

class AccountViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadAccountInfo()
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            try {
                // Simulate loading user data
                val user = User(
                    id = "user123",
                    email = "user@example.com",
                    name = "John Doe"
                )
                _uiState.value = _uiState.value.copy(user = user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSigningOut = true)
            try {
                // Simulate sign out process
                kotlinx.coroutines.delay(1000)
                // Handle sign out completion
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isSigningOut = false
                )
            }
        }
    }
}