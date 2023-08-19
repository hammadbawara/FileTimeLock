package com.hz_apps.filetimelock.ui.lock_file

import androidx.lifecycle.ViewModel
import java.io.File
import java.time.LocalDateTime

class LockFileViewModel: ViewModel() {

    lateinit var lockFile: File
    var unlockDateTime = LocalDateTime.now()

}