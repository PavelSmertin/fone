package com.fone.android.vo

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
@TypeConverters(ArrayConverters::class)
@Entity(tableName = "apps")
class App(
    @PrimaryKey
    @SerializedName("app_id")
    @ColumnInfo(name = "app_id")
    val appId: String,
    @SerializedName("app_number")
    @ColumnInfo(name = "app_number")
    val appNumber: String,
    @SerializedName("home_uri")
    @ColumnInfo(name = "home_uri")
    val homeUri: String,
    @SerializedName("redirect_uri")
    @ColumnInfo(name = "redirect_uri")
    val redirectUri: String,
    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String,
    @SerializedName("icon_url")
    @ColumnInfo(name = "icon_url")
    val icon_url: String,
    @SerializedName("description")
    @ColumnInfo(name = "description")
    val description: String,
    @SerializedName("app_secret")
    @ColumnInfo(name = "app_secret")
    val appSecret: String,
    @SerializedName("capabilites")
    @ColumnInfo(name = "capabilites")
    val capabilites: ArrayList<String>?,
    @SerializedName("creator_id")
    @ColumnInfo(name = "creator_id")
    val creatorId: String

) : Parcelable {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<App>() {
            override fun areItemsTheSame(p0: App, p1: App): Boolean {
                return p0.appId == p1.appId
            }

            override fun areContentsTheSame(p0: App, p1: App): Boolean {
                return p0 == p1
            }
        }
    }
}

enum class AppCap { GROUP, CONTACT }