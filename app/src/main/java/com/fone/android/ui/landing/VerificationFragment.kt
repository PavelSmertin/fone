package com.fone.android.ui.landing

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.fone.android.Constants.KEYS
import com.fone.android.R
import com.fone.android.api.FoneResponse
import com.fone.android.extension.vibrate
import com.fone.android.ui.common.BaseFragment
import com.fone.android.ui.landing.LandingActivity.Companion.ARGS_PIN
import com.fone.android.ui.landing.MobileFragment.Companion.ARGS_PHONE_NUM
import com.fone.android.util.ErrorHandler
import com.fone.android.vo.Account
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
//    @Inject
//    lateinit var jobManager: MixinJobManager
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
        verification_next_fab.setOnClickListener { handlePinVerification() }

        startCountDown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCountDownTimer?.cancel()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private fun handlePinVerification() {
        if (pin == null) {
            handleLogin()
        } else {
            handlePhoneModification()
        }
    }

    private fun handlePhoneModification() {
        showLoading()
//        mobileViewModel.changePhone(arguments!!.getString(ARGS_ID)!!, pin_verification_view.code(), pin = pin!!)
//            .autoDisposable(scopeProvider).subscribe({ r: FoneResponse<Account> ->
//                verification_next_fab.hide()
//                verification_cover.visibility = GONE
//                if (!r.isSuccess) {
//                    handleFailure(r)
//                    return@subscribe
//                }
//                doAsync {
//                    val a = Session.getAccount()
//                    a?.let {
//                        val phone = arguments!!.getString(ARGS_PHONE_NUM) ?: return@doAsync
//                        mobileViewModel.updatePhone(a.userId, phone)
//                        a.phone = phone
//                        Session.storeAccount(a)
//                    }
//                    uiThread {
//                        alert(getString(R.string.change_phone_success)) {
//                            yesButton { dialog ->
//                                dialog.dismiss()
//                                activity?.finish()
//                            }
//                        }.show()
//                    }
//                }
//            }, { t: Throwable ->
//                handleError(t)
//            })
    }

    @SuppressLint("CheckResult")
    private fun handleLogin() {
        showLoading()

//        val registrationId = CryptoPreference.getLocalRegistrationId(context!!)
//        val sessionKey = generateRSAKeyPair()
//        val sessionSecret = Base64.encodeBytes(sessionKey.getPublicKey())
//        val accountRequest = AccountRequest(pin_verification_view.code(),
//            registration_id = registrationId,
//            purpose = VerificationPurpose.SESSION.name,
//            pin = pin,
//            session_secret = sessionSecret)
//        mobileViewModel.create(arguments!!.getString(ARGS_ID)!!, accountRequest)
//            .autoDisposable(scopeProvider).subscribe({ r: FoneResponse<Account> ->
//                if (!isAdded) {
//                    return@subscribe
//                }
//                verification_next_fab.hide()
//                verification_cover.visibility = GONE
//                if (!r.isSuccess) {
//                    handleFailure(r)
//                    return@subscribe
//                }
//
//                account = r.data!!
//                if (account.code_id.isNotEmpty()) {
//                    GlobalScope.launch(SINGLE_DB_THREAD) {
//                        val p = Point()
//                        val ctx = FoneApplication.appContext
//                        ctx.windowManager.defaultDisplay?.getSize(p)
//                        val size = minOf(p.x, p.y)
//                        val b = account.code_url.generateQRCode(size)
//                        b?.saveQRCode(ctx, account.userId)
//                    }
//                }
//                Session.storeAccount(account)
//                Session.storeToken(sessionKey.getPrivateKeyPem())
//                val key = rsaDecrypt(sessionKey.private, account.session_id, account.pin_token)
//                Session.storePinToken(key)
//
//                verification_keyboard.animate().translationY(300f).start()
//                FoneApplication.get().onlining.set(true)
//                if (account.full_name?.isBlank()!!) {
//                    defaultSharedPreferences.putBoolean(Constants.Account.PREF_SET_NAME, true)
//                    mobileViewModel.insertUser(r.data!!.toUser())
//                    InitializeActivity.showSetupName(context!!)
//                }
//                activity?.finish()
//            }, { t: Throwable ->
//                handleError(t)
//            })
    }

    private fun handleFailure(r: FoneResponse<Account>) {
        pin_verification_view.error()
        pin_verification_tip_tv.visibility = VISIBLE
        pin_verification_tip_tv.text = getString(R.string.landing_validation_error)
        if (r.errorCode == ErrorHandler.PHONE_VERIFICATION_CODE_INVALID ||
            r.errorCode == ErrorHandler.PHONE_VERIFICATION_CODE_EXPIRED) {
            verification_next_fab.visibility = View.INVISIBLE
        }
        ErrorHandler.handleMixinError(r.errorCode)
    }


    private fun handleError(t: Throwable) {
        verification_next_fab.hide()
        verification_cover.visibility = GONE
        ErrorHandler.handleError(t)
    }

    private fun showLoading() {
        verification_next_fab.visibility = View.VISIBLE
        verification_next_fab.show()
        verification_cover.visibility = VISIBLE
    }

    private fun hideLoading() {
        verification_next_fab.hide()
        verification_next_fab.visibility = GONE
        verification_cover.visibility = GONE
    }

    private fun sendVerification(gRecaptchaResponse: String? = null) {
        showLoading()
//        val verificationRequest = VerificationRequest(
//            arguments!!.getString(ARGS_PHONE_NUM),
//            null,
//            if (pin == null) VerificationPurpose.SESSION.name else VerificationPurpose.PHONE.name,
//            gRecaptchaResponse)
//        mobileViewModel.verification(verificationRequest)
//            .autoDisposable(scopeProvider).subscribe({ r: FoneApplication<VerificationResponse> ->
//                if (!r.isSuccess) {
//                    hideLoading()
//                    ErrorHandler.handleMixinError(r.errorCode)
//                } else {
//                    hideLoading()
//                    pin_verification_view?.clear()
//                    startCountDown()
//                }
//            }, { t: Throwable ->
//                handleError(t)
//                verification_next_fab.visibility = GONE
//            })
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
                if (isAdded) {
                    hideLoading()
                }
                return
            }
            handlePinVerification()
        }
    }
}