package com.hz_apps.filetimelock.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getTimeIn12HourFormat(dateTime: LocalDateTime): String {
    var hour = dateTime.hour
    val minute = dateTime.minute
    var amPm : String

    if (hour < 12) {
        amPm = "AM"
    } else {
        amPm = "PM"
        hour-=12
    }


    return String.format("%02d:%02d %s", hour, minute, amPm)
}

fun getDateInFormat(dateTime: LocalDateTime): String? {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")
    return dateTime.format(formatter)
}

fun calculateTimeDifference(end: LocalDateTime, start: LocalDateTime,): String {
    if (start >= end) {
        return "unlocked"
    }

    val duration = Duration.between(start, end)

    return when {
        duration.toDays() >= 365 -> "${duration.toDays() / 365} yr"
        duration.toDays() >= 30 -> "${duration.toDays() / 30} mo"
        duration.toDays() > 0 -> "${duration.toDays()} day"
        duration.toHours() > 0 -> "${duration.toHours()} hr"
        duration.toMinutes() > 0 -> "${duration.toMinutes()} min"
        else -> ">1 min"
    }
}

fun calculateTimeDifferenceTillEnd(end: LocalDateTime, start: LocalDateTime): String {
    if (start >= end) {
        return "unlocked"
    }

    val duration = Duration.between(start, end)

    val years = duration.toDays() / 365
    val months = (duration.toDays() % 365) / 30
    val days = (duration.toDays() % 30).toInt()
    val hours = (duration.toHours() % 24).toInt()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60

    val timeParts = mutableListOf<String>()

    if (years > 0) {
        timeParts.add("$years yr")
    }
    if (months > 0) {
        timeParts.add("$months mo")
    }
    if (days > 0) {
        timeParts.add("$days day")
    }
    if (hours > 0) {
        timeParts.add("$hours hr")
    }
    if (minutes > 0) {
        timeParts.add("$minutes min")
    }
    if (seconds > 0) {
        timeParts.add("$seconds sec")
    }

    return timeParts.joinToString(" ")
}