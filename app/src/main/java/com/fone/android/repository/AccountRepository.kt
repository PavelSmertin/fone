package com.fone.android.repository

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.AccountRequest
import com.fone.android.api.request.AccountUpdateRequest
import com.fone.android.api.request.VerificationRequest
import com.fone.android.api.response.VerificationResponse
import com.fone.android.api.service.AccountService
import com.fone.android.vo.Account
import com.fone.android.vo.model.ResponseRegister
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository
    @Inject
    constructor(
        private val accountService: AccountService
    ){

    fun verification(request: VerificationRequest): Observable<FoneResponse<VerificationResponse>> =
        accountService.verification(request)

    fun create(id: String, request: AccountRequest): Observable<FoneResponse<ResponseRegister>> =
        accountService.create(request)

    fun changePhone(id: String, request: AccountRequest): Observable<FoneResponse<Account>> =
        accountService.changePhone(id, request)

    fun update(request: AccountUpdateRequest): Observable<FoneResponse<Account>> =
        accountService.update(request)
}