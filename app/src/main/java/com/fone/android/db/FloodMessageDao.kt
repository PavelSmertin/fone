package com.fone.android.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.fone.android.vo.FloodMessage

@Dao
interface FloodMessageDao : BaseDao<FloodMessage> {

    @Query("SELECT * FROM flood_messages ORDER BY created_at ASC limit 10")
    fun findFloodMessagesSync(): List<FloodMessage>?

    @Query("select count(1) from flood_messages")
    fun getFloodMessageCount(): LiveData<Int>
}