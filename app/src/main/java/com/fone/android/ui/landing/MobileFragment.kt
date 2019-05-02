package com.fone.android.ui.landing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.fone.android.R
import com.fone.android.extension.addFragment
import com.fone.android.ui.common.BaseFragment
import com.fone.android.ui.landing.LandingActivity.Companion.ARGS_PIN
import kotlinx.android.synthetic.main.fragment_mobile.*
import javax.inject.Inject

class MobileFragment : BaseFragment() {

    companion object {
        const val TAG: String = "MobileFragment"
        const val ARGS_PHONE_NUM = "args_phone_num"

        fun newInstance(pin: String? = null): MobileFragment = MobileFragment().apply {
            val b = Bundle().apply {
                if (pin != null) {
                    putString(ARGS_PIN, pin)
                }
            }
            arguments = b
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private var pin: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = layoutInflater.inflate(R.layout.fragment_mobile, container, false) as ViewGroup
        return parent
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pin = arguments!!.getString(ARGS_PIN)
        if (pin != null) {
            mobile_title_tv.setText(R.string.landing_enter_new_mobile_number)
        }
        mobile_fab.setOnClickListener { showDialog() }
        mobile_cover.isClickable = true

    }

    override fun onBackPressed(): Boolean {
        if (pin == null) {
            activity?.supportFragmentManager?.popBackStackImmediate()
            return true
        }
        return false
    }

    private fun showDialog() {
        AlertDialog.Builder(context!!, R.style.MixinAlertDialogTheme)
            .setMessage(getString(R.string.landing_invitation_dialog_content,
                 mobile_et.text.toString()))
            .setNegativeButton(R.string.change) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                requestSend()
                dialog.dismiss()
            }
            .show()
    }

    private fun requestSend() {
        if (!isAdded) return
        mobile_fab.show()
        mobile_cover.visibility = VISIBLE

        activity?.addFragment(this@MobileFragment,
            VerificationFragment.newInstance("-1", mobile_et.text.toString(), pin), VerificationFragment.TAG)

    }


}