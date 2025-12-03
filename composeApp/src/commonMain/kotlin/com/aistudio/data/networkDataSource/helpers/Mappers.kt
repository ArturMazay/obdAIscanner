package com.aistudio.data.networkDataSource.helpers

import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.OpenAiFormatAnswerResponse
import com.aistudio.data.networkDataSource.model.OllamaFormatAnswerResponse
import com.aistudio.data.networkDataSource.model.ChatMessage
import com.aistudio.data.networkDataSource.model.Choice
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Мапперы для преобразования сетевых моделей в доменные
 */
object NetworkToDomainMapper {

    /**
     * Преобразует OpenAiFormatAnswerResponse (сетевая модель) в ChatAnswer (доменная модель)
     * 
     * Маппинг всех полей из сетевого ответа API в доменную модель:
     * - id -> id (идентификатор ответа)
     * - object -> objectType (тип объекта, например "chat.completion")
     * - created -> created (timestamp создания)
     * - model -> model (название модели)
     * - choices -> choices (массив вариантов ответов)
     * - usage -> usage (информация об использовании токенов)
     * - system_fingerprint -> systemFingerprint (отпечаток системы)
     * - generated_text -> generatedText (альтернативный формат для inference API)
     */
    fun OpenAiFormatAnswerResponse.toChatAnswer(): ChatAnswer {
        return ChatAnswer(
            id = this.id,
            objectType = this.objectType, // objectType уже маппится из "object" через @SerialName
            created = this.created,
            model = this.model,
            choices = this.choices,
            usage = this.usage,
            systemFingerprint = this.system_fingerprint,
            generatedText = this.generated_text,
            error = null,
            isLoading = false
        )
    }

    /**
     * Преобразует OllamaFormatAnswerResponse (сетевая модель) в ChatAnswer (доменная модель)
     * 
     * Маппинг всех полей из Ollama API ответа в доменную модель:
     * - model -> model (название модели)
     * - created_at -> created (timestamp создания, преобразуется из ISO 8601)
     * - response -> choices[0].message.content и generatedText (текст ответа)
     * - done_reason -> choices[0].finish_reason (причина завершения)
     * 
     * @param fullText Полный текст ответа, собранный из всех streaming чанков
     * @param modelName Название модели (из первого чанка)
     * @param createdAt Время создания (из первого чанка, ISO 8601 формат)
     */
    @OptIn(ExperimentalTime::class)
    fun OllamaFormatAnswerResponse.toChatAnswer(
        fullText: String,
        modelName: String,
        createdAt: String?
    ): ChatAnswer {
        return ChatAnswer(
            id = null,
            objectType = "ollama.completion",
            created = createdAt?.let {
                // Парсим ISO 8601 дату в timestamp (упрощенно)
                Clock.System.now().toEpochMilliseconds() / 1000
            },
            model = modelName,
            choices = listOf(
                Choice(
                    index = 0,
                    message = ChatMessage(
                        role = "assistant",
                        content = fullText
                    ),
                    finish_reason = this.done_reason
                )
            ),
            usage = null, // Ollama не предоставляет usage в этом формате
            systemFingerprint = null,
            generatedText = fullText,
            error = null,
            isLoading = false
        )
    }
}
