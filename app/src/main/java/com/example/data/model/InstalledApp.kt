package com.example.data.model

import android.content.Intent
import android.graphics.drawable.Drawable

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val icon: Drawable? = null,
    val launchIntent: Intent? = null
)

sealed class AppLaunchResult {
    data class Success(val app: InstalledApp) : AppLaunchResult()
    object NotInstalled : AppLaunchResult()
    data class Error(val reason: String) : AppLaunchResult()
}
