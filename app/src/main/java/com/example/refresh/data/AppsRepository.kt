package com.example.refresh.data

import androidx.lifecycle.LiveData

/*Database table for Applications to monitor.
  All database files were created following a 4 parts tutorial
  from Stevdza-San on Youtube. Part 1 is attached in link below
  https://www.youtube.com/watch?v=lwAvI3WDXBY&t=5s
 */
class AppsRepository(private val appsDao: AppsDao) {

    val getOrderedApps: LiveData<List<Apps>> = appsDao.getOrderedApps()

    suspend fun insertApps(vararg apps: Apps) {
        appsDao.insertApps(*apps)
    }

    suspend fun removeAll() {
        appsDao.removeAll()
    }

    suspend fun removeApp(app: Apps) {
        appsDao.removeApp(app)
    }
}