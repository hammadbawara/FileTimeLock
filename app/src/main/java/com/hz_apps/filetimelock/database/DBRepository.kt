package com.hz_apps.filetimelock.database

import androidx.annotation.WorkerThread

class DBRepository(
    private val lockFileDao: LockFileDao
) {

    fun  getAllLockFiles() : List<LockFile> {
        return lockFileDao.getAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertLockFile(lockFile: LockFile) {
        lockFileDao.insert(lockFile)
    }

    fun deleteLockFile(lockFile: LockFile) {
        lockFileDao.delete(lockFile)
    }

    fun getLastId() : Int {
        return lockFileDao.getLastId()
    }
}