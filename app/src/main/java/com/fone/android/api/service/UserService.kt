package com.fone.android.api.service

import com.fone.android.api.FoneResponse
import com.fone.android.api.request.RelationshipRequest
import com.fone.android.vo.User
import com.fone.android.vo.model.ResponseUser
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    @POST("users/fetch")
    fun getUsers(@Body ids: List<String>): Call<FoneResponse<List<User>>>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: String): Call<FoneResponse<User>>

    @GET("numbers")
    fun search(@Query("search") search: String): Observable<ResponseUser>

    @POST("relationships")
    fun relationship(@Body request: RelationshipRequest): Observable<FoneResponse<User>>

    @GET("blocking_users")
    fun blockingUsers(): Observable<FoneResponse<List<User>>>
}