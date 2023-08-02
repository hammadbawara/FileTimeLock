package com.hz_apps.filetimelock.utils

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