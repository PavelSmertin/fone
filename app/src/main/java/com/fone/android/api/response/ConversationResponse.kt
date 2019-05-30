package com.fone.android.api.response

import com.google.gson.annotations.SerializedName

open class ConversationResponse(
    @SerializedName("id")
    val conversationId: String,
    @SerializedName("creator_id")
    val lastMessage: String,
    @SerializedName("hasUnreadMessages")
    val hasUnreadMessages: Boolean
)