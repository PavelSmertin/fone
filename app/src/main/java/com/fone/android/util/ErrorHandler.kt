package com.fone.android.util

import android.content.Context
import com.fone.android.FoneApplication
import com.fone.android.R
import com.fone.android.api.ClientErrorException
import com.fone.android.api.NetworkException
import com.fone.android.api.ServerErrorException
import com.fone.android.extension.toast
import org.jetbrains.anko.runOnUiThread
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class ErrorHandler {

    companion object {

        fun handleError(throwable: Throwable) {
            val ctx = FoneApplication.appContext
            ctx.runOnUiThread {
                when (throwable) {
//                    is HttpException -> {
//                        handleErrorCode(throwable.code(), ctx)
//                    }
                    is IOException -> when (throwable) {
                        is SocketTimeoutException -> toast(R.string.error_connection_timeout)
                        is UnknownHostException -> toast(R.string.error_no_connection)
                        is ServerErrorException -> toast(R.string.error_server_5xx)
                        is ClientErrorException -> {
                            handleErrorCode(throwable.code, ctx)
                        }
                        is NetworkException -> toast(R.string.error_no_connection)
                        else -> toast(R.string.error_unknown)
                    }
                    else -> toast(R.string.error_unknown)
                }
            }
        }


        private fun handleErrorCode(code: Int, ctx: Context) {
            ctx.runOnUiThread {
                when (code) {
                    BAD_REQUEST -> {
                    }
                    AUTHENTICATION -> {
                        toast(getString(R.string.error_authentication, AUTHENTICATION))
                        FoneApplication.get().closeAndClear()
                    }
                    FORBIDDEN -> {
                        toast(R.string.error_forbidden)
                    }
                    NOT_FOUND -> {
                        toast(getString(R.string.error_not_found, NOT_FOUND))
                    }
                    TOO_MANY_REQUEST -> {
                        toast(getString(R.string.error_too_many_request, TOO_MANY_REQUEST))
                    }
                    SERVER -> {
                        toast(R.string.error_server_5xx)
                    }
                    TIME_INACCURATE -> { }
                    else -> {
                        toast(getString(R.string.error_unknown_with_code, code))
                    }
                }
            }
        }

        private const val BAD_REQUEST = 400
        const val AUTHENTICATION = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val TOO_MANY_REQUEST = 429
        private const val SERVER = 500
        const val TIME_INACCURATE = 911


    }
}