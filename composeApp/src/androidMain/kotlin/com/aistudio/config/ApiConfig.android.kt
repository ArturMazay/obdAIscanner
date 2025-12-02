package com.aistudio.config

import com.aistudio.BuildConfig


actual object ApiConfig {
    actual val huggingFaceToken: String
        get() {
            // 1. Проверяем переменную окружения (работает на всех платформах)
            System.getenv("HF_API_TOKEN")?.let {
                println("🔵 [ApiConfig] Токен получен из переменной окружения HF_API_TOKEN")
                return it
            }
            
            // 2. Используем BuildConfig для Android
            val buildConfigToken = BuildConfig.HF_API_TOKEN
            println("🔵 [ApiConfig] BuildConfig.HF_API_TOKEN: ${if (buildConfigToken.length > 10) "${buildConfigToken.take(10)}..." else buildConfigToken}")
            println("🔵 [ApiConfig] Длина токена из BuildConfig: ${buildConfigToken.length}")
            println("🔵 [ApiConfig] Токен пустой: ${buildConfigToken.isEmpty()}")
            println("🔵 [ApiConfig] Токен равен placeholder: ${buildConfigToken == "YOUR_HUGGING_FACE_TOKEN_HERE"}")
            
            if (buildConfigToken.isNotEmpty() && buildConfigToken != "YOUR_HUGGING_FACE_TOKEN_HERE") {
                println("🟢 [ApiConfig] Используется токен из BuildConfig")
                return buildConfigToken
            }
            
            // 3. Значение по умолчанию
            println("🔴 [ApiConfig] ВНИМАНИЕ: Используется значение по умолчанию! Токен не установлен!")
            return "YOUR_HUGGING_FACE_TOKEN_HERE"
        }
}


