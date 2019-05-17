package com.fone.android.api.service

import com.fone.android.api.FoneResponse
import com.fone.android.websocket.BlazeMessageData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MessageService {
    @GET("messages/status/{offset}")
    fun messageStatusOffset(@Path("offset") offset: Long): Call<FoneResponse<List<BlazeMessageData>>>
}