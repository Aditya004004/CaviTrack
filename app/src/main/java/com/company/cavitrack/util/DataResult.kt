package com.company.cavitrack.util

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val message: String, val code: Int? = null) : DataResult<Nothing>()
}
