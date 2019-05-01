package com.fone.android.db

import androidx.room.Dao
import androidx.room.Query
import com.fone.android.vo.ResendMessage

@Dao
interface ResendMessageDao : BaseDao<ResendMessage> {

    @Query("SELECT * FROM resend_messages WHERE user_id = :userId AND message_id = :messageId")
    fun findResendMessage(userId: String, messageId: String): ResendMessage?
}