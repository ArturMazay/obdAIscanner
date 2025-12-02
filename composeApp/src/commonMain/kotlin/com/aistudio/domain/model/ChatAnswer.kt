package com.aistudio.domain.model

import com.aistudio.data.networkDataSource.model.Choice

/**
 * Доменная модель ответа от AI ассистента
 * Соответствует структуре сетевой модели ChatAnswerResponse,
 * но содержит дополнительные поля для ошибки и статуса загрузки
 */
data class ChatAnswer(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val created: Long? = null,
    val model: String? = null,
    // Альтернативный формат для inference API
    val generatedText: String? = null,
    // Дополнительные поля для обработки ошибок и статуса
    val error: String? = null,
    val isLoading: Boolean = false
)

