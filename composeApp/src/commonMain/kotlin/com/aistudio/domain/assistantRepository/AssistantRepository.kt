package com.aistudio.domain.assistantRepository

import com.aistudio.domain.model.ChatAnswer

/**
 * Интерфейс репозитория для работы с AI ассистентом
 * Работает с доменными моделями
 */
interface AssistantRepository {
    suspend fun sendMessage(message: String): ChatAnswer
}