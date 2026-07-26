package com.example.assistant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import com.example.data.model.AppLaunchResult
import com.example.data.model.InstalledApp
import java.util.Locale

class AppLauncherManager(private val context: Context) {

    fun getInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(mainIntent, 0)
        val appList = mutableListOf<InstalledApp>()

        for (resolveInfo in resolveInfoList) {
            val appName = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            val icon = resolveInfo.loadIcon(pm)

            if (launchIntent != null) {
                appList.add(
                    InstalledApp(
                        appName = appName,
                        packageName = packageName,
                        icon = icon,
                        launchIntent = launchIntent
                    )
                )
            }
        }
        return appList.distinctBy { it.packageName }.sortedBy { it.appName }
    }

    fun findAndLaunchApp(requestedAppName: String): Pair<AppLaunchResult, String> {
        val cleanQuery = extractAppNameQuery(requestedAppName).lowercase(Locale.getDefault()).trim()

        if (cleanQuery.isBlank()) {
            return Pair(
                AppLaunchResult.Error("Invalid app name"),
                "Boss, please specify which app you want me to open."
            )
        }

        // Special handling for YouTube if "youtube" is requested
        if (cleanQuery.contains("youtube") || cleanQuery == "yt") {
            try {
                val allApps = getInstalledApps()
                val ytApp = allApps.firstOrNull {
                    it.packageName.lowercase(Locale.getDefault()).contains("youtube") ||
                            it.appName.lowercase(Locale.getDefault()).contains("youtube")
                }
                if (ytApp != null && ytApp.launchIntent != null) {
                    ytApp.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(ytApp.launchIntent)
                    return Pair(
                        AppLaunchResult.Success(ytApp),
                        "Opening YouTube, Boss!"
                    )
                }
                // Fallback to Web YouTube
                val ytWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(ytWebIntent)
                return Pair(
                    AppLaunchResult.Success(InstalledApp("YouTube", "com.google.android.youtube", launchIntent = ytWebIntent)),
                    "Opening YouTube, Boss!"
                )
            } catch (e: Exception) {
                // Ignore and try general launcher
            }
        }

        // Special handling for System Camera if "camera" is requested
        if (cleanQuery == "camera" || cleanQuery == "kamera") {
            try {
                val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(cameraIntent)
                return Pair(
                    AppLaunchResult.Success(
                        InstalledApp("Camera", "com.android.camera", launchIntent = cameraIntent)
                    ),
                    "Opening Camera, Boss!"
                )
            } catch (e: Exception) {
                // Fallback to normal app search below
            }
        }

        val allApps = getInstalledApps()

        // 1. Exact match
        var matchedApp = allApps.firstOrNull {
            it.appName.lowercase(Locale.getDefault()) == cleanQuery
        }

        // 2. Contains match
        if (matchedApp == null) {
            matchedApp = allApps.firstOrNull {
                it.appName.lowercase(Locale.getDefault()).contains(cleanQuery) ||
                        cleanQuery.contains(it.appName.lowercase(Locale.getDefault()))
            }
        }

        // 3. Package name match
        if (matchedApp == null) {
            matchedApp = allApps.firstOrNull {
                it.packageName.lowercase(Locale.getDefault()).contains(cleanQuery)
            }
        }

        if (matchedApp != null && matchedApp.launchIntent != null) {
            return try {
                matchedApp.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(matchedApp.launchIntent)
                Pair(
                    AppLaunchResult.Success(matchedApp),
                    "Opening ${matchedApp.appName}, Boss!"
                )
            } catch (e: Exception) {
                Pair(
                    AppLaunchResult.Error(e.localizedMessage ?: "Launch failed"),
                    "Boss, unable to launch ${matchedApp.appName} right now."
                )
            }
        }

        // Fallback for popular web services if native app not installed
        val webFallbackUrl = when {
            cleanQuery.contains("google") -> "https://www.google.com"
            cleanQuery.contains("instagram") -> "https://www.instagram.com"
            cleanQuery.contains("facebook") -> "https://www.facebook.com"
            cleanQuery.contains("whatsapp") -> "https://web.whatsapp.com"
            cleanQuery.contains("twitter") || cleanQuery.contains("x.com") -> "https://x.com"
            else -> null
        }

        if (webFallbackUrl != null) {
            return try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallbackUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                Pair(
                    AppLaunchResult.Success(InstalledApp(cleanQuery.replaceFirstChar { it.titlecase(Locale.getDefault()) }, cleanQuery, launchIntent = webIntent)),
                    "Opening $cleanQuery, Boss!"
                )
            } catch (e: Exception) {
                Pair(
                    AppLaunchResult.NotInstalled,
                    "Boss, $cleanQuery open nahi ho paya."
                )
            }
        }

        return Pair(
            AppLaunchResult.NotInstalled,
            "Boss, ye app ($cleanQuery) aapke phone me install nahi hai."
        )
    }

    private fun extractAppNameQuery(rawQuery: String): String {
        var query = rawQuery.trim()
        val prefixes = listOf("open ", "launch ", "start ", "run ", "chalu karo ", "kholo ", "play ", "go to ", "show ")
        for (prefix in prefixes) {
            if (query.lowercase(Locale.getDefault()).startsWith(prefix)) {
                query = query.substring(prefix.length).trim()
                break
            }
        }
        val suffixes = listOf(" kholo", " open karo", " chalu karo", " open", " launch karo", " play karo", " play")
        for (suffix in suffixes) {
            if (query.lowercase(Locale.getDefault()).endsWith(suffix)) {
                query = query.substring(0, query.length - suffix.length).trim()
                break
            }
        }
        return query
    }
}
