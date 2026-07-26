package com.example.data.repository

import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: ChatMessageDao) {

    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()

    suspend fun addMessage(
        sender: String,
        text: String,
        actionType: String = "CHAT",
        isError: Boolean = false,
        isFinancialWarning: Boolean = false
    ): Long {
        val entity = ChatMessageEntity(
            sender = sender,
            messageText = text,
            actionType = actionType,
            isError = isError,
            isFinancialWarning = isFinancialWarning
        )
        return dao.insertMessage(entity)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
