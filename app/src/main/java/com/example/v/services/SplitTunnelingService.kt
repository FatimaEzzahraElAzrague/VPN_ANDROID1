package com.example.v.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.v.vpn.SplitTunnelingConfig
import com.example.v.vpn.SplitTunnelingMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Split Tunneling Service
 * Manages split tunneling configuration and applies it to VPN connections
 */
class SplitTunnelingService private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "SplitTunnelingService"
        private const val PREFS_NAME = "split_tunneling_prefs"
        private const val KEY_CONFIG = "split_tunneling_config"
        private const val KEY_ENABLED = "split_tunneling_enabled"
        private const val KEY_MODE = "split_tunneling_mode"
        private const val KEY_APPS = "split_tunneling_apps"
        
        @Volatile
        private var INSTANCE: SplitTunnelingService? = null
        
        fun getInstance(context: Context): SplitTunnelingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SplitTunnelingService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // State flows for reactive UI updates
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled
    
    private val _mode = MutableStateFlow(SplitTunnelingMode.EXCLUDE)
    val mode: StateFlow<SplitTunnelingMode> = _mode
    
    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps
    
    private val _config = MutableStateFlow<SplitTunnelingConfig?>(null)
    val config: StateFlow<SplitTunnelingConfig?> = _config
    
    init {
        loadConfiguration()
    }
    
    /**
     * Enable or disable split tunneling
     */
    fun setEnabled(enabled: Boolean) {
        Log.d(TAG, "Setting split tunneling enabled: $enabled")
        _isEnabled.value = enabled
        saveConfiguration()
    }
    
    /**
     * Set split tunneling mode (INCLUDE or EXCLUDE)
     */
    fun setMode(mode: SplitTunnelingMode) {
        Log.d(TAG, "Setting split tunneling mode: $mode")
        _mode.value = mode
        saveConfiguration()
    }
    
    /**
     * Add apps to split tunneling selection
     */
    fun addApps(appPackages: List<String>) {
        Log.d(TAG, "Adding apps to split tunneling: $appPackages")
        val currentApps = _selectedApps.value.toMutableSet()
        currentApps.addAll(appPackages)
        _selectedApps.value = currentApps
        saveConfiguration()
    }
    
    /**
     * Remove apps from split tunneling selection
     */
    fun removeApps(appPackages: List<String>) {
        Log.d(TAG, "Removing apps from split tunneling: $appPackages")
        val currentApps = _selectedApps.value.toMutableSet()
        currentApps.removeAll(appPackages)
        _selectedApps.value = currentApps
        saveConfiguration()
    }
    
    /**
     * Set the complete list of selected apps
     */
    fun setSelectedApps(apps: Set<String>) {
        Log.d(TAG, "Setting selected apps: $apps")
        _selectedApps.value = apps
        saveConfiguration()
    }
    
    /**
     * Clear all selected apps
     */
    fun clearSelectedApps() {
        Log.d(TAG, "Clearing all selected apps")
        _selectedApps.value = emptySet()
        saveConfiguration()
    }
    
    /**
     * Get current split tunneling configuration
     */
    fun getConfiguration(): SplitTunnelingConfig {
        return SplitTunnelingConfig(
            mode = _mode.value,
            appPackages = _selectedApps.value.toList(),
            isEnabled = _isEnabled.value
        )
    }
    
    /**
     * Check if an app should use VPN based on current configuration
     */
    fun shouldAppUseVPN(packageName: String): Boolean {
        if (!_isEnabled.value) {
            // If split tunneling is disabled, all apps use VPN
            return true
        }
        
        val isSelected = _selectedApps.value.contains(packageName)
        
        return when (_mode.value) {
            SplitTunnelingMode.INCLUDE -> {
                // Only selected apps use VPN
                isSelected
            }
            SplitTunnelingMode.EXCLUDE -> {
                // All apps use VPN except selected ones
                !isSelected
            }
        }
    }
    
    /**
     * Get apps that should use VPN
     */
    fun getAppsUsingVPN(): Set<String> {
        if (!_isEnabled.value) {
            return emptySet() // All apps use VPN
        }
        
        return when (_mode.value) {
            SplitTunnelingMode.INCLUDE -> {
                // Only selected apps use VPN
                _selectedApps.value
            }
            SplitTunnelingMode.EXCLUDE -> {
                // All apps use VPN except selected ones
                // Return empty set to indicate "all except selected"
                emptySet()
            }
        }
    }
    
    /**
     * Get apps that should NOT use VPN
     */
    fun getAppsNotUsingVPN(): Set<String> {
        if (!_isEnabled.value) {
            return emptySet() // No apps bypass VPN
        }
        
        return when (_mode.value) {
            SplitTunnelingMode.INCLUDE -> {
                // All apps except selected ones bypass VPN
                emptySet() // Return empty set to indicate "all except selected"
            }
            SplitTunnelingMode.EXCLUDE -> {
                // Only selected apps bypass VPN
                _selectedApps.value
            }
        }
    }
    
    /**
     * Reset to default configuration
     */
    fun resetToDefault() {
        Log.d(TAG, "Resetting split tunneling to default")
        _isEnabled.value = false
        _mode.value = SplitTunnelingMode.EXCLUDE
        _selectedApps.value = emptySet()
        saveConfiguration()
    }
    
    /**
     * Load configuration from SharedPreferences
     */
    private fun loadConfiguration() {
        try {
            _isEnabled.value = prefs.getBoolean(KEY_ENABLED, false)
            
            val modeString = prefs.getString(KEY_MODE, SplitTunnelingMode.EXCLUDE.name)
            _mode.value = try {
                SplitTunnelingMode.valueOf(modeString ?: SplitTunnelingMode.EXCLUDE.name)
            } catch (e: IllegalArgumentException) {
                SplitTunnelingMode.EXCLUDE
            }
            
            val appsJson = prefs.getString(KEY_APPS, "[]")
            val appsList: List<String> = try {
                gson.fromJson(appsJson, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
            _selectedApps.value = appsList.toSet()
            
            // Update config state
            _config.value = getConfiguration()
            
            Log.d(TAG, "Loaded configuration: enabled=${_isEnabled.value}, mode=${_mode.value}, apps=${_selectedApps.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading split tunneling configuration", e)
            resetToDefault()
        }
    }
    
    /**
     * Save configuration to SharedPreferences
     */
    private fun saveConfiguration() {
        try {
            prefs.edit()
                .putBoolean(KEY_ENABLED, _isEnabled.value)
                .putString(KEY_MODE, _mode.value.name)
                .putString(KEY_APPS, gson.toJson(_selectedApps.value.toList()))
                .apply()
            
            // Update config state
            _config.value = getConfiguration()
            
            Log.d(TAG, "Saved configuration: enabled=${_isEnabled.value}, mode=${_mode.value}, apps=${_selectedApps.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving split tunneling configuration", e)
        }
    }
    
    /**
     * Export configuration as JSON string
     */
    fun exportConfiguration(): String {
        return gson.toJson(getConfiguration())
    }
    
    /**
     * Import configuration from JSON string
     */
    fun importConfiguration(json: String): Boolean {
        return try {
            val config = gson.fromJson(json, SplitTunnelingConfig::class.java)
            _isEnabled.value = config.isEnabled
            _mode.value = config.mode
            _selectedApps.value = config.appPackages.toSet()
            saveConfiguration()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing split tunneling configuration", e)
            false
        }
    }
}
