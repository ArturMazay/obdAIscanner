package com.aistudio.data.networkDataSource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// OpenAI совместимый формат ответа
@Serializable
data class OpenAiFormatAnswerResponse(
    // Уникальный идентификатор ответа
    // Пример: "e3f5ad2bc99c5cb5e9abfa4169d489bf"
    val id: String? = null,

    // Тип объекта (обычно "chat.completion" для chat completions API)
    // Пример: "chat.completion"
    // Используем обратные кавычки, так как object - зарезервированное слово в Kotlin
    @SerialName("object")
    val objectType: String? = null,

    // Unix timestamp создания ответа (в секундах)
    // Пример: 1764691379
    val created: Long? = null,

    // Название модели, которая сгенерировала ответ
    // Пример: "deepseek/deepseek-v3.2"
    val model: String? = null,

    // Массив вариантов ответов (обычно один элемент)
    // Каждый элемент содержит сообщение от ассистента
    val choices: List<Choice>? = null,

    // Информация об использовании токенов
    // Содержит количество токенов в промпте, ответе и общее количество
    val usage: Usage? = null,

    // Отпечаток системы (fingerprint) для отслеживания версии модели
    // Пример: "" или "fp_abc123..."
    val system_fingerprint: String? = null,

    // Альтернативный формат для inference API (если используется другой endpoint)
    val generated_text: String? = null
)
