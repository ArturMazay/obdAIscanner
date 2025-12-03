package com.aistudio.data.networkDataSource.model

import kotlinx.serialization.Serializable

/**
 * Формат ответа для структурированного JSON
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7
)

/**
 * JSON Schema для структурированного ответа (опционально)
 * Используется для более строгого контроля формата ответа
 */
/**
 * JSON Schema для структурированного ответа (опционально)
 * Используется для более строгого контроля формата ответа
 * Примечание: schema убран, так как Map<String, Any> не сериализуется напрямую
 */
@Serializable
data class JsonSchema(
    val name: String? = null,
    val strict: Boolean = false
)



// Hugging Face может вернуть массив ответов
typealias ChatResponseList = List<OpenAiFormatAnswerResponse>

// OpenAI совместимый формат (для альтернативного API)
@Serializable
data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String?
)

/**
 * Информация об использовании токенов в запросе
 * Показывает сколько токенов было использовано для промпта и ответа
 */
@Serializable
data class Usage(
    // Количество токенов в промпте (вопрос пользователя + системное сообщение)
    // Пример: 74 токена
    val prompt_tokens: Int,
    
    // Количество токенов в ответе модели
    // Пример: 115 токенов
    val completion_tokens: Int,
    
    // Общее количество токенов (prompt_tokens + completion_tokens)
    // Пример: 189 токенов
    val total_tokens: Int
    
    // Примечание: prompt_tokens_details и completion_tokens_details убраны,
    // так как они обычно null и Map<String, Any> не сериализуется напрямую в kotlinx.serialization
    // Если понадобятся, можно добавить как JsonObject или игнорировать через @Transient
    // В JSON они приходят как null, поэтому игнорируем их
)

