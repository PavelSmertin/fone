package com.fone.android.extension

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.toBytes(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val data = stream.toByteArray()
    stream.closeSilently()
    return data
}
