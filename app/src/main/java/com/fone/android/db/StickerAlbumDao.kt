package com.fone.android.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.fone.android.vo.StickerAlbum

@Dao
interface StickerAlbumDao : BaseDao<StickerAlbum> {

    @Query("SELECT * FROM sticker_albums WHERE category = 'SYSTEM' ORDER BY created_at DESC")
    fun getSystemAlbums(): LiveData<List<StickerAlbum>>

    @Query("SELECT * FROM sticker_albums WHERE category = 'PERSONAL' ORDER BY created_at ASC")
    fun getPersonalAlbums(): StickerAlbum?
}