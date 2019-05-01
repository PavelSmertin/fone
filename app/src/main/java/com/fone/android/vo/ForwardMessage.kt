package com.fone.android.vo

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Entity
@Parcelize
data class ForwardMessage(
    val type: String,
    val id: String? = null,
    val content: String? = null,
    val mediaUrl: String? = null,
    val sharedUserId: String? = null
) : Parcelable

enum class ForwardCategory {
    TEXT,
    IMAGE,
    DATA,
    VIDEO,
    STICKER,
    CONTACT,
    AUDIO
}

fun ForwardMessage.addTo(list: MutableList<ForwardMessage>) {
    list.add(this)
}