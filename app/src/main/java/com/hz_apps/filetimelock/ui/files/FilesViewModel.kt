package com.hz_apps.filetimelock.ui.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.utils.FileSort
import java.time.LocalDateTime

class FilesViewModel () : ViewModel() {

    var timeNow : LocalDateTime? = null

    lateinit var sortBy : FileSort
    var isAscending = true

    var numOfSelectedItems = 0

    fun getLockedFiles(repository : DBRepository) : LiveData<MutableList<LockFile>>{
        return repository.getAllLockFiles(sortBy, isAscending)
    }
}