package com.aistudio.domain.assistantRepository

import com.aistudio.data.networkDataSource.helpers.ResultResponse
import com.aistudio.domain.model.ChatAnswer

/**
 * Реализация репозитория AI ассистента
 * Маппинг уже выполнен в DataSource, репозиторий просто преобразует ResultResponse в Result
 */
class AssistantRepositoryImpl(
    private val networkDataSource: AssistantDataSource
) : AssistantRepository {

    override suspend fun sendMessage(message: String): ChatAnswer =
        when (val result = networkDataSource.sendMessage(message)) {
            is ResultResponse.Success -> {
                result.value
            }

            is ResultResponse.Error -> ChatAnswer(error = result.error)
        }
}