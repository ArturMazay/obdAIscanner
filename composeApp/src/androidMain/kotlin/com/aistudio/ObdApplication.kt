package com.aistudio

import android.app.Application
import com.aistudio.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ObdApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@ObdApplication)
            androidLogger()
        }
    }
}