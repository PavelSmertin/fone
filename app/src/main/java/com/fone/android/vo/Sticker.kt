package com.fone.android.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "stickers")
data class Sticker(
    @PrimaryKey
    @SerializedName("sticker_id")
    @ColumnInfo(name = "sticker_id")
    val stickerId: String,
    @SerializedName("album_id")
    @ColumnInfo(name = "album_id")
    val albumId: String?,
    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String,
    @SerializedName("asset_url")
    @ColumnInfo(name = "asset_url")
    val assetUrl: String,
    @SerializedName("asset_type")
    @ColumnInfo(name = "asset_type")
    val assetType: String,
    @SerializedName("asset_width")
    @ColumnInfo(name = "asset_width")
    val assetWidth: Int,
    @SerializedName("asset_height")
    @ColumnInfo(name = "asset_height")
    val assetHeight: Int,
    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    var createdAt: String,
    @ColumnInfo(name = "last_use_at")
    var lastUseAt: String?
)
