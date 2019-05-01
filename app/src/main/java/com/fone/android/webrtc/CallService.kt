package com.fone.android.webrtc

import android.content.Context

class CallService {


    enum class CallState {
        STATE_IDLE, STATE_DIALING, STATE_RINGING, STATE_ANSWERING, STATE_CONNECTED, STATE_BUSY
    }

    companion object {
        fun cancel(ctx: Context) = startService()

        fun decline(ctx: Context) = startService()

        fun localEnd(ctx: Context) = startService()

        private fun startService() {}

    }
}