package com.aistudio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.domain.assistantRepository.AssistantRepository
import com.aistudio.domain.model.ChatAnswer
import com.aistudio.ui.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * UI состояние для экрана AI ассистента
 */
data class AssistantUiState(
    val userInput: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel для AI ассистента
 * Работает с доменными моделями через Repository
 */
class AssistantViewModel(
    private val repository: AssistantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState

    fun updateUserInput(input: String) {
        _uiState.value = _uiState.value.copy(
            userInput = input,
            error = null
        )
    }

    fun sendMessage() {
        val messageText = _uiState.value.userInput.trim()
        if (messageText.isEmpty()) return

        // Логируем вопрос пользователя
        println("=".repeat(80))
        println("🔵 [AssistantViewModel] Вопрос пользователя:")
        println("=".repeat(80))
        println(messageText)
        println("=".repeat(80))

        // Добавляем сообщение пользователя в историю
        val userMessage = ChatMessage(
            id = generateMessageId(),
            text = messageText,
            isUser = true
        )
        
        val currentMessages = _uiState.value.messages + userMessage
        
        _uiState.value = _uiState.value.copy(
            messages = currentMessages,
            userInput = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            // Преобразуем историю UI сообщений в формат для API
            // Строим историю: чередуем user и assistant сообщения
            val userMessages = _uiState.value.messages.filter { it.isUser }
            val aiMessages = _uiState.value.messages.filter { !it.isUser }
            
            val conversationHistory = mutableListOf<com.aistudio.data.networkDataSource.model.ChatMessage>()
            val maxPairs = minOf(userMessages.size, aiMessages.size)
            for (i in 0 until maxPairs) {
                conversationHistory.add(
                    com.aistudio.data.networkDataSource.model.ChatMessage(
                        role = "user",
                        content = userMessages[i].text
                    )
                )
                conversationHistory.add(
                    com.aistudio.data.networkDataSource.model.ChatMessage(
                        role = "assistant",
                        content = aiMessages[i].text
                    )
                )
            }
            // Ограничиваем историю последними 20 сообщениями (10 пар)
            val limitedHistory = conversationHistory.takeLast(20)
            
            val result = repository.sendMessage(messageText, limitedHistory)
            
            // Логируем сырой ответ от модели
            println("=".repeat(80))
            println("🟢 [AssistantViewModel] Сырой ответ от модели (ChatAnswer):")
            println("=".repeat(80))
            println("ID: ${result.id}")
            println("Model: ${result.model}")
            println("ObjectType: ${result.objectType}")
            println("Created: ${result.created}")
            println("Choices count: ${result.choices?.size ?: 0}")
            result.choices?.forEachIndexed { index, choice ->
                println("  Choice[$index]:")
                println("    - index: ${choice.index}")
                println("    - role: ${choice.message.role}")
                println("    - content: ${choice.message.content}")
                println("    - finish_reason: ${choice.finish_reason}")
            }
            println("GeneratedText: ${result.generatedText}")
            println("Usage: ${result.usage}")
            println("SystemFingerprint: ${result.systemFingerprint}")
            println("Error: ${result.error}")
            println("IsLoading: ${result.isLoading}")
            println("=".repeat(80))
            
            // Извлекаем текст ответа из ChatAnswer
            val responseText = when {
                result.choices?.firstOrNull()?.message?.content != null -> {
                    result.choices.first().message.content
                }
                result.generatedText != null -> {
                    result.generatedText
                }
                result.error != null -> {
                    "Ошибка: ${result.error}"
                }
                else -> {
                    "Пустой ответ от модели"
                }
            }
            
            // Добавляем ответ AI в историю
            val aiMessage = ChatMessage(
                id = generateMessageId(),
                text = responseText,
                isUser = false
            )
            
            _uiState.value = _uiState.value.copy(
                messages = currentMessages + aiMessage,
                isLoading = false,
                error = result.error
            )
        }
    }
    
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            error = null
        )
    }
}
