package com.fone.android.api

class LocalJobException : RuntimeException() {

    fun shouldRetry() = true
}