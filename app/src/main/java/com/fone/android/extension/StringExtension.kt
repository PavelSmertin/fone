@file:Suppress("NOTHING_TO_INLINE")

package com.fone.android.extension


import android.util.ArrayMap
import com.fone.android.util.GzipException
import okio.*
import org.threeten.bp.Instant
import java.io.IOException
import java.security.MessageDigest
import java.util.*
import kotlin.collections.set
import kotlin.math.abs


val idCodeMap = ArrayMap<String, Int>()

fun String.getColorCode(
    count: Int
): Int {
    var code = idCodeMap[this]
    if (code != null) return code

    val hashcode = try {
        UUID.fromString(this).hashCode()
    } catch (e: IllegalArgumentException) {
        hashCode()
    }
    code = abs(hashcode).rem(count)
    idCodeMap[this] = code
    return code
}

fun Long.formatMillis(): String {
    val formatBuilder = StringBuilder()
    val formatter = Formatter(formatBuilder, Locale.getDefault())
    getStringForTime(formatBuilder, formatter, this)
    return formatBuilder.toString()
}

fun getStringForTime( builder : StringBuilder, formatter: Formatter,  timeMs: Long) : String {
    val totalSeconds = (timeMs + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    builder.setLength(0)
    if (hours > 0) {
        return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    } else {
        return formatter.format("%02d:%02d", minutes, seconds).toString()
    }
}

inline fun String.sha256(): ByteArray {
    val md = MessageDigest.getInstance("SHA256")
    return md.digest(toByteArray())
}

private val HEX_CHARS = "0123456789abcdef"
fun ByteArray.toHex(): String {
    val hex = HEX_CHARS.toCharArray()
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(hex[firstIndex])
        result.append(hex[secondIndex])
    }

    return result.toString()
}

@Throws(IOException::class)
fun String.gzip(): ByteString {
    val result = Buffer()
    val sink = Okio.buffer(GzipSink(result))
    sink.use {
        sink.write(toByteArray())
    }
    return result.readByteString()
}

@Throws(GzipException::class)
fun ByteString.ungzip(): String {
    val buffer = Buffer().write(this)
    val gzip = GzipSource(buffer as Source)
    return Okio.buffer(gzip).readUtf8()
}

fun String.getEpochNano(): Long {
    val inst = Instant.parse(this)
    var time = inst.epochSecond
    time *= 1000000000L
    time += inst.nano
    return time
}