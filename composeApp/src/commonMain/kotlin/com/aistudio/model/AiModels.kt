package com.aistudio.model

import kotlinx.serialization.Serializable

// Novita API формат (router.huggingface.co) - OpenAI совместимый
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7
)

// Альтернативный формат (для inference API)
@Serializable
data class InferenceRequest(
    val inputs: String,
    val model: String
)

// OpenAI совместимый формат ответа
@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val created: Long? = null,
    val model: String? = null,
    // Альтернативный формат для inference API
    val generated_text: String? = null
)

// Hugging Face может вернуть массив ответов
typealias ChatResponseList = List<ChatResponse>

// OpenAI совместимый формат (для альтернативного API)
@Serializable
data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String
)

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7
)

@Serializable
data class OpenAIResponse(
    val id: String,
    val choices: List<Choice>,
    val created: Long,
    val model: String
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String?
)

// Формат ошибки от Novita API
@Serializable
data class ApiErrorResponse(
    val message: String? = null,
    val type: String? = null,
    val trace_id: String? = null
)

// Формат ошибки с простым полем error (строка)
@Serializable
data class SimpleErrorResponse(
    val error: String
)

// Старый формат ошибки (для совместимости)
@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

