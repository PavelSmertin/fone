package com.fone.android.extension

import android.content.Context
import com.fone.android.R
import com.fone.android.util.TimeCache
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

private const val DAY_DURATION = 24 * 3600 * 1000

private val LocaleZone by lazy {
    ZoneId.systemDefault()
}

fun nowInUtc() = Instant.now().toString()

fun String.timeAgo(context: Context): String {
    var timeAgo = TimeCache.singleton.getTimeAgo(this)
    if (timeAgo == null) {
        val date = ZonedDateTime.parse(this).withZoneSameInstant(LocaleZone)
        val today = ZonedDateTime.of(
            ZonedDateTime.now().toLocalDate(),
            LocalTime.MIN, LocaleZone.normalized()).toInstant().toEpochMilli()
        val offset = today - date.toInstant().toEpochMilli()
        timeAgo = when {
            (offset > 7 * DAY_DURATION) -> {
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(LocaleZone))
            }
            (offset > DAY_DURATION) -> {
                when (date.dayOfWeek) {
                    DayOfWeek.MONDAY -> context.getString(R.string.week_monday)
                    DayOfWeek.TUESDAY -> context.getString(R.string.week_tuesday)
                    DayOfWeek.WEDNESDAY -> context.getString(R.string.week_wednesday)
                    DayOfWeek.THURSDAY -> context.getString(R.string.week_thursday)
                    DayOfWeek.FRIDAY -> context.getString(R.string.week_friday)
                    DayOfWeek.SATURDAY -> context.getString(R.string.week_saturday)
                    DayOfWeek.SUNDAY -> context.getString(R.string.week_sunday)
                    else -> date.format(DateTimeFormatter.ofPattern("MM:dd").withZone(LocaleZone))
                }
            }
            (today > date.toInstant().toEpochMilli()) -> {
                context.getString(R.string.yesterday)
            }
            else -> {
                date.format(DateTimeFormatter.ofPattern("HH:mm").withZone(LocaleZone))
            }
        }
        TimeCache.singleton.putTimeAgo(this, timeAgo)
    }
    return timeAgo as String
}

fun String.timeAgoDate(context: Context): String {
    val today = ZonedDateTime.of(ZonedDateTime.now().toLocalDate(),
        LocalTime.MIN, LocaleZone.normalized()).toInstant().toEpochMilli()
    var timeAgoDate = TimeCache.singleton.getTimeAgoDate(this + today)
    if (timeAgoDate == null) {
        val date = ZonedDateTime.parse(this).withZoneSameInstant(LocaleZone)
        val offset = today - date.toInstant().toEpochMilli()
        timeAgoDate = when {
            (offset > 7 * DAY_DURATION) -> {
                date.format(DateTimeFormatter.ofPattern("MM/dd").withZone(LocaleZone))
            }
            (offset > DAY_DURATION) -> {
                when (date.dayOfWeek) {
                    DayOfWeek.MONDAY -> context.getString(R.string.week_monday)
                    DayOfWeek.TUESDAY -> context.getString(R.string.week_tuesday)
                    DayOfWeek.WEDNESDAY -> context.getString(R.string.week_wednesday)
                    DayOfWeek.THURSDAY -> context.getString(R.string.week_thursday)
                    DayOfWeek.FRIDAY -> context.getString(R.string.week_friday)
                    DayOfWeek.SATURDAY -> context.getString(R.string.week_saturday)
                    DayOfWeek.SUNDAY -> context.getString(R.string.week_sunday)
                    else -> date.format(DateTimeFormatter.ofPattern("MM/dd").withZone(LocaleZone))
                }
            }
            (today > date.toInstant().toEpochMilli()) -> {
                context.getString(R.string.yesterday)
            }
            else -> {
                context.getString(R.string.today)
            }
        }
        TimeCache.singleton.putTimeAgoDate(this + today, timeAgoDate)
    }
    return timeAgoDate as String
}

fun String.timeAgoDay(): String {
    val today = ZonedDateTime.of(ZonedDateTime.now().toLocalDate(),
        LocalTime.MIN, LocaleZone.normalized()).toInstant().toEpochMilli()
    var timeAgoDate = TimeCache.singleton.getTimeAgoDate(this + today)
    if (timeAgoDate == null) {
        val date = ZonedDateTime.parse(this).withZoneSameInstant(LocaleZone)
        timeAgoDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(LocaleZone))
        TimeCache.singleton.putTimeAgoDate(this + today, timeAgoDate)
    }
    return timeAgoDate as String
}

fun String.hashForDate(): Long {
    var hashForDate = TimeCache.singleton.getHashForDate(this)
    if (hashForDate == null) {
        val date = ZonedDateTime.parse(this).toOffsetDateTime()
        val time = date.format(DateTimeFormatter.ofPattern("yyyMMdd").withZone(LocaleZone))
        hashForDate = time.hashCode().toLong()
        TimeCache.singleton.putHashForDate(this, hashForDate)
    }

    return hashForDate as Long
}

fun String.timeAgoClock(): String {
    var timeAgoClock = TimeCache.singleton.getTimeAgoClock(this)
    if (timeAgoClock == null) {
        val date = ZonedDateTime.parse(this).toOffsetDateTime()
        val time = date.format(DateTimeFormatter.ofPattern("HH:mm").withZone(LocaleZone))
        timeAgoClock = if (time.startsWith("0")) {
            time.substring(1)
        } else {
            time
        }
        TimeCache.singleton.putTimeAgoClock(this, timeAgoClock)
    }
    return timeAgoClock as String
}

fun isSameDay(time: String?, otherTime: String?): Boolean {
    if (time == null || otherTime == null) {
        return false
    }
    val date = time.hashForDate()
    val otherDate = otherTime.hashForDate()
    return date == otherDate
}