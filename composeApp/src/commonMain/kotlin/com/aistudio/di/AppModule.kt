package com.aistudio.di

import com.aistudio.ui.AssistantViewModel
import com.aistudio.config.ApiConfig
import com.aistudio.data.networkDataSource.AssistantDataSourceImpl
import com.aistudio.domain.assistantRepository.AssistantRepository
import com.aistudio.domain.assistantRepository.AssistantRepositoryImpl
import com.aistudio.domain.assistantRepository.AssistantDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Модуль зависимостей приложения
 * Настроена чистая архитектура:
 * DataSource (сетевой слой) -> Repository (доменный слой) -> ViewModel (презентационный слой)
 */
val appModule = module {
    // Data Layer: Сетевой источник данных
    single<AssistantDataSource> { AssistantDataSourceImpl(httpClient = get()) }
    
    // Domain Layer: Репозиторий
    single<AssistantRepository> { AssistantRepositoryImpl(networkDataSource = get())
    }
    
    // Presentation Layer: ViewModel
    single { 
        AssistantViewModel(
            repository = get()
        ) 
    }
}


val ktorClientModule = module {

    single {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 120_000
                socketTimeoutMillis = 120_000
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
