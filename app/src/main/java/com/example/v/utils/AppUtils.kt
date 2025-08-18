package com.example.v.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.v.models.InstalledApp

object AppUtils {
    
    fun getInstalledApps(context: Context): List<InstalledApp> {
        val packageManager = context.packageManager
        val installedApps = mutableListOf<InstalledApp>()
        
        try {
            val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            for (appInfo in applications) {
                // Skip our own app
                if (appInfo.packageName == context.packageName) continue
                
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                
                // For user apps, require launcher intent. For system apps, be more lenient
                if (!isSystemApp) {
                    // User apps must have launcher intent
                    val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent == null) continue
                }
                
                val appName = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    appInfo.packageName
                }
                
                // Skip apps with empty or null names
                if (appName.isBlank() || appName == "null") continue
                
                val appIcon = try {
                    packageManager.getApplicationIcon(appInfo.packageName)
                } catch (e: Exception) {
                    null
                }
                
                installedApps.add(
                    InstalledApp(
                        packageName = appInfo.packageName,
                        appName = appName,
                        appIcon = appIcon,
                        isSystemApp = isSystemApp,
                        isSelected = false
                    )
                )
            }
            
            // Sort by app name - separate user apps from system apps
            val userApps = installedApps.filter { !it.isSystemApp }.sortedBy { it.appName }
            val systemApps = installedApps.filter { it.isSystemApp }.sortedBy { it.appName }
            
            // Return user apps first, then system apps - no artificial limit
            return userApps + systemApps
            
        } catch (e: Exception) {
            // Log error silently for production
            return emptyList()
        }
    }
    
    fun getFilteredApps(context: Context, showSystemApps: Boolean = false): List<InstalledApp> {
        val allApps = getInstalledApps(context)
        
        return if (showSystemApps) {
            // When "Show System Apps" is ON, show ONLY system apps
            allApps.filter { it.isSystemApp }
        } else {
            // When "Show System Apps" is OFF, show ONLY user apps (not system apps)
            allApps.filter { !it.isSystemApp }
        }
    }
    
    fun getAllApps(context: Context): List<InstalledApp> {
        // Return all apps (both user and system apps) for initial Split Tunneling view
        return getInstalledApps(context)
    }
    
    fun getAppCounts(context: Context): Map<String, Int> {
        val allApps = getInstalledApps(context)
        val userApps = allApps.filter { !it.isSystemApp }
        val systemApps = allApps.filter { it.isSystemApp }
        
        return mapOf(
            "total" to allApps.size,
            "user" to userApps.size,
            "system" to systemApps.size
        )
    }
    
    fun getFilteredAppsWithCount(context: Context, showSystemApps: Boolean = false): Pair<List<InstalledApp>, Int> {
        val filteredApps = getFilteredApps(context, showSystemApps)
        return Pair(filteredApps, filteredApps.size)
    }
} 