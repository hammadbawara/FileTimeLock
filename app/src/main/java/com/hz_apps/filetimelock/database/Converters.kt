package com.hz_apps.filetimelock.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class Converters{
    @TypeConverter
    fun dateToTimeStamp(dateTime: LocalDateTime): Long {
        return dateTime.toEpochSecond(ZonedDateTime.now().offset)
    }

    @TypeConverter
    fun timeStampsToDate(seconds: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault())
    }
}