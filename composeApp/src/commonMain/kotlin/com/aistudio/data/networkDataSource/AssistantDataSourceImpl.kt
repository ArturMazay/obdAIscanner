package com.aistudio.data.networkDataSource

import com.aistudio.data.networkDataSource.helpers.NetworkToDomainMapper.toChatAnswer
import com.aistudio.data.networkDataSource.helpers.ResultResponse
import com.aistudio.domain.assistantRepository.AssistantDataSource
import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.ChatMessage
import com.aistudio.data.networkDataSource.model.ChatRequest
import com.aistudio.data.networkDataSource.model.OpenAiFormatAnswerResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Реализация источника данных для AI ассистента
 * Выполняет сетевые запросы и маппинг в доменные модели
 */
class AssistantDataSourceImpl(
    private val httpClient: HttpClient
) : AssistantDataSource {

    companion object {
        private const val MODEL_NAME = "deepseek/deepseek-v3.2"
        private const val API_URL =
            "https://router.huggingface.co/novita/v3/openai/chat/completions"
        private const val DEFAULT_TEMPERATURE = 0.7
    }

    override suspend fun sendMessage(message: String): ResultResponse<ChatAnswer> = try {
            val request = createRequest(message)
            val response: HttpResponse = httpClient.post(API_URL) { setBody(request) }
            handleResponse(response)

        } catch (e: Exception) {
            println("🔴 [AssistantDataSource] Исключение при отправке запроса: ${e.message}")
            e.printStackTrace()
            ResultResponse.Error(e.message ?: "Неизвестная ошибка")
        }


    private fun createRequest(message: String): ChatRequest {

            // Формат запроса для Ollama API
            val systemMessage = """
                Ты полезный AI ассистент. Отвечай на вопросы пользователя четко и по делу.

                ВАЖНО:
                - Отвечай обычным текстом, БЕЗ markdown разметки (не используй ```json, ```, **, и т.д.)
            """.trimIndent()

        return ChatRequest(
            model = MODEL_NAME,
            messages = listOf(
                ChatMessage(
                    role = "system",
                    content = systemMessage
                ),
                ChatMessage(
                    role = "user",
                    content = message
                )
            ),
            stream = false,
            temperature = DEFAULT_TEMPERATURE
        )
    }

    /**
     * Разбор HTTP-ответа:
     * - при 200 парсим успешный JSON и маппим в ChatAnswer
     * - при ошибках читаем тело как текст и возвращаем осмысленное сообщение
     */
    private suspend fun handleResponse(response: HttpResponse): ResultResponse<ChatAnswer> {
        return when (val status = response.status) {
            HttpStatusCode.OK -> {
                try {
                    // DTO под формат ответа HF/OpenAI
                    val dto: OpenAiFormatAnswerResponse = response.body()
                    val chatAnswer = dto.toChatAnswer()
                    // Проверяем что есть хотя бы какой-то текст
                    val hasText = chatAnswer.choices?.firstOrNull()?.message?.content != null ||
                            chatAnswer.generatedText != null
                    if (hasText) {
                        ResultResponse.Success(chatAnswer)
                    } else {
                        ResultResponse.Error("Пустой ответ от модели")
                    }
                } catch (e: Exception) {
                    ResultResponse.Error("Ошибка парсинга ответа: ${e.message}")
                }
            }

            HttpStatusCode.ServiceUnavailable -> {
                val errorText = safeErrorBody(response)
                ResultResponse.Error(
                    "Модель загружается, попробуйте через несколько секунд. $errorText"
                )
            }

            HttpStatusCode.NotFound -> {
                val errorText = safeErrorBody(response)
                ResultResponse.Error(
                    "Модель не найдена. Проверьте название модели: $MODEL_NAME. $errorText"
                )
            }

            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                val errorText = safeErrorBody(response)
                ResultResponse.Error(
                    "Ошибка авторизации при обращении к API. $errorText"
                )
            }

            HttpStatusCode.TooManyRequests -> {
                val errorText = safeErrorBody(response)
                ResultResponse.Error(
                    "Слишком много запросов к модели. Попробуйте позже. $errorText"
                )
            }

            else -> {
                val errorText = safeErrorBody(response)
                ResultResponse.Error(
                    "Неизвестная ошибка (${status.value} ${status.description}). $errorText"
                )
            }
        }
    }

    /**
     * Безопасно читаем тело ошибки как текст
     */
    private suspend fun safeErrorBody(response: HttpResponse): String {
        return try {
            val text = response.bodyAsText()
            if (text.isBlank()) "" else "Детали: $text"
        } catch (e: Exception) {
            ""
        }
    }
}
