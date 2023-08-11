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

fun getDateInFormat(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")
    return dateTime.format(formatter)
}

fun calculateTimeDifference(lockDate: LocalDateTime, unlockDate: LocalDateTime): String {
    if (lockDate >= unlockDate) {
        return "unlocked"
    }

    val duration = Duration.between(lockDate, unlockDate)

    return when {
        duration.toDays() >= 365 -> "${duration.toDays() / 365} yr"
        duration.toDays() >= 30 -> "${duration.toDays() / 30} mo"
        duration.toDays() >= 7 -> "${duration.toDays() / 7} wk"
        duration.toDays() > 0 -> "${duration.toDays()} day"
        duration.toHours() > 0 -> "${duration.toHours()} hr"
        duration.toMinutes() > 0 -> "${duration.toMinutes()} min"
        else -> "Less than a minute"
    }
}