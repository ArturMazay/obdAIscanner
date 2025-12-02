package com.aistudio.config

import java.util.Properties
import java.io.File

actual object ApiConfig {
    actual val huggingFaceToken: String
        get() {
            // 1. Проверяем переменную окружения
            System.getenv("HF_API_TOKEN")?.let { return it }
            
            // 2. Читаем из local.properties для JVM
            val localPropertiesFile = File("local.properties")
            if (localPropertiesFile.exists()) {
                val properties = Properties()
                localPropertiesFile.inputStream().use { properties.load(it) }
                properties.getProperty("hf.api.token")?.let { return it }
            }
            
            // 3. Значение по умолчанию
            return "YOUR_HUGGING_FACE_TOKEN_HERE"
        }
}

