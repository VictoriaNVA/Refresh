package com.example.refresh.data

import androidx.lifecycle.LiveData
import androidx.room.*

/*Database table for Applications to monitor.
  All database files were created following a 4 parts tutorial
  from Stevdza-San on Youtube. Part 1 is attached in link below
  https://www.youtube.com/watch?v=lwAvI3WDXBY&t=5s
 */
@Dao
interface AppsDao {
    @Query("SELECT * FROM Apps ORDER BY 'App Name' asc")
    fun getOrderedApps(): LiveData<List<Apps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(vararg apps: Apps)

    @Query("DELETE FROM apps")
    suspend fun removeAll()

    @Delete
    suspend fun removeApp(app: Apps)

}