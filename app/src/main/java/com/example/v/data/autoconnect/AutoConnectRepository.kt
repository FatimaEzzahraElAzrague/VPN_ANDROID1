package com.example.v.data.autoconnect

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoConnectRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auto_connect_prefs", 
        Context.MODE_PRIVATE
    )
    
    private val _settingsFlow = MutableStateFlow(get())
    
    companion object {
        private const val KEY_ENABLED = "auto_connect_enabled"
        private const val KEY_MODE = "auto_connect_mode"
    }
    
    fun get(): AutoConnectSettings {
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        val modeOrdinal = prefs.getInt(KEY_MODE, AutoConnectMode.ANY_WIFI_OR_CELLULAR.ordinal)
        val mode = AutoConnectMode.values().getOrNull(modeOrdinal) 
            ?: AutoConnectMode.ANY_WIFI_OR_CELLULAR
            
        return AutoConnectSettings(enabled, mode)
    }
    
    fun set(enabled: Boolean, mode: AutoConnectMode) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_MODE, mode.ordinal)
            .apply()
            
        _settingsFlow.value = AutoConnectSettings(enabled, mode)
    }
    
    fun isAutoConnectEnabled(): Flow<Boolean> {
        return kotlinx.coroutines.flow.flow {
            emit(get().enabled)
        }
    }
    
    fun getAutoConnectMode(): Flow<AutoConnectMode> {
        return kotlinx.coroutines.flow.flow {
            emit(get().mode)
        }
    }
    
    fun getSettingsFlow(): Flow<AutoConnectSettings> = _settingsFlow.asStateFlow()
}
