package com.fone.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fone.android.job.BlazeMessageService
import com.fone.android.job.BlazeMessageService.Companion.ACTION_TO_BACKGROUND

class ExitBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        if (intent.action == ACTION_TO_BACKGROUND) {
            BlazeMessageService.startService(context, ACTION_TO_BACKGROUND)
        }
//        else if (intent.action == CallNotificationBuilder.ACTION_EXIT) {
//            CallService.stopService(context)
//        }
    }
}