package com.fone.android.api

import java.io.IOException

class NetworkException : IOException() {

    fun shouldRetry(): Boolean {
        return true
    }
}

class WebSocketException : IOException() {
    fun shouldRetry(): Boolean {
        return true
    }
}