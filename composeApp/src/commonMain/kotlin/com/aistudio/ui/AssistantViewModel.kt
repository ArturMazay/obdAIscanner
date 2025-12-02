package com.aistudio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.domain.assistantRepository.AssistantRepository
import com.aistudio.domain.model.ChatAnswer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI состояние для экрана AI ассистента
 */
data class AssistantUiState(
    val userInput: String = "",
    val answer: ChatAnswer? = null,
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
        val message = _uiState.value.userInput.trim()
        if (message.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                answer = null
            )

            val answer = repository.sendMessage(message)

            _uiState.value = _uiState.value.copy(
                answer = answer,
                isLoading = false,
                userInput = "",
                error = answer.error
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
