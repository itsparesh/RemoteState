package com.example.remotestate.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SaveData::class], version = 1, exportSchema = false)
abstract class SaveDataDatabase : RoomDatabase() {

    abstract fun saveDataDao(): SaveDataDao

    companion object {
        @Volatile
        private var INSTANCE: SaveDataDatabase? = null

        fun getDatabase(context: Context): SaveDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SaveDataDatabase::class.java,
                    "dataDB.db"
                ).fallbackToDestructiveMigration()
                    .allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}