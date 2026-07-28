package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class UserProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nyra_user_profile_prefs", Context.MODE_PRIVATE)

    init {
        ensureUidGenerated()
    }

    fun ensureUidGenerated(): String {
        var existingUid = prefs.getString("user_uid", "") ?: ""
        if (existingUid.length != 10 || !existingUid.all { it.isDigit() }) {
            // Generate a unique 10-digit UID (e.g. 8492048291)
            val firstDigit = Random.nextInt(1, 10)
            val remainingDigits = (1..9).map { Random.nextInt(0, 10) }.joinToString("")
            existingUid = "$firstDigit$remainingDigits"

            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            prefs.edit()
                .putString("user_uid", existingUid)
                .putString("user_name", "Boss")
                .putString("user_email", "boss@nyra.ai")
                .putString("user_tier", "VIP Pro Member")
                .putString("joined_date", currentDate)
                .apply()
        }
        return existingUid
    }

    fun getUserProfile(): UserProfile {
        val uid = ensureUidGenerated()
        val userName = prefs.getString("user_name", "Boss") ?: "Boss"
        val userEmail = prefs.getString("user_email", "boss@nyra.ai") ?: "boss@nyra.ai"
        val userTier = prefs.getString("user_tier", "VIP Pro Member") ?: "VIP Pro Member"
        val joinedDate = prefs.getString("joined_date", "Today") ?: "Today"
        val avatarId = prefs.getInt("avatar_id", 0)
        val photoUrl = prefs.getString("photo_url", null)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val authProvider = prefs.getString("auth_provider", "Google / Firebase") ?: "Google / Firebase"

        return UserProfile(
            uid = uid,
            userName = userName,
            userEmail = userEmail,
            accountTier = userTier,
            joinedDate = joinedDate,
            avatarId = avatarId,
            photoUrl = photoUrl,
            isLoggedIn = isLoggedIn,
            authProvider = authProvider
        )
    }

    fun saveAuthSession(name: String, email: String, photoUrl: String? = null, provider: String = "Google / Firebase") {
        ensureUidGenerated()
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name.ifBlank { "Boss" })
            .putString("user_email", email.ifBlank { "boss@nyra.ai" })
            .putString("photo_url", photoUrl)
            .putString("auth_provider", provider)
            .apply()
    }

    fun signOut() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
    }

    fun updateUserProfile(userName: String, userEmail: String, avatarId: Int = 0) {
        prefs.edit()
            .putString("user_name", userName.trim())
            .putString("user_email", userEmail.trim())
            .putInt("avatar_id", avatarId)
            .apply()
    }
}
