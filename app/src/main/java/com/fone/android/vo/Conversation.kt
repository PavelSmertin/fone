package com.fone.android.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fone.android.extension.nowInUtc
import java.util.*

@Entity(tableName = "conversations", indices = [
    Index(value = ["conversation_id"], unique = true),
    Index(value = ["created_at"])])
open class Conversation(
    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String?,
    @ColumnInfo(name = "category")
    val category: String?,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "icon_url")
    val iconUrl: String?,
    @ColumnInfo(name = "announcement")
    val announcement: String?,
    @ColumnInfo(name = "code_url")
    val codeUrl: String?,
    @ColumnInfo(name = "pay_type")
    val payType: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "pin_time")
    val pinTime: String?,
    @ColumnInfo(name = "last_message_id")
    val lastMessageId: String?,
    @ColumnInfo(name = "last_read_message_id")
    val lastReadMessageId: String?,
    @ColumnInfo(name = "unseen_message_count")
    val unseenMessageCount: Int?,
    @ColumnInfo(name = "status")
    val status: Int,
    @ColumnInfo(name = "draft")
    val draft: String? = null,
    @ColumnInfo(name = "mute_until")
    val muteUntil: String? = null
)

enum class ConversationCategory { CONTACT, GROUP }
enum class ConversationStatus { START, FAILURE, SUCCESS, QUIT }

fun Conversation.isGroup(): Boolean {
    return category == ConversationCategory.GROUP.name
}

fun Conversation.isContact(): Boolean {
    return category == ConversationCategory.CONTACT.name
}

fun createConversation(conversationId: String, category: String?, recipientId: String, status: Int) =
    ConversationBuilder(conversationId, nowInUtc(), status)
        .setCategory(category)
        .setOwnerId(recipientId)
        .setAnnouncement("")
        .setCodeUrl("")
        .setPayType("")
        .setUnseenMessageCount(0)
        .build()

fun generateConversationId(senderId: String, recipientId: String): String {
    val mix = minOf(senderId, recipientId) + maxOf(senderId, recipientId)
    return UUID.nameUUIDFromBytes(mix.toByteArray()).toString()
}
