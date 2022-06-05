package com.bugs.posisiin01.tools

sealed class DataState<T> {
    class Loading<T> : DataState<T>()
    class Success<T>(val data: T) : DataState<T>()
    data class Failed<T>(val info: String) : DataState<T>()

    companion object {
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success<T>(data)
        fun <T> failed(info: String) = Failed<T>(info)
    }
}
