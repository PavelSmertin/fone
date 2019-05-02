package com.fone.android.ui.landing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.fone.android.Constants
import com.fone.android.R
import com.fone.android.extension.defaultSharedPreferences
import com.fone.android.extension.hideKeyboard
import com.fone.android.extension.putBoolean
import com.fone.android.extension.showKeyboard
import com.fone.android.ui.common.BaseFragment
import com.fone.android.ui.home.MainActivity
import kotlinx.android.synthetic.main.fragment_setup_name.*
import javax.inject.Inject

class SetupNameFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mobileViewModel: MobileViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MobileViewModel::class.java)
    }

    companion object {
        fun newInstance() = SetupNameFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_setup_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name_et.setText(Build.MODEL)
        name_fab.setOnClickListener {
            name_et?.hideKeyboard()
            defaultSharedPreferences.putBoolean(Constants.Account.PREF_SET_NAME, false)
            startActivity(Intent(context, MainActivity::class.java))
            activity?.finish()
        }
        name_et.addTextChangedListener(mWatcher)
        name_cover.isClickable = true

        name_et.post {
            name_et?.requestFocus()
            name_et?.showKeyboard()
        }
    }

    private fun handleEditView(str: String) {
        name_et.setSelection(name_et.text.toString().length)
        if (str.isNotEmpty()) {
            name_fab.visibility = View.VISIBLE
        } else {
            name_fab.visibility = View.INVISIBLE
        }
    }

    private val mWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            handleEditView(s.toString())
        }
    }
}