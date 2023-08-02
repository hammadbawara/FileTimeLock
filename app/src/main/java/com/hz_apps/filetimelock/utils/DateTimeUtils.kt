package com.hz_apps.filetimelock.utils

import java.time.LocalDateTime

fun getTimeIn12HourFormat(dateTime: LocalDateTime): String {
    val hour = dateTime.hour
    val minute = dateTime.minute
    val amPm = if (hour < 12) "AM" else "PM"

    return String.format("%02d:%02d %s", hour, minute, amPm)
}

fun getDateInFormat(dateTime: LocalDateTime) : String {
    val day = dateTime.dayOfMonth
    val month = getMonthName(dateTime.monthValue)
    val year = dateTime.year

    return String.format("%02d %s %d", day, month, year)
}

private fun getMonthName(month: Int): String {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    return monthNames[month - 1]
}