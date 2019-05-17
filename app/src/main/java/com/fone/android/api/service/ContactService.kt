package com.fone.android.api.service

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.ContactRequest
import com.fone.android.vo.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ContactService {

    @POST("contacts")
    fun syncContacts(@Body contacts: List<ContactRequest>): Call<FoneResponse<Any>>

    @GET("friends")
    fun friends(): Call<FoneResponse<List<User>>>

    @GET("contacts")
    fun contacts(): Call<FoneResponse<List<User>>>
}