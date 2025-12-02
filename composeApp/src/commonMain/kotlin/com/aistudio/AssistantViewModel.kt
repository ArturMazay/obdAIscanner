package com.aistudio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssistantUiState(
    val userInput: String = "",
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AssistantViewModel(
    private val dataSource: AssistantDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun updateUserInput(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input, error = null)
    }

    fun sendMessage() {
        val message = _uiState.value.userInput.trim()
        if (message.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                response = ""
            )

            dataSource.sendMessage(message)
                .onSuccess { assistantMessage ->
                    _uiState.value = _uiState.value.copy(
                        response = assistantMessage,
                        isLoading = false,
                        userInput = ""
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Произошла ошибка"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}