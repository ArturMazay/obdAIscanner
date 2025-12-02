package com.aistudio

import com.aistudio.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.viewModel


val appModule = module {
    single<AssistantDataSource> {
        AssistantDataSourceImpl(
            httpClient = get(),
            json = get()
        )
    }
    viewModel {
        AssistantViewModel(
            dataSource = get()
        )
    }
}


val ktorClientModule = module {

    single {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            defaultRequest {
                val token = ApiConfig.huggingFaceToken
                // Логируем первые 10 символов токена для отладки (безопасно)
                val tokenPreview = if (token.length > 10) "${token.take(10)}..." else token
                println("🔵 [AppModule] Используется токен: $tokenPreview")
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }
    
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}
