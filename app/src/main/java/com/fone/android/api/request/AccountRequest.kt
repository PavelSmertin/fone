package com.fone.android.api.request


data class AccountRequest(
    val username: String?,
    val number: Long?,
    val password: String? = "sasd"
)