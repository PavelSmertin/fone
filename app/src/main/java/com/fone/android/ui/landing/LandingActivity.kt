package com.fone.android.ui.landing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fone.android.R
import com.fone.android.extension.replaceFragment
import com.fone.android.ui.common.BaseActivity


class LandingActivity : BaseActivity() {

    companion object {
        const val ARGS_PIN = "args_pin"

        fun show(context: Context) {
            val intent = Intent(context, LandingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }

        fun show(context: Context, verificationCode: String) {
            val intent = Intent(context, LandingActivity::class.java).apply {
                putExtra(ARGS_PIN, verificationCode)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        val pin = intent.getStringExtra(ARGS_PIN)
        val fragment = MobileFragment.newInstance(pin)
        replaceFragment(fragment, R.id.container)
    }
}