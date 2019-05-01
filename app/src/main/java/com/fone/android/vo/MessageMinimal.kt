package com.fone.android.vo

import androidx.room.Entity

@Entity
data class MessageMinimal(
    val id: String,
    val created_at: String
)