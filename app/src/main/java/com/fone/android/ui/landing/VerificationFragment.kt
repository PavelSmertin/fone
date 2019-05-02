package com.fone.android.ui.landing

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.fone.android.Constants
import com.fone.android.Constants.KEYS
import com.fone.android.FoneApplication
import com.fone.android.R
import com.fone.android.extension.defaultSharedPreferences
import com.fone.android.extension.nowInUtc
import com.fone.android.extension.putBoolean
import com.fone.android.extension.vibrate
import com.fone.android.ui.common.BaseFragment
import com.fone.android.ui.landing.LandingActivity.Companion.ARGS_PIN
import com.fone.android.ui.landing.MobileFragment.Companion.ARGS_PHONE_NUM
import com.fone.android.util.Session
import com.fone.android.vo.Account
import com.fone.android.vo.toUser
import com.fone.android.widget.Keyboard
import com.fone.android.widget.VerificationCodeView
import kotlinx.android.synthetic.main.fragment_verification.*
import javax.inject.Inject

class VerificationFragment : BaseFragment() {

    companion object {
        const val TAG: String = "VerificationFragment"
        private const val ARGS_ID = "args_id"

        fun newInstance(id: String, phoneNum: String, pin: String? = null): VerificationFragment {
            val verificationFragment = VerificationFragment()
            val b = bundleOf(
                ARGS_ID to id,
                ARGS_PHONE_NUM to phoneNum,
                ARGS_PIN to pin
            )
            verificationFragment.arguments = b
            return verificationFragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mobileViewModel: MobileViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MobileViewModel::class.java)
    }

    private var mCountDownTimer: CountDownTimer? = null
    private lateinit var account: Account

    private val pin: String? by lazy {
        arguments!!.getString(ARGS_PIN)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_verification, container, false) as ViewGroup
        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_iv.setOnClickListener { activity?.onBackPressed() }
        pin_verification_view.setOnCodeEnteredListener(mPinVerificationListener)
        pin_verification_title_tv.text =
            getString(R.string.landing_validation_title, arguments!!.getString(ARGS_PHONE_NUM))
        verification_left_bottom_tv.setOnClickListener { sendVerification() }
        verification_keyboard.setKeyboardKeys(KEYS)
        verification_keyboard.setOnClickKeyboardListener(mKeyboardListener)
        verification_cover.isClickable = true
        verification_next_fab.setOnClickListener { handleLogin() }

        startCountDown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCountDownTimer?.cancel()
    }

    @SuppressLint("CheckResult")
    private fun handleLogin() {
        verification_next_fab.hide()
        verification_cover.visibility = GONE

        var account = Account(
            "1",
            "1",
            "default",
            "0000-0000-0000-0000",
            "-1",
            "Твой батя",
            "https://placeimg.com/140/140/any",
            "123",
            null,
            "-1",
            "0000",
            0,
            "-1",
            "-1",
            nowInUtc(),
            "empty",
            true,
            "empty"
        )

        Session.storeAccount(account)
        Session.storeToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NDc4MzIxODA1ODY2NzYyMjUiLCJleHAiOjE1NTc5NDc2MTd9.ROFRBEX9UID95-B8-uXTOAZIkr67Nhp9LeHOeHeLApE")
        verification_keyboard.animate().translationY(300f).start()

        FoneApplication.get().onlining.set(true)
        //if (account.full_name?.isBlank()!!) {
            defaultSharedPreferences.putBoolean(Constants.Account.PREF_SET_NAME, true)
            mobileViewModel.insertUser(account.toUser())
            mobileViewModel.initialConversation()
            InitializeActivity.showLoading(context!!)
        //}
        activity?.finish()
    }

    private fun sendVerification() {
        pin_verification_view?.clear()
        startCountDown()
    }

    private fun startCountDown() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(60000, 1000) {

            override fun onTick(l: Long) {
                if (verification_left_bottom_tv != null)
                    verification_left_bottom_tv.text = getString(R.string.landing_resend_code_disable, l / 1000)
            }

            override fun onFinish() {
                resetCountDown()
            }
        }
        mCountDownTimer?.start()
        verification_left_bottom_tv.isEnabled = false
        context?.getColor(R.color.colorGray)?.let { verification_left_bottom_tv.setTextColor(it) }
    }

    private fun resetCountDown() {
        if (verification_left_bottom_tv != null) {
            verification_left_bottom_tv.setText(R.string.landing_resend_code_enable)
            verification_left_bottom_tv.isEnabled = true
            context?.getColor(R.color.colorBlue)?.let { verification_left_bottom_tv.setTextColor(it) }
        }
    }

    private val mKeyboardListener: Keyboard.OnClickKeyboardListener = object : Keyboard.OnClickKeyboardListener {
        override fun onKeyClick(position: Int, value: String) {
            context?.vibrate(longArrayOf(0, 30))
            if (position == 11) {
                pin_verification_view?.delete()
            } else {
                pin_verification_view?.append(value)
            }
        }

        override fun onLongClick(position: Int, value: String) {
            context?.vibrate(longArrayOf(0, 30))
            if (position == 11) {
                pin_verification_view?.clear()
            } else {
                pin_verification_view?.append(value)
            }
        }
    }

    private val mPinVerificationListener: VerificationCodeView.OnCodeEnteredListener = object :
        VerificationCodeView.OnCodeEnteredListener {
        override fun onCodeEntered(code: String) {
            pin_verification_tip_tv.visibility = INVISIBLE
            if (code.isEmpty() || code.length != pin_verification_view.count) {
                return
            }
            handleLogin()
        }
    }
}