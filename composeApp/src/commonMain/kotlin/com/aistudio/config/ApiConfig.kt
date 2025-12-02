package com.aistudio.config

/**
 * Hugging Face API Token Configuration
 * 
 * Установите токен одним из способов:
 * 1. Через переменную окружения: export HF_API_TOKEN=your_token_here
 * 2. Через local.properties: hf.api.token=your_token_here (только для Android/JVM)
 * 
 * Получите токен на: https://huggingface.co/settings/tokens
 */
expect object ApiConfig {
    val huggingFaceToken: String
}
