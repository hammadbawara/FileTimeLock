package com.hz_apps.filetimelock.ui.lock_file

import androidx.lifecycle.ViewModel
import java.io.File
import java.time.LocalDateTime

class LockFileViewModel: ViewModel() {

    var lockFile: File? = null
    private var lockDateTime : LocalDateTime? = null

    fun getDateTime() : LocalDateTime {
        return if( lockDateTime == null) {
            LocalDateTime.now()
        }else {
            lockDateTime!!
        }
    }

    fun setDateTime(ldt: LocalDateTime) {
        lockDateTime = ldt
    }

}