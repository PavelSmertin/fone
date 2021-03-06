package com.fone.android.api.request

import com.google.gson.annotations.SerializedName

data class PinRequest(
    @SerializedName("pin")
    val pin: String,
    @SerializedName("old_pin")
    val oldPin: String? = null
)
