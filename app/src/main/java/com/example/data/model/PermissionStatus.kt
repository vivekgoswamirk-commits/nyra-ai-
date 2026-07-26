package com.example.data.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.service.NyraAccessibilityService

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val isMandatory: Boolean = false,
    val intent: Intent? = null
)

object PermissionHelper {

    fun checkAllPermissions(context: Context): List<PermissionItem> {
        val list = mutableListOf<PermissionItem>()

        // 1. Microphone Permission
        val micGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        list.add(
            PermissionItem(
                id = "MIC",
                title = "Microphone Access",
                description = "Required so you can talk to Nyra using voice commands.",
                isGranted = micGranted,
                isMandatory = false
            )
        )

        // 2. Overlay Permission (System Alert Window)
        val overlayGranted = Settings.canDrawOverlays(context)
        val overlayIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        list.add(
            PermissionItem(
                id = "OVERLAY",
                title = "Overlay Permission",
                description = "Allows Nyra to display voice indicators over other apps.",
                isGranted = overlayGranted,
                intent = overlayIntent
            )
        )

        // 3. Notification Access
        val notifListenerGranted = isNotificationListenerEnabled(context)
        val notifIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        list.add(
            PermissionItem(
                id = "NOTIFICATION",
                title = "Notification Access",
                description = "Enables Nyra to notify you and read incoming assistant alerts.",
                isGranted = notifListenerGranted,
                intent = notifIntent
            )
        )

        // 4. Accessibility Service
        val accessibilityGranted = isAccessibilityServiceEnabled(context)
        val accessIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        list.add(
            PermissionItem(
                id = "ACCESSIBILITY",
                title = "Accessibility Service",
                description = "Allows Nyra to perform hands-free device navigation actions.",
                isGranted = accessibilityGranted,
                intent = accessIntent
            )
        )

        // 5. Battery Optimization Exemption
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(context.packageName)
        val batteryIntent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        list.add(
            PermissionItem(
                id = "BATTERY",
                title = "Battery Optimization Exemption",
                description = "Keeps Nyra responsive in the background without being killed by the OS.",
                isGranted = batteryGranted,
                intent = batteryIntent
            )
        )

        return list
    }

    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(context.packageName)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedService = "${context.packageName}/${NyraAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices != null && enabledServices.contains(expectedService)
    }
}
