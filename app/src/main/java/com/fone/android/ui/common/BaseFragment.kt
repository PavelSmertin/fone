package com.fone.android.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.fone.android.di.Injectable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

open class BaseFragment : Fragment(), Injectable {
    protected val scopeProvider: AndroidLifecycleScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    open fun onBackPressed() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        interceptClick(view)
    }

    private fun interceptClick(view: View) {
        view.setOnClickListener { }
    }
}