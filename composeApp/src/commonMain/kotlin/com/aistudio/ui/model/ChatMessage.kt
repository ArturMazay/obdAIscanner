package com.aistudio.ui.model

/**
 * Модель сообщения для UI чата
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean, // true - сообщение пользователя, false - ответ AI
    val timestamp: Long = System.currentTimeMillis()
)

