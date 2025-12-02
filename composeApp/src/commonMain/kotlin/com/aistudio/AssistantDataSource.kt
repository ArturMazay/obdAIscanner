package com.aistudio

import com.aistudio.model.ChatResponse

interface AssistantDataSource {
    suspend fun sendMessage(message: String): Result<String>
}