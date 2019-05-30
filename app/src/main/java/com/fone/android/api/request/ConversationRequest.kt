package com.fone.android.api.request

import com.google.gson.annotations.SerializedName

class ConversationRequest(
    @SerializedName("username")
    val username: String? = null
)