package com.fone.android.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.fone.android.vo.MessageHistory

@Dao
interface MessageHistoryDao : BaseDao<MessageHistory> {

    @Transaction
    @Query("SELECT * FROM messages_history WHERE message_id = :messageId")
    fun findMessageHistoryById(messageId: String): MessageHistory?
}
