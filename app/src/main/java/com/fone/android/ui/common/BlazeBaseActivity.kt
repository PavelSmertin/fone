package com.fone.android.ui.common

import com.fone.android.FoneApplication
import com.fone.android.job.BlazeMessageService.Companion.ACTION_ACTIVITY_PAUSE
import com.fone.android.job.BlazeMessageService.Companion.ACTION_ACTIVITY_RESUME
import com.fone.android.job.BlazeMessageService.Companion.startService

import com.fone.android.util.Session

abstract class BlazeBaseActivity : BaseActivity() {

    override fun onResume() {
        super.onResume()
        if (Session.checkToken() && FoneApplication.get().onlining.get()) {
            startService(this, ACTION_ACTIVITY_RESUME)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Session.checkToken() && FoneApplication.get().onlining.get()) {
            startService(this, ACTION_ACTIVITY_PAUSE)
        }
    }
}