package com.aistudio.config

import com.aistudio.BuildConfig


actual object ApiConfig {
    actual val huggingFaceToken: String
        get() {
            // 1. Проверяем переменную окружения (работает на всех платформах)
            System.getenv("HF_API_TOKEN")?.let { return it }
            
            // 2. Используем BuildConfig для Android
            if (BuildConfig.HF_API_TOKEN.isNotEmpty()) {
                return BuildConfig.HF_API_TOKEN
            }
            
            // 3. Значение по умолчанию
            return "YOUR_HUGGING_FACE_TOKEN_HERE"
        }
}

