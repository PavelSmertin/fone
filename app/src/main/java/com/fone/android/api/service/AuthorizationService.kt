package com.fone.android.api.service

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.AuthorizeRequest
import com.fone.android.api.response.AuthorizationResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthorizationService {

    @POST("oauth/authorize")
    fun authorize(@Body request: AuthorizeRequest): Observable<FoneResponse<AuthorizationResponse>>

    @GET("authorizations")
    fun authorizations(): Observable<FoneResponse<List<AuthorizationResponse>>>

}