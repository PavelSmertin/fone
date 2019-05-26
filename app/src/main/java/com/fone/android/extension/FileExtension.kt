package com.fone.android.extension


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.os.EnvironmentCompat
import com.fone.android.FoneApplication
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun Context.getImagePath(): File {
    val root = getMediaPath()
    return File("$root${File.separator}Images")
}

fun Context.getMediaPath(): File {
    return File("${getAppPath().absolutePath}${File.separator}Media${File.separator}")
}

private fun Context.getAppPath(): File {
    return if (!hasWritePermission()) {
        getBestAvailableCacheRoot()
    } else if (isAvailable()) {
        File(
            "${Environment.getExternalStorageDirectory()}${File.separator}Mixin${File.separator}"
        )
    } else {
        var externalFile: Array<File>? = ContextCompat.getExternalFilesDirs(this, null)
        if (externalFile == null) {
            externalFile = arrayOf(this.getExternalFilesDir(null))
        }
        val root = File("${externalFile[0]}${File.separator}Mixin${File.separator}")
        root.mkdirs()
        return if (root.exists()) {
            root
        } else {
            getBestAvailableCacheRoot()
        }
    }
}

private fun isAvailable(): Boolean {
    val state = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
        return true
    }
    return false
}

private fun hasWritePermission(): Boolean {
    return ContextCompat.checkSelfPermission(FoneApplication.appContext,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

private fun Context.getBestAvailableCacheRoot(): File {
    val roots = ContextCompat.getExternalCacheDirs(this)
    roots.filter { it != null && Environment.MEDIA_MOUNTED == EnvironmentCompat.getStorageState(it) }
        .forEach { return it }
    return this.cacheDir
}


fun File.createImageTemp(prefix: String? = null, type: String? = null, noMedia: Boolean = true): File {
    val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return if (prefix != null) {
        newTempFile("${prefix}_IMAGE_$time", type ?: ".jpg", noMedia)
    } else {
        newTempFile("IMAGE_$time", type ?: ".jpg", noMedia)
    }
}

private fun File.newTempFile(name: String, type: String, noMedia: Boolean): File {
    if (!this.exists()) {
        this.mkdirs()
    }
    if (noMedia) {
        createNoMediaDir()
    }
    return createTempFile(name, type, this)
}

fun File.createNoMediaDir() {
    val no = File(this, ".nomedia")
    if (!no.exists()) {
        no.createNewFile()
    }
}




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