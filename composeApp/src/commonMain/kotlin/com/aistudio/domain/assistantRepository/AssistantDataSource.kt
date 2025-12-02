package com.aistudio.domain.assistantRepository

import com.aistudio.data.networkDataSource.helpers.ResultResponse
import com.aistudio.domain.model.ChatAnswer

/**
 * Интерфейс для источника данных AI ассистента
 * Возвращает доменную модель ChatAnswer после маппинга из сетевой модели
 */
interface AssistantDataSource {
    suspend fun sendMessage(message: String): ResultResponse<ChatAnswer>
}