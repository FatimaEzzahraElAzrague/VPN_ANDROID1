package com.example.v.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SecurityUiState(
    val adBlockEnabled: Boolean = true,
    val malwareBlockEnabled: Boolean = true,
    val familyModeEnabled: Boolean = false,
    val dnsLeakProtectionEnabled: Boolean = true,
    val killSwitchEnabled: Boolean = false,
    val error: String? = null
)

class SecurityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    fun toggleAdBlock(enabled: Boolean) {
        updateSetting { it.copy(adBlockEnabled = enabled) }
    }

    fun toggleMalwareBlock(enabled: Boolean) {
        updateSetting { it.copy(malwareBlockEnabled = enabled) }
    }

    fun toggleFamilyMode(enabled: Boolean) {
        updateSetting { it.copy(familyModeEnabled = enabled) }
    }

    fun toggleDnsLeakProtection(enabled: Boolean) {
        updateSetting { it.copy(dnsLeakProtectionEnabled = enabled) }
    }

    fun toggleKillSwitch(enabled: Boolean) {
        updateSetting { it.copy(killSwitchEnabled = enabled) }
    }

    private fun updateSetting(update: (SecurityUiState) -> SecurityUiState) {
        viewModelScope.launch {
            try {
                _uiState.value = update(_uiState.value)
                // Simulate API call to save settings
                kotlinx.coroutines.delay(100)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}