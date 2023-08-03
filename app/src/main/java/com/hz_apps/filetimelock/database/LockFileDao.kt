package com.hz_apps.filetimelock.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LockFileDao {

    @Query("SELECT * FROM LockFile")
    fun getAll() : List<LockFile>

    @Insert
    fun insert(file : LockFile)

    @Delete
    fun delete(file : LockFile)
    @Query("SELECT max(id) FROM LockFile")
    fun getLastId(): Int


}