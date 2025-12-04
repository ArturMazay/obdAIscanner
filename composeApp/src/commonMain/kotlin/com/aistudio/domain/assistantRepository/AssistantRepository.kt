package com.aistudio.domain.assistantRepository

import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.ChatMessage as NetworkChatMessage

/**
 * Интерфейс репозитория для работы с AI ассистентом
 * Работает с доменными моделями
 */
interface AssistantRepository {
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<NetworkChatMessage> = emptyList()
    ): ChatAnswer
}