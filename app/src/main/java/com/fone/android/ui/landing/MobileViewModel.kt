package com.fone.android.ui.landing

import androidx.lifecycle.ViewModel
import com.fone.android.Constants
import com.fone.android.FoneApplication
import com.fone.android.api.FoneResponse
import com.fone.android.api.request.AccountRequest
import com.fone.android.api.request.AccountUpdateRequest
import com.fone.android.api.request.VerificationRequest
import com.fone.android.api.response.VerificationResponse
import com.fone.android.extension.defaultSharedPreferences
import com.fone.android.extension.nowInUtc
import com.fone.android.repository.AccountRepository
import com.fone.android.repository.ConversationRepository
import com.fone.android.repository.UserRepository
import com.fone.android.vo.*
import com.fone.android.vo.model.ResponseRegister
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MobileViewModel @Inject internal
constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val messageRepository: ConversationRepository
) : ViewModel() {

    fun initialConversation() {
        var conversation = Conversation(
            "1",
            "1",
            ConversationCategory.CONTACT.name,
            "Давай поговорим",
            "https://placeimg.com/140/140/any",
            null,
            null,
            "",
            nowInUtc(),
            null,
            null,
            null,
            0,
            ConversationStatus.SUCCESS.ordinal,
            null)
        messageRepository.insertConversation(conversation, mutableListOf<Participant>())
    }

    fun loginVerification(request: VerificationRequest): Observable<FoneResponse<VerificationResponse>> =
        Observable.just(request).flatMap {
            val logoutComplete = FoneApplication.appContext.defaultSharedPreferences.getBoolean(Constants.Account.PREF_LOGOUT_COMPLETE, true)
            if (!logoutComplete) {
                FoneApplication.get().clearData()
            }

            accountRepository.verification(request)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun verification(request: VerificationRequest): Observable<FoneResponse<VerificationResponse>> =
        accountRepository.verification(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun create(id: String, request: AccountRequest): Observable<FoneResponse<ResponseRegister>> =
        accountRepository.create(id, request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun update(request: AccountUpdateRequest): Observable<FoneResponse<Account>> =
        accountRepository.update(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun insertUser(user: User) {
        userRepository.upsert(user)
    }

    fun updatePhone(id: String, phone: String) = userRepository.updatePhone(id, phone)

}