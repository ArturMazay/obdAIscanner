package com.aistudio

import com.aistudio.model.ApiErrorResponse
import com.aistudio.model.ChatRequest
import com.aistudio.model.ChatResponse
import com.aistudio.model.ChatResponseList
import com.aistudio.model.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AssistantDataSourceImpl(
    private val httpClient: HttpClient,
    private val json: Json
) : AssistantDataSource {

    private val modelName = "deepseek/deepseek-v3.2"
    private val apiUrl = "https://router.huggingface.co/novita/v3/openai/chat/completions"

    override suspend fun sendMessage(message: String): Result<String> {
        println("🔵 [AssistantDataSource] Отправка запроса к API")
        println("🔵 [AssistantDataSource] URL: $apiUrl")
        println("🔵 [AssistantDataSource] Модель: $modelName")
        println("🔵 [AssistantDataSource] Сообщение: $message")
        
        return try {
            // Используем OpenAI совместимый формат для chat completions
            val request = ChatRequest(
                model = modelName,
                messages = listOf(
                    com.aistudio.model.ChatMessage(
                        role = "user",
                        content = message
                    )
                ),
                stream = false,
                temperature = 0.7
            )
            
            println("🔵 [AssistantDataSource] Запрос создан: model=$modelName, messages=${request.messages.size}")
            
            val response = httpClient.post(apiUrl) {
                setBody(request)
            }
            
            println("🔵 [AssistantDataSource] Получен ответ. Статус: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("🟢 [AssistantDataSource] HTTP 200 OK - парсинг ответа...")
                    // Пробуем разные форматы ответа
                    val responseText = try {
                        // Попробуем как OpenAI формат (choices)
                        val chatResponse: ChatResponse = response.body()
                        println("🟢 [AssistantDataSource] Успешно распарсен как ChatResponse")
                        // Проверяем OpenAI формат
                        val openAiText = chatResponse.choices?.firstOrNull()?.message?.content
                        if (openAiText != null) {
                            println("🟢 [AssistantDataSource] Найден ответ в формате OpenAI: ${openAiText.take(100)}...")
                            openAiText
                        } else {
                            // Проверяем inference формат
                            chatResponse.generated_text ?: ""
                        }
                    } catch (e: Exception) {
                        println("🟡 [AssistantDataSource] Ошибка парсинга ChatResponse: ${e.message}")
                        println("🟡 [AssistantDataSource] Пробуем как массив ChatResponse...")
                        try {
                            // Попробуем как массив ChatResponse
                            val chatResponseList: ChatResponseList = response.body()
                            println("🟢 [AssistantDataSource] Успешно распарсен как массив ChatResponse")
                            chatResponseList.firstOrNull()?.generated_text ?: ""
                        } catch (e2: Exception) {
                            println("🟡 [AssistantDataSource] Ошибка парсинга массива ChatResponse: ${e2.message}")
                            println("🟡 [AssistantDataSource] Пробуем как строку...")
                            try {
                                // Попробуем как строку напрямую
                                val rawResponse = response.body<String>()
                                println("🟢 [AssistantDataSource] Получен raw ответ (${rawResponse.length} символов): ${rawResponse.take(200)}...")
                                // Может быть JSON строка, попробуем распарсить
                                if (rawResponse.startsWith("{") || rawResponse.startsWith("[")) {
                                    try {
                                        val jsonResponse = json.parseToJsonElement(rawResponse)
                                        println("🟢 [AssistantDataSource] Успешно распарсен JSON")
                                        val result = when {
                                            jsonResponse.jsonObject.containsKey("generated_text") -> {
                                                jsonResponse.jsonObject["generated_text"]?.jsonPrimitive?.content ?: ""
                                            }
                                            jsonResponse.jsonObject.containsKey("text") -> {
                                                jsonResponse.jsonObject["text"]?.jsonPrimitive?.content ?: ""
                                            }
                                            jsonResponse.jsonObject.containsKey("output") -> {
                                                jsonResponse.jsonObject["output"]?.jsonPrimitive?.content ?: ""
                                            }
                                            else -> {
                                                println("🟡 [AssistantDataSource] Неизвестный формат JSON, возвращаем raw")
                                                rawResponse
                                            }
                                        }
                                        result
                                    } catch (e: Exception) {
                                        println("🟡 [AssistantDataSource] Ошибка парсинга JSON: ${e.message}")
                                        rawResponse
                                    }
                                } else {
                                    rawResponse
                                }
                            } catch (e3: Exception) {
                                println("🔴 [AssistantDataSource] Ошибка получения raw ответа: ${e3.message}")
                                println("🔴 [AssistantDataSource] Stack trace: ${e3.stackTraceToString()}")
                                "Не удалось распарсить ответ: ${e3.message}"
                            }
                        }
                    }
                    if (responseText.isNotEmpty() && !responseText.startsWith("Не удалось")) {
                        println("🟢 [AssistantDataSource] Успешно получен ответ: ${responseText.take(100)}...")
                        Result.success(responseText)
                    } else {
                        println("🔴 [AssistantDataSource] Пустой или некорректный ответ: $responseText")
                        Result.failure(Exception("Пустой ответ от модели: $responseText"))
                    }
                }
                HttpStatusCode.ServiceUnavailable -> {
                    println("🟡 [AssistantDataSource] HTTP 503 Service Unavailable")
                    // Модель может быть загружена, нужно подождать
                    val errorText = try {
                        response.body<String>()
                    } catch (e: Exception) {
                        println("🔴 [AssistantDataSource] Ошибка получения текста ошибки: ${e.message}")
                        "Модель загружается"
                    }
                    println("🔴 [AssistantDataSource] Текст ошибки: $errorText")
                    Result.failure(Exception("Модель загружается, попробуйте через несколько секунд. $errorText"))
                }
                HttpStatusCode.NotFound -> {
                    println("🔴 [AssistantDataSource] HTTP 404 Not Found")
                    println("🔴 [AssistantDataSource] Модель не найдена: $modelName")
                    Result.failure(Exception("Модель не найдена. Проверьте название модели: $modelName"))
                }
                else -> {
                    println("🔴 [AssistantDataSource] HTTP ${response.status.value} ${response.status.description}")
                    val errorText = try {
                        response.body<String>()
                    } catch (e: Exception) {
                        println("🔴 [AssistantDataSource] Ошибка получения текста ошибки: ${e.message}")
                        null
                    }
                    println("🔴 [AssistantDataSource] Raw текст ошибки: $errorText")
                    
                    // Пробуем разные форматы ошибок
                    val errorMessage = try {
                        // Пробуем новый формат Novita API
                        val apiError: ApiErrorResponse = response.body()
                        println("🔴 [AssistantDataSource] Распарсен ApiErrorResponse: ${apiError.message}")
                        apiError.message
                    } catch (e1: Exception) {
                        println("🔴 [AssistantDataSource] Ошибка парсинга ApiErrorResponse: ${e1.message}")
                        try {
                            // Пробуем старый формат с вложенным error
                            val errorResponse: ErrorResponse = response.body()
                            println("🔴 [AssistantDataSource] Распарсен ErrorResponse: ${errorResponse.error.message}")
                            errorResponse.error.message
                        } catch (e2: Exception) {
                            println("🔴 [AssistantDataSource] Ошибка парсинга ErrorResponse: ${e2.message}")
                            // Используем raw текст или дефолтное сообщение
                            errorText ?: "Ошибка запроса: ${response.status.value} ${response.status.description}"
                        }
                    }
                    println("🔴 [AssistantDataSource] Финальное сообщение об ошибке: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            println("🔴 [AssistantDataSource] Исключение при отправке запроса: ${e.message}")
            println("🔴 [AssistantDataSource] Stack trace: ${e.stackTraceToString()}")
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }
}