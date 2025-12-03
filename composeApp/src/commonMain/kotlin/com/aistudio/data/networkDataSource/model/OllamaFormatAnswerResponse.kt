package com.aistudio.data.networkDataSource.model

import kotlinx.serialization.Serializable

/**
 * Формат ответа от Ollama API
 * Используется для локальных моделей через Ollama
 */
@Serializable
data class OllamaFormatAnswerResponse(
    // Название модели
    // Пример: "deepseek-coder:1.3b-instruct"
    val model: String,

    // Время создания ответа в формате ISO 8601
    // Пример: "2025-12-02T18:30:25.810094086Z"
    val created_at: String? = null,

    // Текст ответа от модели
    // Пример: "Привет! Я AI ассистент..."
    val response: String? = null,

    // Флаг завершения генерации
    // true - генерация завершена, false - еще генерируется (streaming)
    val done: Boolean = false,

    // Причина завершения
    // "stop" - нормальное завершение
    // "load" - модель еще загружается
    // "error" - произошла ошибка
    val done_reason: String? = null,

    // Контекст для следующего запроса (опционально)
    val context: List<Long>? = null,

    // Общее время генерации в наносекундах (опционально)
    val total_duration: Long? = null,

    // Время загрузки в наносекундах (опционально)
    val load_duration: Long? = null,

    // Количество токенов в промпте (опционально)
    val prompt_eval_count: Int? = null,

    // Время оценки промпта в наносекундах (опционально)
    val prompt_eval_duration: Long? = null,

    // Количество сгенерированных токенов (опционально)
    val eval_count: Int? = null,

    // Время генерации в наносекундах (опционально)
    val eval_duration: Long? = null
)

