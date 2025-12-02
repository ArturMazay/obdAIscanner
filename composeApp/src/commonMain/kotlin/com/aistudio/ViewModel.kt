package com.aistudio

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
    protected val viewModelScope: CoroutineScope
    protected open fun onCleared()
}

