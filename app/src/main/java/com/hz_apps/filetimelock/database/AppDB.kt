package com.hz_apps.filetimelock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LockFile::class], version = 2, exportSchema = false)
abstract class AppDB : RoomDatabase() {
    abstract fun lockFileDao() : LockFileDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getInstance(context: Context): AppDB {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "locked_files"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}