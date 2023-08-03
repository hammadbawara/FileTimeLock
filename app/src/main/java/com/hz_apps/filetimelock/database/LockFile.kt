package com.hz_apps.filetimelock.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LockFile (
    @PrimaryKey val id : Int,
    val name : String,
    val lockTime: Long,
    val unlockTime : Long,
    val location: String,
    val size : String
) {
}