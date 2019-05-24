package com.fone.android.api.service

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.*
import com.fone.android.api.response.VerificationResponse
import com.fone.android.vo.Account
import com.fone.android.vo.model.ResponseRegister
import com.google.gson.JsonObject
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AccountService {

    @POST("verifications")
    fun verification(@Body request: VerificationRequest): Observable<FoneResponse<VerificationResponse>>

    @POST("register")
    fun create(@Body request: AccountRequest): Observable<FoneResponse<ResponseRegister>>

    @POST("verifications/{id}")
    fun changePhone(@Path("id") id: String, @Body request: AccountRequest): Observable<FoneResponse<Account>>

    @POST("me")
    fun update(@Body request: AccountUpdateRequest): Observable<FoneResponse<Account>>

    @POST("me/preferences")
    fun preferences(@Body request: AccountUpdateRequest): Observable<FoneResponse<Account>>

    @GET("me")
    fun getMe(): Call<FoneResponse<Account>>

    @POST("logout")
    fun logoutAsync(@Body request: LogoutRequest): Deferred<FoneResponse<Unit>>

    @GET("codes/{id}")
    fun code(@Path("id") id: String): Observable<FoneResponse<JsonObject>>

    @POST("invitations/{code}")
    fun invitations(@Path("code") code: String): Observable<FoneResponse<Account>>

    @POST("pin/update")
    fun updatePin(@Body request: PinRequest): Observable<FoneResponse<Account>>

    @POST("pin/verify")
    fun verifyPin(@Body request: PinRequest): Observable<FoneResponse<Account>>

    @POST("session")
    fun updateSession(@Body request: SessionRequest): Observable<FoneResponse<Account>>

}