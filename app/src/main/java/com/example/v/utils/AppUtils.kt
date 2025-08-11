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
                // Skip system apps, our own app, and apps without launcher intent
                if (appInfo.packageName == context.packageName) continue
                
                // Skip apps that don't have launcher intent (background services, etc.)
                val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent == null) continue
                
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
                
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                
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
            
            // Sort by app name and limit to top 100 for performance
            return installedApps.sortedBy { it.appName }.take(100)
            
        } catch (e: Exception) {
            // Log error silently for production
            return emptyList()
        }
    }
    
    fun getFilteredApps(context: Context, showSystemApps: Boolean = true): List<InstalledApp> {
        val allApps = getInstalledApps(context)
        return if (showSystemApps) {
            allApps
        } else {
            allApps.filter { !it.isSystemApp }
        }
    }
} 