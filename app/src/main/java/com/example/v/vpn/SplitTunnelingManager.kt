package com.example.v.vpn

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.v.vpn.WireGuardVpnService.Companion.ACTION_SET_SPLIT_TUNNELING

/**
 * Split Tunneling Manager
 * Provides easy-to-use methods for configuring split tunneling
 */
class SplitTunnelingManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SplitTunnelingManager"
        
        @Volatile
        private var INSTANCE: SplitTunnelingManager? = null
        
        fun getInstance(context: Context): SplitTunnelingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SplitTunnelingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Set split tunneling mode with app list
     * @param mode "INCLUDE" for whitelist mode, "EXCLUDE" for blacklist mode
     * @param appPackages List of app package names
     */
    fun setSplitTunnelingMode(mode: String, appPackages: List<String>) {
        Log.d(TAG, "Setting split tunneling mode: $mode with ${appPackages.size} apps")
        
        val intent = Intent(context, WireGuardVpnService::class.java).apply {
            action = ACTION_SET_SPLIT_TUNNELING
            putExtra("mode", mode)
            putStringArrayListExtra("app_packages", ArrayList(appPackages))
        }
        
        try {
            context.startService(intent)
            Log.d(TAG, "Split tunneling configuration sent to VPN service")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set split tunneling mode", e)
        }
    }
    
    /**
     * Configure include mode (whitelist) - only selected apps go through VPN
     * @param appPackages List of app package names that should use VPN
     */
    fun setIncludeMode(appPackages: List<String>) {
        Log.d(TAG, "Setting INCLUDE mode with ${appPackages.size} apps")
        setSplitTunnelingMode("INCLUDE", appPackages)
    }
    
    /**
     * Configure exclude mode (blacklist) - all apps use VPN except selected ones
     * @param appPackages List of app package names that should bypass VPN
     */
    fun setExcludeMode(appPackages: List<String>) {
        Log.d(TAG, "Setting EXCLUDE mode with ${appPackages.size} apps")
        setSplitTunnelingMode("EXCLUDE", appPackages)
    }
    
    /**
     * Disable split tunneling - all apps go through VPN
     */
    fun disableSplitTunneling() {
        Log.d(TAG, "Disabling split tunneling - all apps will use VPN")
        setSplitTunnelingMode("EXCLUDE", emptyList())
    }
    
    /**
     * Configure split tunneling for banking apps (include mode)
     * Only banking apps will use VPN for enhanced security
     */
    fun configureBankingAppsOnly() {
        Log.d(TAG, "Configuring split tunneling for banking apps only")
        setIncludeMode(SplitTunnelingApps.BANKING_APPS)
    }
    
    /**
     * Configure split tunneling for streaming apps (exclude mode)
     * All apps use VPN except streaming apps to avoid geo-restrictions
     */
    fun configureStreamingAppsExcluded() {
        Log.d(TAG, "Configuring split tunneling to exclude streaming apps")
        setExcludeMode(SplitTunnelingApps.STREAMING_APPS)
    }
    
    /**
     * Configure split tunneling for local network apps (exclude mode)
     * All apps use VPN except local network apps that need direct access
     */
    fun configureLocalNetworkAppsExcluded() {
        Log.d(TAG, "Configuring split tunneling to exclude local network apps")
        setExcludeMode(SplitTunnelingApps.LOCAL_NETWORK_APPS)
    }
    
    /**
     * Configure split tunneling for gaming apps (exclude mode)
     * All apps use VPN except gaming apps for better performance
     */
    fun configureGamingAppsExcluded() {
        Log.d(TAG, "Configuring split tunneling to exclude gaming apps")
        setExcludeMode(SplitTunnelingApps.GAMING_APPS)
    }
    
    /**
     * Configure split tunneling for social media apps (exclude mode)
     * All apps use VPN except social media apps
     */
    fun configureSocialMediaAppsExcluded() {
        Log.d(TAG, "Configuring split tunneling to exclude social media apps")
        setExcludeMode(SplitTunnelingApps.SOCIAL_MEDIA_APPS)
    }
    
    /**
     * Configure custom split tunneling with multiple app categories
     * @param includeApps Apps that should use VPN (include mode)
     * @param excludeApps Apps that should bypass VPN (exclude mode)
     * Note: This method prioritizes include mode if both lists are provided
     */
    fun configureCustomSplitTunneling(includeApps: List<String> = emptyList(), excludeApps: List<String> = emptyList()) {
        when {
            includeApps.isNotEmpty() -> {
                Log.d(TAG, "Configuring custom INCLUDE mode with ${includeApps.size} apps")
                setIncludeMode(includeApps)
            }
            excludeApps.isNotEmpty() -> {
                Log.d(TAG, "Configuring custom EXCLUDE mode with ${excludeApps.size} apps")
                setExcludeMode(excludeApps)
            }
            else -> {
                Log.d(TAG, "No apps specified, disabling split tunneling")
                disableSplitTunneling()
            }
        }
    }
    
    /**
     * Get current split tunneling configuration from SharedPreferences
     */
    fun getCurrentSplitTunnelingConfig(): SplitTunnelingConfig {
        val preferences = context.getSharedPreferences("vpn_preferences", Context.MODE_PRIVATE)
        val modeName = preferences.getString("split_tunneling_mode", "EXCLUDE") ?: "EXCLUDE"
        val appPackages = preferences.getStringSet("split_tunneling_apps", emptySet())?.toList() ?: emptyList()
        val isEnabled = preferences.getBoolean("split_tunneling_enabled", false)
        
        val mode = when (modeName) {
            "INCLUDE" -> SplitTunnelingMode.INCLUDE
            "EXCLUDE" -> SplitTunnelingMode.EXCLUDE
            else -> SplitTunnelingMode.EXCLUDE
        }
        
        return SplitTunnelingConfig(
            mode = mode,
            appPackages = appPackages,
            isEnabled = isEnabled
        )
    }
    
    /**
     * Check if split tunneling is currently enabled
     */
    fun isSplitTunnelingEnabled(): Boolean {
        return getCurrentSplitTunnelingConfig().isEnabled
    }
    
    /**
     * Get the current split tunneling mode
     */
    fun getCurrentSplitTunnelingMode(): SplitTunnelingMode {
        return getCurrentSplitTunnelingConfig().mode
    }
    
    /**
     * Get the current list of apps in split tunneling configuration
     */
    fun getCurrentSplitTunnelingApps(): List<String> {
        return getCurrentSplitTunnelingConfig().appPackages
    }
}
