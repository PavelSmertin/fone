package com.fone.android.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.fone.android.FoneApplication
import com.fone.android.R
import com.fone.android.extension.defaultSharedPreferences
import com.fone.android.extension.putBoolean
import com.fone.android.ui.common.BaseFragment
import com.fone.android.ui.home.MainActivity
import javax.inject.Inject

class LoadingFragment : BaseFragment() {

    companion object {
        const val TAG: String = "LoadingFragment"
        const val IS_LOADED = "is_loaded"
        fun newInstance() = LoadingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_loading, container, false)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireContext().defaultSharedPreferences.putBoolean(IS_LOADED, false)
        FoneApplication.get().onlining.set(true)
        load()
    }

    private fun load() {
        context!!.defaultSharedPreferences.putBoolean(IS_LOADED, true)
        MainActivity.show(context!!)
        activity?.finish()
    }

    private var count = 2
}