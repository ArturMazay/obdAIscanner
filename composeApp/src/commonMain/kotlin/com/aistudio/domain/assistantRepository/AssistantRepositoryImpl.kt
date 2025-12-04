package com.aistudio.domain.assistantRepository

import com.aistudio.data.networkDataSource.helpers.ResultResponse
import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.ChatMessage as NetworkChatMessage

/**
 * Реализация репозитория AI ассистента
 * Маппинг уже выполнен в DataSource, репозиторий просто преобразует ResultResponse в Result
 */
class AssistantRepositoryImpl(
    private val networkDataSource: AssistantDataSource
) : AssistantRepository {

    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<NetworkChatMessage>
    ): ChatAnswer {
        println("=".repeat(80))
        println("🟡 [AssistantRepositoryImpl] Получен запрос от ViewModel")
        println("🟡 [AssistantRepositoryImpl] Вопрос: $message")
        println("🟡 [AssistantRepositoryImpl] История диалога: ${conversationHistory.size} сообщений")
        conversationHistory.takeLast(6).forEachIndexed { index, msg ->
            println("  [${conversationHistory.size - 6 + index}] ${msg.role}: ${msg.content.take(50)}...")
        }
        println("=".repeat(80))
        
        val result = networkDataSource.sendMessage(message, conversationHistory)
        
        return when (result) {
            is ResultResponse.Success -> {
                println("🟢 [AssistantRepositoryImpl] Успешный ответ от DataSource")
                println("🟢 [AssistantRepositoryImpl] ChatAnswer:")
                println("  - id: ${result.value.id}")
                println("  - model: ${result.value.model}")
                println("  - choices: ${result.value.choices?.size ?: 0}")
                println("  - generatedText: ${result.value.generatedText?.take(100) ?: "null"}...")
                println("=".repeat(80))
                result.value
            }

            is ResultResponse.Error -> {
                println("🔴 [AssistantRepositoryImpl] Ошибка от DataSource: ${result.error}")
                println("=".repeat(80))
                ChatAnswer(error = result.error)
            }
        }
    }
}