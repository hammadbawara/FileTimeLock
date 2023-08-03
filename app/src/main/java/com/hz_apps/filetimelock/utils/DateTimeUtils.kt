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
        duration.toDays() >= 365 -> "${duration.toDays() / 365} years"
        duration.toDays() >= 30 -> "${duration.toDays() / 30} months"
        duration.toDays() >= 7 -> "${duration.toDays() / 7} weeks"
        duration.toDays() > 0 -> "${duration.toDays()} days"
        duration.toHours() > 0 -> "${duration.toHours()} hours"
        duration.toMinutes() > 0 -> "${duration.toMinutes()} minutes"
        else -> "Less than a minute"
    }
}