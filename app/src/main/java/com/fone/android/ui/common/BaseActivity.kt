package com.fone.android.ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), HasSupportFragmentInjector {

    protected val scopeProvider: AndroidLifecycleScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment>? =
        dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        val fragments = supportFragmentManager.fragments
        if (fragments.size > 0) {
            // Make sure there is a BaseFragment handle this event.
            fragments.indices.reversed()
                .map { fragments[it] }
                .filter { it != null && it is BaseFragment && it.onBackPressed() }
                .forEach { return }
        }
        super.onBackPressed()
    }
}