package com.fone.android.api.request


data class AccountRequest(
    val username: String?,
    val number: String?,
    val password: String? = "sasd"
)