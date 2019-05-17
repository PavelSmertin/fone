package com.fone.android.api.request

import com.fone.android.BuildConfig
import com.google.gson.annotations.SerializedName

data class SessionRequest(
    val platform: String = "Android",
    @SerializedName("platform_version")
    val platformVersion: String = android.os.Build.VERSION.RELEASE,
    @SerializedName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("notification_token")
    val notificationToken: String
)