package com.fone.android.api.service

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.RelationshipRequest
import com.fone.android.vo.User
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

    @POST("users/fetch")
    fun getUsers(@Body ids: List<String>): Call<FoneResponse<List<User>>>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: String): Call<FoneResponse<User>>

    @GET("search/{query}")
    fun search(@Path("query") query: String): Observable<FoneResponse<User>>

    @POST("relationships")
    fun relationship(@Body request: RelationshipRequest): Observable<FoneResponse<User>>

    @GET("blocking_users")
    fun blockingUsers(): Observable<FoneResponse<List<User>>>
}