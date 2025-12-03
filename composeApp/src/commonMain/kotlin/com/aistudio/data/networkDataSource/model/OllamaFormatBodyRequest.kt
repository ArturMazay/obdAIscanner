package com.aistudio.data.networkDataSource.model

import kotlinx.serialization.Serializable

/**
 * Формат запроса для Ollama API /api/generate
 * Ollama использует другой формат, не OpenAI-совместимый
 */
@Serializable
data class OllamaFormatBodyRequest(
    // Название модели
    // Пример: "deepseek-coder:1.3b-instruct"
    val model: String,

    // Промпт для генерации (текст запроса)
    // Пример: "Привет! Расскажи о Kotlin"
    val prompt: String,

    // Включить streaming ответ (по умолчанию false)
    val stream: Boolean = false,

    // Температура для генерации (опционально)
    // Пример: 0.7
    val temperature: Double? = null,

    // Системный промпт (опционально)
    // Пример: "Ты полезный AI ассистент..."
    val system: String? = null,

    // Контекст из предыдущих сообщений (опционально)
    val context: List<Long>? = null,

    // Максимальное количество токенов в ответе (опционально)
    val num_predict: Int? = null
)

