package com.fone.android.db

import androidx.room.Dao
import androidx.room.Query
import com.fone.android.vo.Job

@Dao
interface JobDao : BaseDao<Job> {
    @Query("SELECT * FROM jobs WHERE `action` = 'ACKNOWLEDGE_MESSAGE_RECEIPTS' ORDER BY created_at ASC LIMIT 100")
    fun findAckJobsSync(): List<Job>?

    @Query("SELECT * FROM jobs WHERE `action` = 'ACKNOWLEDGE_SESSION_MESSAGE_RECEIPTS' ORDER BY created_at ASC LIMIT 100")
    fun findSessionAckJobsSync(): List<Job>?

    @Query("SELECT * FROM jobs WHERE `action` = 'CREATE_SESSION_MESSAGE' ORDER BY created_at ASC LIMIT 100")
    fun findCreatePlainSessionJobsSync(): List<Job>?

    @Query("DELETE FROM jobs WHERE `action` = 'CREATE_SESSION_MESSAGE'")
    fun removeExtensionSessionJob()
}