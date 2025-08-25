package com.example.v.vpn

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Split Tunneling Service for per-app VPN routing
 * Matches desktop functionality while remaining Android-compatible
 */
class SplitTunnelingService private constructor(private val context: Context) {
    companion object {
        private const val TAG = "SplitTunnelingService"
        private const val PREFS_NAME = "split_tunneling_prefs"
        private const val KEY_ENABLED = "split_tunneling_enabled"
        private const val KEY_VPN_APPS = "vpn_apps"
        private const val KEY_BYPASS_APPS = "bypass_apps"
        
        @Volatile
        private var INSTANCE: SplitTunnelingService? = null
        
        fun getInstance(context: Context): SplitTunnelingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SplitTunnelingService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // State flows for reactive UI updates
    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val isEnabled: StateFlow<Boolean> = _isEnabled
    
    private val _vpnApps = MutableStateFlow<Set<String>>(getVpnApps())
    val vpnApps: StateFlow<Set<String>> = _vpnApps
    
    private val _bypassApps = MutableStateFlow<Set<String>>(getBypassApps())
    val bypassApps: StateFlow<Set<String>> = _bypassApps
    
    /**
     * Enable/disable split tunneling
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        Log.i(TAG, "🔧 Split tunneling ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Add app to VPN routing (through VPN)
     */
    fun addVpnApp(packageName: String) {
        val current = _vpnApps.value.toMutableSet()
        current.add(packageName)
        _vpnApps.value = current
        saveVpnApps(current)
        Log.i(TAG, "➕ App added to VPN routing: $packageName")
    }
    
    /**
     * Remove app from VPN routing
     */
    fun removeVpnApp(packageName: String) {
        val current = _vpnApps.value.toMutableSet()
        current.remove(packageName)
        _vpnApps.value = current
        saveVpnApps(current)
        Log.i(TAG, "➖ App removed from VPN routing: $packageName")
    }
    
    /**
     * Add app to bypass list (not through VPN)
     */
    fun addBypassApp(packageName: String) {
        val current = _bypassApps.value.toMutableSet()
        current.add(packageName)
        _bypassApps.value = current
        saveBypassApps(current)
        Log.i(TAG, "➕ App added to bypass list: $packageName")
    }
    
    /**
     * Remove app from bypass list
     */
    fun removeBypassApp(packageName: String) {
        val current = _bypassApps.value.toMutableSet()
        current.remove(packageName)
        _bypassApps.value = current
        saveBypassApps(current)
        Log.i(TAG, "➖ App removed from bypass list: $packageName")
    }
    
    /**
     * Check if app should use VPN
     */
    fun shouldAppUseVPN(packageName: String): Boolean {
        if (!_isEnabled.value) {
            return true // All apps use VPN when split tunneling is disabled
        }
        
        return _vpnApps.value.contains(packageName)
    }
    
    /**
     * Check if app should bypass VPN
     */
    fun shouldAppBypassVPN(packageName: String): Boolean {
        if (!_isEnabled.value) {
            return false // No apps bypass VPN when split tunneling is disabled
        }
        
        return _bypassApps.value.contains(packageName)
    }
    
    /**
     * Get all installed apps
     */
    fun getInstalledApps(): List<AppInfo> {
        return try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Only user apps
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = appInfo.loadLabel(packageManager).toString(),
                        icon = appInfo.loadIcon(packageManager),
                        isVpnApp = _vpnApps.value.contains(appInfo.packageName),
                        isBypassApp = _bypassApps.value.contains(appInfo.packageName)
                    )
                }
                .sortedBy { it.appName }
                
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting installed apps: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get system apps (essential apps that should always use VPN)
     */
    fun getSystemApps(): List<AppInfo> {
        return try {
            val packageManager = context.packageManager
            val systemApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            systemApps
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 } // Only system apps
                .filter { isEssentialSystemApp(it.packageName) }
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = appInfo.loadLabel(packageManager).toString(),
                        icon = appInfo.loadIcon(packageManager),
                        isVpnApp = true, // System apps always use VPN
                        isBypassApp = false
                    )
                }
                .sortedBy { it.appName }
                
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting system apps: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get split tunneling configuration for VPN service
     */
    fun getSplitTunnelingConfig(): SplitTunnelingConfig {
        return SplitTunnelingConfig(
            mode = SplitTunnelingMode.EXCLUDE,
            appPackages = _bypassApps.value.toList(),
            isEnabled = _isEnabled.value
        )
    }
    
    /**
     * Reset to default configuration
     */
    fun resetToDefault() {
        val defaultVpnApps = setOf(
            "com.android.chrome",
            "com.google.android.gm",
            "com.whatsapp",
            "com.instagram.android",
            "com.facebook.katana"
        )
        
        val defaultBypassApps = setOf(
            "com.android.vending", // Play Store
            "com.google.android.apps.maps", // Maps
            "com.ubercab" // Uber
        )
        
        _vpnApps.value = defaultVpnApps
        _bypassApps.value = defaultBypassApps
        _isEnabled.value = true
        
        saveVpnApps(defaultVpnApps)
        saveBypassApps(defaultBypassApps)
        prefs.edit().putBoolean(KEY_ENABLED, true).apply()
        
        Log.i(TAG, "🔄 Split tunneling reset to default configuration")
    }
    
    private fun isEssentialSystemApp(packageName: String): Boolean {
        return packageName in setOf(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.phone",
            "com.android.providers.telephony"
        )
    }
    
    private fun getVpnApps(): Set<String> {
        return prefs.getStringSet(KEY_VPN_APPS, emptySet()) ?: emptySet()
    }
    
    private fun getBypassApps(): Set<String> {
        return prefs.getStringSet(KEY_BYPASS_APPS, emptySet()) ?: emptySet()
    }
    
    private fun saveVpnApps(apps: Set<String>) {
        prefs.edit().putStringSet(KEY_VPN_APPS, apps).apply()
    }
    
    private fun saveBypassApps(apps: Set<String>) {
        prefs.edit().putStringSet(KEY_BYPASS_APPS, apps).apply()
    }
}

/**
 * App information for split tunneling UI
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isVpnApp: Boolean,
    val isBypassApp: Boolean
)
