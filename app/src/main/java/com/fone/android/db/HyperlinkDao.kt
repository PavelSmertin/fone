package com.fone.android.db

import androidx.room.Dao
import androidx.room.Query
import com.fone.android.vo.Hyperlink


@Dao
interface HyperlinkDao : BaseDao<Hyperlink> {
    @Query("SELECT * FROM hyperlinks WHERE hyperlink = :hyperlink")
    fun findHyperlinkByLink(hyperlink: String): Hyperlink?
}