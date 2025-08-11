package com.example.v.vpn

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Split tunneling configuration data class
 * Manages which apps should go through the VPN or bypass it
 */
data class SplitTunnelingConfig(
    val mode: SplitTunnelingMode,
    val appPackages: List<String>,
    val isEnabled: Boolean = true
) {
    companion object {
        private const val TAG = "SplitTunnelingConfig"
        
        /**
         * Create a default split tunneling configuration (all apps through VPN)
         */
        fun default(): SplitTunnelingConfig {
            return SplitTunnelingConfig(
                mode = SplitTunnelingMode.EXCLUDE,
                appPackages = emptyList(),
                isEnabled = false
            )
        }
        
        /**
         * Validate app packages and filter out non-installed apps
         */
        fun validateAppPackages(context: Context, packages: List<String>): List<String> {
            val packageManager = context.packageManager
            val validPackages = mutableListOf<String>()
            
            packages.forEach { packageName ->
                try {
                    // Check if the app is installed
                    packageManager.getPackageInfo(packageName, 0)
                    validPackages.add(packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "App package not found: $packageName - skipping")
                } catch (e: Exception) {
                    Log.e(TAG, "Error validating package: $packageName", e)
                }
            }
            
            return validPackages
        }
    }
}

/**
 * Split tunneling modes
 */
enum class SplitTunnelingMode {
    INCLUDE,  // Only selected apps go through VPN (whitelist)
    EXCLUDE   // All apps go through VPN except selected ones (blacklist)
}

/**
 * Common app packages for split tunneling examples
 */
object SplitTunnelingApps {
    // Banking and financial apps
    val BANKING_APPS = listOf(
        "com.chase.sig.android",
        "com.wellsfargo.mobile",
        "com.bankofamerica.mobile",
        "com.capitalone.mobile",
        "com.usaa.mobile.android",
        "com.ally.android"
    )
    
    // Streaming apps that might have geo-restrictions
    val STREAMING_APPS = listOf(
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient",
        "com.hulu.plus",
        "com.disney.disneyplus",
        "com.hbo.hbonow",
        "com.spotify.music",
        "com.apple.android.music"
    )
    
    // Social media apps
    val SOCIAL_MEDIA_APPS = listOf(
        "com.facebook.katana",
        "com.instagram.android",
        "com.twitter.android",
        "com.snapchat.android",
        "com.whatsapp",
        "com.telegram.messenger",
        "com.discord"
    )
    
    // Gaming apps
    val GAMING_APPS = listOf(
        "com.activision.callofduty.shooter",
        "com.epicgames.fortnite",
        "com.roblox.client",
        "com.mojang.minecraftpe",
        "com.supercell.clashofclans",
        "com.supercell.clashroyale"
    )
    
    // Productivity apps that might need local network access
    val PRODUCTIVITY_APPS = listOf(
        "com.microsoft.office.word",
        "com.microsoft.office.excel",
        "com.microsoft.office.powerpoint",
        "com.google.android.apps.docs.editors.docs",
        "com.google.android.apps.docs.editors.sheets",
        "com.google.android.apps.docs.editors.slides"
    )
    
    // Local network apps that should bypass VPN
    val LOCAL_NETWORK_APPS = listOf(
        "com.plexapp.android",
        "com.spotify.music",
        "com.sonos.acr",
        "com.roku.remote",
        "com.amazon.firetv.remote"
    )
}
