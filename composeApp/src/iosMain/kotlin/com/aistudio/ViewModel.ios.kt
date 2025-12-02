package com.aistudio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

actual abstract class ViewModel {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)
    
    actual val viewModelScope: CoroutineScope
        get() = scope
    
    actual open fun onCleared() {
        job.cancel()
    }
}

