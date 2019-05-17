package com.fone.android.api.request

import com.fone.android.BuildConfig
import com.google.gson.annotations.SerializedName

data class VerificationRequest(
    val phone: String?,
    val invitation: String?,
    val purpose: String,
    @SerializedName("g_recaptcha_response")
    val gRecaptchaResponse: String?,
    val package_name: String = BuildConfig.APPLICATION_ID
)

enum class VerificationPurpose {
    SESSION,
    PHONE
}
