package com.example.remotestate.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SaveData")
class SaveData() {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "Latitude")
    var latitude: Double = 0.0

    @ColumnInfo(name = "Longitude")
    var longitude: Double = 0.0

    @ColumnInfo(name = "TimeStamp")
    var timeStamp: String = ""

    constructor(latitude: Double, longitude: Double, timeStamp: String) : this() {
        this.latitude = latitude
        this.longitude = longitude
        this.timeStamp = timeStamp
    }
}