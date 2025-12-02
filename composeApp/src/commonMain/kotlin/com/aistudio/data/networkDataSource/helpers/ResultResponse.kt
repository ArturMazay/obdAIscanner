package com.aistudio.data.networkDataSource.helpers

sealed class ResultResponse<out T> {
    data class Success<out T>(val value: T) : ResultResponse<T>()
    data class Error(val error: String?) : ResultResponse<Nothing>()
}