package com.fone.android.ui.common

abstract class BlazeBaseActivity : BaseActivity() {

    override fun onResume() {
        super.onResume()
//        if (Session.checkToken() && FoneApplication.get().onlining.get()) {
//            startService(this, ACTION_ACTIVITY_RESUME)
//        }
    }

    override fun onPause() {
        super.onPause()
//        if (Session.checkToken() && FoneApplication.get().onlining.get()) {
//            startService(this, ACTION_ACTIVITY_PAUSE)
//        }
    }
}