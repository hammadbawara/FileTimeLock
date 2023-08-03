package com.hz_apps.filetimelock.ui.lock_file

import androidx.lifecycle.ViewModel
import java.io.File
import java.time.LocalDateTime

class LockFileViewModel: ViewModel() {

    var lockFile: File? = null
    private var unlockTime : LocalDateTime? = null

    fun getUnlockTime() : LocalDateTime {
        if( unlockTime == null) {
            unlockTime = LocalDateTime.now()
        }
        return unlockTime!!
    }

    fun setDateTime(ldt: LocalDateTime) {
        unlockTime = ldt
    }

}