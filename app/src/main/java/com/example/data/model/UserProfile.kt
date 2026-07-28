package com.example.data.model

data class UserProfile(
    val uid: String,
    val userName: String,
    val userEmail: String,
    val accountTier: String,
    val joinedDate: String,
    val avatarId: Int = 0,
    val photoUrl: String? = null,
    val isLoggedIn: Boolean = false,
    val authProvider: String = "Google / Firebase"
)
