package com.example.refresh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*Database table for Applications to monitor.
  All database files were created following a 4 parts tutorial
  from Stevdza-San on Youtube. Part 1 is attached in link below
  https://www.youtube.com/watch?v=lwAvI3WDXBY&t=5s
 */
@Database(entities = [Apps::class], version = 3, exportSchema = false)
abstract class AppsRoomDb : RoomDatabase() {

    abstract fun appsDao(): AppsDao

    companion object {
        //singleton prevents multiple instances opening simultaneously
        @Volatile
        private var INSTANCE: AppsRoomDb? = null

        fun getInstance(context: Context): AppsRoomDb {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppsRoomDb::class.java,
                    "apps_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}