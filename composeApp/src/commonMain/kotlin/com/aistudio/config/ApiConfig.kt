package com.aistudio.config

object ApiConfig {
    /**
     * Hugging Face API Token
     * 
     * Установите токен одним из способов:
     * 1. Через переменную окружения: export HF_API_TOKEN=your_token_here
     * 2. Через local.properties: hf.api.token=your_token_here (только для Android/JVM)
     * 
     * Получите токен на: https://huggingface.co/settings/tokens
     */
    val huggingFaceToken: String
        get() {
            // 1. Проверяем переменную окружения (работает на всех платформах)
            System.getenv("HF_API_TOKEN")?.let { return it }
            
            // 2. Значение по умолчанию - замените на свой токен локально
            // ВАЖНО: НЕ коммитьте реальный токен в репозиторий!
            return "YOUR_HUGGING_FACE_TOKEN_HERE"
        }
}
