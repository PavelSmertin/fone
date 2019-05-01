package com.fone.android.vo

import androidx.room.Entity
import org.threeten.bp.Instant

@Entity
data class ConversationItem(
    val conversationId: String,
    val avatarUrl: String?,
    val groupIconUrl: String?,
    val category: String?,
    val groupName: String?,
    val name: String,
    val ownerId: String,
    val ownerIdentityNumber: String,
    val status: Int,
    val lastReadMessageId: String?,
    val unseenMessageCount: Int?,
    val content: String?,
    val contentType: String?,
    val mediaUrl: String?,
    val createdAt: String?,
    val pinTime: String?,
    val senderId: String?,
    val senderFullName: String?,
    val messageStatus: String?,
    val actionName: String?,
    val participantFullName: String?,
    val participantUserId: String?,
    val ownerMuteUntil: String?,
    val ownerVerified: Boolean?,
    val muteUntil: String?,
    val snapshotType: String?,
    val appId: String?
) {
    fun isGroup() = category == ConversationCategory.GROUP.name

    fun isContact() = category == ConversationCategory.CONTACT.name

    fun getConversationName(): String {
        return when {
            isContact() -> name
            isGroup() -> groupName!!
            else -> ""
        }
    }

    fun iconUrl(): String? {
        return when {
            isContact() -> avatarUrl
            isGroup() -> groupIconUrl
            else -> null
        }
    }

    fun isMute(): Boolean {
        if (isContact() && ownerMuteUntil != null) {
            return Instant.now().isBefore(Instant.parse(ownerMuteUntil))
        }
        if (isGroup() && muteUntil != null) {
            return Instant.now().isBefore(Instant.parse(muteUntil))
        }
        return false
    }

    fun isBot(): Boolean {
        return appId != null
    }

    fun isCallMessage() =
        contentType == MessageCategory.WEBRTC_AUDIO_CANCEL.name ||
            contentType == MessageCategory.WEBRTC_AUDIO_DECLINE.name ||
            contentType == MessageCategory.WEBRTC_AUDIO_END.name ||
            contentType == MessageCategory.WEBRTC_AUDIO_BUSY.name ||
            contentType == MessageCategory.WEBRTC_AUDIO_FAILED.name
}
