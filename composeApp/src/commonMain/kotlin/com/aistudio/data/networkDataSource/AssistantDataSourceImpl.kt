package com.aistudio.data.networkDataSource

import com.aistudio.data.networkDataSource.helpers.NetworkToDomainMapper.toChatAnswer
import com.aistudio.data.networkDataSource.helpers.ResultResponse
import com.aistudio.domain.assistantRepository.AssistantDataSource
import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.ChatMessage
import com.aistudio.data.networkDataSource.model.OpenAiFormatAnswerResponse
import com.aistudio.data.networkDataSource.model.OpenAiFormatBodyRequest
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

    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<com.aistudio.data.networkDataSource.model.ChatMessage>
    ): ResultResponse<ChatAnswer> = try {
            val request = createRequest(message, conversationHistory)
            
            // Логируем запрос
            println("=".repeat(80))
            println("🔵 [AssistantDataSourceImpl] Отправка запроса к API")
            println("🔵 [AssistantDataSourceImpl] URL: $API_URL")
            println("🔵 [AssistantDataSourceImpl] Модель: $MODEL_NAME")
            println("🔵 [AssistantDataSourceImpl] Вопрос пользователя: $message")
            println("🔵 [AssistantDataSourceImpl] История диалога: ${conversationHistory.size} сообщений")
            println("=".repeat(80))
            
            val response: HttpResponse = httpClient.post(API_URL) { setBody(request) }
            
            println("🔵 [AssistantDataSourceImpl] Получен ответ. Статус: ${response.status}")
            
            // Читаем и логируем сырой ответ ДО парсинга
            val rawResponse = try {
                response.bodyAsText()
            } catch (e: Exception) {
                println("🟡 [AssistantDataSourceImpl] Не удалось прочитать сырой ответ: ${e.message}")
                null
            }
            
            // Логируем сырой ответ
            rawResponse?.let {
                println("=".repeat(80))
                println("🔵 [AssistantDataSourceImpl] Сырой ответ от API (${it.length} символов):")
                println("=".repeat(80))
                println(it)
                println("=".repeat(80))
            }
            
            handleResponse(response, rawResponse)

        } catch (e: Exception) {
            println("🔴 [AssistantDataSourceImpl] Исключение при отправке запроса: ${e.message}")
            e.printStackTrace()
            ResultResponse.Error(e.message ?: "Неизвестная ошибка")
        }


    private fun createRequest(
        message: String,
        conversationHistory: List<ChatMessage>
    ): OpenAiFormatBodyRequest {

        // Подсчитываем количество вопросов, заданных AI в истории
        // Вопрос - это сообщение assistant, которое заканчивается на "?" или содержит вопросительные слова
        val questionsAsked = conversationHistory
            .filter { it.role == "assistant" }
            .count { msg ->
                val content = msg.content.trim()
                content.endsWith("?") || 
                content.contains("?") ||
                content.contains("вопрос", ignoreCase = true) ||
                content.contains("какой", ignoreCase = true) ||
                content.contains("какая", ignoreCase = true) ||
                content.contains("какое", ignoreCase = true) ||
                content.contains("как", ignoreCase = true) ||
                content.contains("что", ignoreCase = true) ||
                content.contains("где", ignoreCase = true) ||
                content.contains("когда", ignoreCase = true) ||
                content.contains("почему", ignoreCase = true)
            }
        
        println("🔵 [AssistantDataSourceImpl] Подсчет вопросов:")
        println("  - Всего сообщений assistant в истории: ${conversationHistory.count { it.role == "assistant" }}")
        println("  - Вопросов задано: $questionsAsked из 5")
        
        val systemMessage = """
Ты — полезный AI-ассистент, специализирующийся на помощи с ремонтом и обслуживанием автомобилей.

СТРОГИЙ АЛГОРИТМ РАБОТЫ (ОБЯЗАТЕЛЬНО СЛЕДУЙ ЕМУ):

ШАГ 1: ПЕРВЫЙ ВОПРОС ПОЛЬЗОВАТЕЛЯ
Когда пользователь задает первый вопрос (например, "как поменять колесо"):
- Ты НЕ даешь ответ сразу
- Ты извлекаешь общий контекст
- Ты задаешь ПЕРВЫЙ уточняющий вопрос

ШАГ 2: УТОЧНЯЮЩИЕ ВОПРОСЫ (ОБЯЗАТЕЛЬНО 5 ВОПРОСОВ)
Ты ОБЯЗАН задать ровно 5 уточняющих вопросов подряд:
- После каждого ответа пользователя задавай СЛЕДУЮЩИЙ вопрос
- Каждый вопрос должен быть конкретным и помогать понять проблему лучше
- НЕ давай финальный ответ до тех пор, пока не задашь все 5 вопросов
- Вопросы должны быть разными и уточнять разные аспекты проблемы

ШАГ 3: ФИНАЛЬНЫЙ ОТВЕТ (ТОЛЬКО ПОСЛЕ 5 ВОПРОСОВ)
После получения ответа на 5-й вопрос:
- СРАЗУ дай развернутый, практичный ответ
- Финальный ответ должен быть структурированным, пошаговым
- Учитывай ВСЕ ответы пользователя на твои вопросы
- Давай конкретные инструкции, советы и решения

ПРАВИЛА:
- Отвечай четко, по делу, практично
- Используй простой язык, понятный обычному человеку
- Отвечай обычным текстом, БЕЗ markdown разметки (не используй ```json, ```, **, и т.д.)
- Уточняющие вопросы должны быть конкретными и полезными
- Каждый вопрос должен заканчиваться знаком "?"

ТЕКУЩИЙ СТАТУС:
- Задано вопросов: $questionsAsked из 5

ТВОЕ ДЕЙСТВИЕ СЕЙЧАС:
${if (questionsAsked < 5) {
    "ЗАДАЙ СЛЕДУЮЩИЙ УТОЧНЯЮЩИЙ ВОПРОС (вопрос номер ${questionsAsked + 1} из 5). НЕ давай финальный ответ!"
} else {
    "ДАЙ ФИНАЛЬНЫЙ РАЗВЕРНУТЫЙ ОТВЕТ на основе всех ответов пользователя. НЕ задавай больше вопросов!"
}}

ПРИМЕРЫ УТОЧНЯЮЩИХ ВОПРОСОВ:
1. "Какая именно проблема у автомобиля?"
2. "Какой тип автомобиля (марка, модель)?"
3. "Когда это началось происходить?"
4. "Есть ли какие-то звуки или другие симптомы?"
5. "Какой у вас опыт в ремонте автомобилей?"

ВАЖНО: Строго следуй алгоритму. Если задано меньше 5 вопросов — задавай вопросы. Если задано 5 вопросов — дай финальный ответ.
""".trimIndent()

        // Собираем все сообщения: system + история + новое сообщение
        val allMessages = mutableListOf<ChatMessage>().apply {
            add(ChatMessage(role = "system", content = systemMessage))
            addAll(conversationHistory)
            add(ChatMessage(role = "user", content = message))
        }

        return OpenAiFormatBodyRequest(
            model = MODEL_NAME,
            messages = allMessages,
            stream = false,
            temperature = DEFAULT_TEMPERATURE
        )
    }

    /**
     * Разбор HTTP-ответа:
     * - при 200 парсим успешный JSON и маппим в ChatAnswer
     * - при ошибках читаем тело как текст и возвращаем осмысленное сообщение
     */
    private suspend fun handleResponse(response: HttpResponse, rawResponse: String? = null): ResultResponse<ChatAnswer> {
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
