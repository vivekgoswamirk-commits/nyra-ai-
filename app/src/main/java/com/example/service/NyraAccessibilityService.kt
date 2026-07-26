package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class NyraAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Nyra Accessibility Service Connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility event processing for Nyra assistant device navigation
        event?.let {
            val packageName = it.packageName?.toString()
            Log.v(TAG, "Accessibility Event from package: $packageName")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Nyra Accessibility Service Interrupted")
    }

    fun performGlobalActionNav(action: Int): Boolean {
        return performGlobalAction(action)
    }

    companion object {
        private const val TAG = "NyraAccessibility"
        var instance: NyraAccessibilityService? = null
            private set
    }
}
