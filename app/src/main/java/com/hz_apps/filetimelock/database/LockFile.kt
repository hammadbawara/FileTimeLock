package com.hz_apps.filetimelock.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hz_apps.filetimelock.utils.calculateTimeDifference
import java.io.Serializable
import java.time.LocalDateTime

@Entity
data class LockFile(
    @PrimaryKey val id : Int,
    val name : String,
    val dateAdded: LocalDateTime,
    val dateUnlock : LocalDateTime,
    val path: String,
    val size : Long,
    val extension: String,
    var isUnlocked: Boolean,
): Serializable {
    @Ignore
    var remainingTime : String = ""

    fun calculateRemainingTime(dateNow : LocalDateTime){
        if (!isUnlocked) {
            remainingTime = calculateTimeDifference(dateUnlock, dateNow)
        }
    }
}

