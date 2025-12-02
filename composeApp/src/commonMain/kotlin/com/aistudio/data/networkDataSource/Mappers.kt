package com.aistudio.data.networkDataSource

import com.aistudio.domain.model.ChatAnswer
import com.aistudio.data.networkDataSource.model.ChatAnswerResponse

/**
 * Мапперы для преобразования сетевых моделей в доменные
 */

    /**
     * Преобразует ChatAnswerResponse (сетевая модель) в ChatAnswer (доменная модель)
     */
    fun ChatAnswerResponse.toChatAnswer(): ChatAnswer {
        return ChatAnswer(
            id = this.id,
            choices = this.choices,
            created = this.created,
            model = this.model,
            generatedText = this.generated_text,
            error = null,
            isLoading = false
        )
    }


