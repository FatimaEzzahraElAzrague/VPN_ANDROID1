package com.example.v.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable? // Store Drawable instead of converting to painter here
)

fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    return pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        .map { resolveInfo ->
            val appInfo = resolveInfo.activityInfo.applicationInfo
            AppInfo(
                packageName = appInfo.packageName,
                name = pm.getApplicationLabel(appInfo).toString(),
                icon = pm.getApplicationIcon(appInfo) // Store the Drawable directly
            )
        }
        .sortedBy { it.name }
}