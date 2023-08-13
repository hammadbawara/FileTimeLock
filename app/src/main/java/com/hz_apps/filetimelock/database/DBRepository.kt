package com.hz_apps.filetimelock.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.hz_apps.filetimelock.utils.FileSort

class DBRepository(
    private val lockFileDao: LockFileDao
) {

    fun getAllLockFiles(sortBy: FileSort): LiveData<MutableList<LockFile>> {
        return when (sortBy) {
            FileSort.NAME -> lockFileDao.getAllByFileName()
            FileSort.NAME_DESC -> lockFileDao.getAllByFileNameDesc()
            FileSort.SIZE -> lockFileDao.getAllByFileSize()
            FileSort.SIZE_DESC -> lockFileDao.getAllByFileSizeDesc()
            FileSort.LOCK_DATE -> lockFileDao.getAllByLockDate()
            FileSort.LOCK_DATE_DESC -> lockFileDao.getAllByLockDateDesc()
            FileSort.UNLOCK_DATE -> lockFileDao.getAllByUnlockDate()
            FileSort.UNLOCK_DATE_DESC -> lockFileDao.getAllByUnlockDateDesc()
        }
    }

    suspend fun setFileUnlocked(id : Int) {
        lockFileDao.setFileUnlocked(id)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertLockFile(lockFile: LockFile) {
        lockFileDao.insert(lockFile)
    }

    fun deleteLockFile(lockFile: LockFile) {
        lockFileDao.delete(lockFile)
    }

    suspend fun getLastId() : Int {
        return lockFileDao.getLastId()
    }

    fun delete(file: LockFile) {
        lockFileDao.delete(file)
    }
}