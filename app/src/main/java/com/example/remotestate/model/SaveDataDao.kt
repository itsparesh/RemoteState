package com.example.remotestate.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SaveDataDao {
    @Insert
    fun insertSaveData(saveData: SaveData)

    @Query("Select * from SaveData")
    fun getSaveData(): List<SaveData>?
}