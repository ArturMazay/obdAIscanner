package com.aistudio.data.networkDataSource.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7
)


// OpenAI совместимый формат ответа
@Serializable
data class ChatAnswerResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val created: Long? = null,
    val model: String? = null,
    // Альтернативный формат для inference API
    val generated_text: String? = null
)

// Hugging Face может вернуть массив ответов
typealias ChatResponseList = List<ChatAnswerResponse>

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

