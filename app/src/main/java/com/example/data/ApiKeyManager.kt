package com.example.data

import android.content.Context
import android.content.SharedPreferences

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nyra_api_prefs", Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val storedKey = prefs.getString("gemini_api_key", "") ?: ""
        if (storedKey.isNotBlank()) {
            return storedKey.trim()
        }
        return try {
            val buildConfigKey = com.example.BuildConfig.GEMINI_API_KEY
            if (buildConfigKey != "MY_GEMINI_API_KEY") buildConfigKey.trim() else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun hasApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    fun clearApiKey() {
        prefs.edit().remove("gemini_api_key").apply()
    }
}
