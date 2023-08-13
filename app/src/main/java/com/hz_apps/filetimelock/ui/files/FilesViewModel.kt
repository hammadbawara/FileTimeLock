package com.hz_apps.filetimelock.ui.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
import java.time.LocalDateTime

class FilesViewModel () : ViewModel() {

    var timeNow : LocalDateTime? = null

    fun getLockedFiles(repository : DBRepository) : LiveData<MutableList<LockFile>>{
        return repository.getAllLockFiles()
    }
}