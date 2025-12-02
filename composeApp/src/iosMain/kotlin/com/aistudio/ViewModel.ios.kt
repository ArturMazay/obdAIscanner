package com.aistudio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual abstract class ViewModel {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    actual val viewModelScope: CoroutineScope
        get() = scope
    
    actual open fun onCleared() {
        job.cancel()
    }
}

