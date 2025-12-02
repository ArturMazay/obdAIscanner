package com.aistudio.di

import com.aistudio.appModule
import com.aistudio.ktorClientModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin(config : KoinAppDeclaration? = null){
    startKoin {
        printLogger()
        includes(config)
        modules(appModule,ktorClientModule)
    }
}