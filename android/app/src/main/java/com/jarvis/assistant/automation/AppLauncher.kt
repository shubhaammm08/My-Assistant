package com.jarvis.assistant.automation

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

class AppLauncher(private val context: Context) {
    private val TAG = "JarvisAppLauncher"

    fun launchAppByName(appName: String): Boolean {
        val pm = context.packageManager
        val cleanName = appName.trim().lowercase()

        // 1. Direct package lookup shortcuts for popular applications
        val wellKnownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "camera" to "com.google.android.GoogleCamera",
            "settings" to "com.android.settings",
            "clock" to "com.google.android.deskclock",
            "calculator" to "com.google.android.calculator",
            "calendar" to "com.google.android.calendar",
            "maps" to "com.google.android.apps.maps",
            "spotify" to "com.spotify.music",
            "gmail" to "com.google.android.gm"
        )

        for ((alias, pkg) to wellKnownPackages) {
            if (cleanName.contains(alias)) {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Log.i(TAG, "Launched well-known app: $pkg")
                    return true
                }
            }
        }

        // 2. Scan installed application labels
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in installedApps) {
                // Ignore system background processes without launch intent
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) ?: continue
                val label = pm.getApplicationLabel(appInfo).toString().lowercase()

                if (label.contains(cleanName) || cleanName.contains(label)) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Log.i(TAG, "Launched installed app: ${appInfo.packageName} (label: $label)")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving app launch intent: ${e.message}")
        }

        // 3. Fallback: Search Play Store for app
        return try {
            val marketIntent = Intent(
                Intent.ACTION_VIEW,
                android.net.Uri.parse("market://search?q=$appName")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(marketIntent)
            true
        } catch (e: Exception) {
            false
        }
    }
}

