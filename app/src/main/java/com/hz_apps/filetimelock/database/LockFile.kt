package com.hz_apps.filetimelock.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class LockFile (
    @PrimaryKey val id : Int,
    val name : String,
    val lockTime: LocalDateTime,
    val unlockTime : LocalDateTime,
    val location: String,
    val size : String,
    val extension: String
) {
}