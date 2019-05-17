package com.fone.android.api.response

import com.fone.android.api.request.ParticipantRequest
import com.google.gson.annotations.SerializedName

open class ConversationResponse(
    @SerializedName("conversation_id")
    val conversationId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("creator_id")
    val creatorId: String,
    @SerializedName("icon_url")
    val iconUrl: String,
    @SerializedName("code_url")
    val codeUrl: String,
    @SerializedName("announcement")
    val announcement: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("participants")
    val participants: List<ParticipantRequest>,
    @SerializedName("mute_until")
    val muteUntil: String
)