package com.fone.android.extension


import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import com.fone.android.FoneApplication


fun Bitmap.toDrawable(): Drawable = BitmapDrawable(FoneApplication.appContext.resources, this)

fun String.toDrawable() = this.decodeBase64().encodeBitmap()?.toDrawable()

fun String.decodeBase64(): ByteArray {
    return Base64.decode(this, 0)
}

fun ByteArray.encodeBitmap(): Bitmap? {
    return if (this.isEmpty()) {
        null
    } else {
        BitmapFactory.decodeByteArray(this, 0, this.size)
    }
}