package com.aistudio.data.networkDataSource

import com.aistudio.data.networkDataSource.helpers.NetworkToDomainMapper.toChatAnswer
import com.aistudio.data.networkDataSource.helpers.ResultResponse
import com.aistudio.domain.assistantRepository.AssistantDataSource
import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.ChatMessage
import com.aistudio.data.networkDataSource.model.Choice
import com.aistudio.data.networkDataSource.model.OllamaFormatBodyRequest
import com.aistudio.data.networkDataSource.model.OllamaFormatAnswerResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Реализация источника данных для Ollama API
 * Выполняет сетевые запросы и маппинг в доменные модели
 * Обрабатывает streaming ответы в формате NDJSON (newline-delimited JSON)
 */
class AssistantOllamaDataSourceImpl(
    private val httpClient: HttpClient
) : AssistantDataSource {

    companion object {
        private const val MODEL_NAME = "deepseek-coder:1.3b-instruct"
        private const val API_URL = "http://92.51.45.127:11434/api/generate"
        private const val DEFAULT_TEMPERATURE = 0.7

        // Json парсер для парсинга сырого ответа (NDJSON формат)
        private val jsonParser = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    override suspend fun sendMessage(message: String): ResultResponse<ChatAnswer> = try {
        val request = createRequest(message)
        val response: HttpResponse = httpClient.post(API_URL) { setBody(request) }
        handleResponse(response)

    } catch (e: Exception) {
        println("🔴 [AssistantOllamaDataSourceImpl] Исключение при отправке запроса: ${e.message}")
        e.printStackTrace()
        ResultResponse.Error(e.message ?: "Неизвестная ошибка")
    }

    private fun createRequest(message: String): OllamaFormatBodyRequest {
        val systemMessage = """
            Ты полезный AI ассистент. Отвечай на вопросы пользователя четко и по делу.

            ВАЖНО:
            - Отвечай обычным текстом, БЕЗ markdown разметки (не используй ```json, ```, **, и т.д.)
        """.trimIndent()

        return OllamaFormatBodyRequest(
            model = MODEL_NAME,
            prompt = message,
            stream = false,
            temperature = DEFAULT_TEMPERATURE,
            system = systemMessage
        )
    }

    /**
     * Разбор HTTP-ответа:
     * - при 200 парсим NDJSON формат (streaming ответ) и маппим в ChatAnswer
     * - при ошибках читаем тело как текст и возвращаем осмысленное сообщение
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun handleResponse(response: HttpResponse): ResultResponse<ChatAnswer> {
        return when (val status = response.status) {
            HttpStatusCode.OK -> {
                try {
                    // Читаем сырой ответ
                    val responseText = response.bodyAsText()

                    // Ollama возвращает streaming ответ в формате NDJSON (newline-delimited JSON)
                    // Каждая строка - отдельный JSON объект
                    val lines = responseText.trim().lines().filter { it.isNotBlank() }

                    // Собираем все части ответа
                    val fullResponse = StringBuilder()
                    var finalResponse: OllamaFormatAnswerResponse? = null
                    var modelName: String? = null
                    var createdAt: String? = null

                    // Парсим каждую строку отдельно
                    lines.forEachIndexed { index, line ->
                        try {
                            val ollamaChunk: OllamaFormatAnswerResponse = jsonParser.decodeFromString(line)

                            // Сохраняем метаданные из первого чанка
                            if (index == 0) {
                                modelName = ollamaChunk.model
                                createdAt = ollamaChunk.created_at
                            }

                            // Собираем текст ответа
                            ollamaChunk.response?.let { fullResponse.append(it) }

                            // Сохраняем последний чанк (он содержит финальный статус)
                            if (ollamaChunk.done) {
                                finalResponse = ollamaChunk
                            }
                        } catch (e: Exception) {
                            println("🟡 [AssistantOllamaDataSourceImpl] Ошибка парсинга чанка [$index]: ${e.message}")
                        }
                    }

                    // Используем последний чанк для получения финального статуса
                    val ollamaResponse = finalResponse ?: run {
                        // Если не нашли финальный чанк, пробуем распарсить последнюю строку
                        if (lines.isNotEmpty()) {
                            try {
                                jsonParser.decodeFromString<OllamaFormatAnswerResponse>(lines.last())
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                    }

                    if (ollamaResponse == null) {
                        return ResultResponse.Error("Не удалось обработать ответ от Ollama API")
                    }

                    // Проверяем статус модели
                    when (ollamaResponse.done_reason) {
                        "load" -> {
                            return ResultResponse.Error("Модель еще загружается. Подождите несколько секунд и попробуйте снова.")
                        }
                        "error" -> {
                            return ResultResponse.Error("Ошибка при генерации ответа моделью")
                        }
                    }

                    // Проверяем наличие ответа
                    val responseText_ollama = fullResponse.toString()
                    if (responseText_ollama.isBlank()) {
                        return ResultResponse.Error("Пустой ответ от модели")
                    }

                    // Преобразуем Ollama ответ в ChatAnswer через маппер
                    val chatAnswer = ollamaResponse.toChatAnswer(
                        fullText = responseText_ollama,
                        modelName = modelName ?: ollamaResponse.model,
                        createdAt = createdAt
                    )

                    ResultResponse.Success(chatAnswer)

                } catch (e: Exception) {
                    println("🔴 [AssistantOllamaDataSourceImpl] Ошибка парсинга ответа: ${e.message}")
                    e.printStackTrace()
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

            HttpStatusCode.BadRequest -> {
                val errorText = safeErrorBody(response)
                ResultResponse.Error(
                    "Неверный запрос к API. Проверьте параметры запроса. $errorText"
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

