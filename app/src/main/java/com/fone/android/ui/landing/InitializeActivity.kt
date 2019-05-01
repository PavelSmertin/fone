package com.fone.android.ui.landing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fone.android.R
import com.fone.android.extension.replaceFragment
import com.fone.android.ui.common.BaseActivity


class InitializeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        val setName = intent.getBooleanExtra(SET_NAME, false)
        when {
            setName -> replaceFragment(SetupNameFragment.newInstance(), R.id.container)
            else -> replaceFragment(LoadingFragment.newInstance(), R.id.container, LoadingFragment.TAG)
        }
    }

    override fun onBackPressed() {
    }

    companion object {
        const val SET_NAME = "set_name"
        private fun getIntent(context: Context, setName: Boolean): Intent {
            return Intent(context, InitializeActivity::class.java).apply {
                this.putExtra(SET_NAME, setName)
            }
        }

        fun showLoading(context: Context) {
            context.startActivity(getIntent(context, false))
        }

        fun showSetupName(context: Context) {
            context.startActivity(getIntent(context, true))
        }
    }
}