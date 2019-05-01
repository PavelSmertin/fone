package com.fone.android.vo

import com.google.gson.annotations.SerializedName

data class AppCardData(
    @SerializedName("icon_url")
    val iconUrl: String,
    val title: String,
    val description: String,
    val action: String
)
