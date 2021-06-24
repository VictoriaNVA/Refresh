package com.example.refresh.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*Database table for Applications to monitor.
  All database files were created following a 4 parts tutorial
  from Stevdza-San on Youtube. Part 1 is attached in link below
  https://www.youtube.com/watch?v=lwAvI3WDXBY&t=5s
 */

//Currently only one table is implemented, with 2 columns

@Entity(
    tableName = "Apps", indices = [Index(
        value = ["App_Name", "Package_Name"],
        unique = true
    )]
)
data class Apps(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "App_Name") val name: String,
    @ColumnInfo(name = "Package_Name") val packageName: String
)