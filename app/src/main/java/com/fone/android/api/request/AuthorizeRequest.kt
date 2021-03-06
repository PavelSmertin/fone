package com.fone.android.api.request

import com.google.gson.annotations.SerializedName

class AuthorizeRequest(
    @SerializedName("authorization_id")
    val authorizationId: String,
    val scopes: List<String>
)
