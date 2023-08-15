package com.hz_apps.filetimelock.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LockFileDao {

    @Query("SELECT * FROM LockFile")
    fun getAll() : LiveData<MutableList<LockFile>>

    @Insert
    fun insert(file : LockFile)

    @Delete
    fun delete(file : LockFile)
    @Query("SELECT max(id) FROM LockFile")
    fun getLastId(): Int

    @Query("SELECT * FROM LockFile ORDER BY name ASC")
    fun getAllByFileName(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY name DESC")
    fun getAllByFileNameDesc(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY size ASC")
    fun getAllByFileSize(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY size DESC")
    fun getAllByFileSizeDesc(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY dateAdded ASC")
    fun getAllByLockDate(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY dateAdded DESC")
    fun getAllByLockDateDesc(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY dateUnlock ASC")
    fun getAllByUnlockDate(): LiveData<MutableList<LockFile>>

    @Query("SELECT * FROM LockFile ORDER BY dateUnlock DESC")
    fun getAllByUnlockDateDesc(): LiveData<MutableList<LockFile>>

    @Query("UPDATE lockfile set isUnlocked=1 where id = :id")
    fun setFileUnlocked(id : Int)

}