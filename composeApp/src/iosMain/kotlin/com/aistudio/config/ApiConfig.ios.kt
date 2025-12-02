package com.aistudio.config

actual object ApiConfig {
    actual val huggingFaceToken: String
        get() {
            // Для iOS используем только переменную окружения
            System.getenv("HF_API_TOKEN")?.let { return it }
            
            // Значение по умолчанию
            return "YOUR_HUGGING_FACE_TOKEN_HERE"
        }
}

