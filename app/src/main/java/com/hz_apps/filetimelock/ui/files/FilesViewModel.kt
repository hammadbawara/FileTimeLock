package com.hz_apps.filetimelock.ui.files

import androidx.lifecycle.ViewModel
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile

class FilesViewModel () : ViewModel() {

    fun getLockedFiles(repository : DBRepository) : List<LockFile>{
        return repository.getAllLockFiles()
    }
}