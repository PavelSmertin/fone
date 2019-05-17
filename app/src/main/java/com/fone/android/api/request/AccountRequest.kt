package com.fone.android.api.request

import android.os.Build
import com.fone.android.BuildConfig

data class AccountRequest(
    val code: String?,
    val invitation: String? = null,
    val notification_token: String? = null,
    val registration_id: Int? = null,
    val platform: String = "Android",
    val platform_version: String = Build.VERSION.RELEASE,
    val app_version: String = BuildConfig.VERSION_NAME,
    val package_name: String = BuildConfig.APPLICATION_ID,
    var purpose: String = VerificationPurpose.SESSION.name,
    val pin: String? = null,
    val session_secret: String? = null
)