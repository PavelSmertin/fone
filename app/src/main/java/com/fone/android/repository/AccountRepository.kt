package com.fone.android.repository

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.AccountRequest
import com.fone.android.api.request.AccountUpdateRequest
import com.fone.android.api.request.VerificationRequest
import com.fone.android.api.response.ConversationResponse
import com.fone.android.api.response.VerificationResponse
import com.fone.android.api.service.AccountService
import com.fone.android.api.service.ConversationService
import com.fone.android.api.service.UserService
import com.fone.android.vo.Account
import com.fone.android.vo.model.ResponseRegister
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository
    @Inject
    constructor(
        private val accountService: AccountService,
        private val conversationService: ConversationService,
        private val userService: UserService

        ){

    fun verification(request: VerificationRequest): Observable<FoneResponse<VerificationResponse>> =
        accountService.verification(request)

    fun create(id: String, request: AccountRequest): Observable<ResponseRegister> =
        accountService.create(request)

    fun changePhone(id: String, request: AccountRequest): Observable<FoneResponse<Account>> =
        accountService.changePhone(id, request)

    fun update(request: AccountUpdateRequest): Observable<FoneResponse<Account>> =
        accountService.update(request)

    fun join(conversationId: String): Observable<FoneResponse<ConversationResponse>> {
        return conversationService.join(conversationId)
    }

    fun search(query: String): Observable<ArrayList<String>> =
        userService.search(query)

}