package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String = "CHAT", // "CHAT", "APP_LAUNCH", "TIME", "DEVICE_CONTROL", "SAFETY_WARNING"
    val isError: Boolean = false,
    val isFinancialWarning: Boolean = false
)
