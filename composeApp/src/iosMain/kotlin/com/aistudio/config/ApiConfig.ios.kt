package com.aistudio.config

actual object ApiConfig {
    actual val huggingFaceToken: String
        get() {
            // Для iOS переменные окружения недоступны во время выполнения
            // Установите токен здесь напрямую или используйте другой механизм конфигурации
            // ВАЖНО: НЕ коммитьте реальный токен в репозиторий!
            return "YOUR_HUGGING_FACE_TOKEN_HERE"
        }
}

