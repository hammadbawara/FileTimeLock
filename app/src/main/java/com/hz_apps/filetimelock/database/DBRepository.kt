package com.hz_apps.filetimelock.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.hz_apps.filetimelock.utils.FileSort

class DBRepository(
    private val lockFileDao: LockFileDao
) {

    fun getAllLockFiles(sortBy: FileSort, isAscending : Boolean): LiveData<MutableList<LockFile>> {
        return when (sortBy) {
            FileSort.NAME -> if (isAscending) lockFileDao.getAllByFileName() else lockFileDao.getAllByFileNameDesc()
            FileSort.SIZE -> if (isAscending) lockFileDao.getAllByFileSize() else lockFileDao.getAllByFileSizeDesc()
            FileSort.DATE_ADDED -> if (isAscending) lockFileDao.getAllByLockDate() else lockFileDao.getAllByLockDateDesc()
            FileSort.DATE_UNLOCK -> if (isAscending) lockFileDao.getAllByUnlockDate() else lockFileDao.getAllByUnlockDateDesc()
            else -> {
                if (isAscending) lockFileDao.getAllByFileName() else lockFileDao.getAllByFileNameDesc()
            }
        }
    }

    suspend fun setFileUnlocked(id : Int, newPath : String) {
        lockFileDao.setFileUnlocked(id, newPath)

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