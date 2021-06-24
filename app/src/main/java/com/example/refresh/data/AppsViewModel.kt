package com.example.refresh.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*Database table for Applications to monitor.
  All database files were created following a 4 parts tutorial
  from Stevdza-San on Youtube. Part 1 is attached in link below
  https://www.youtube.com/watch?v=lwAvI3WDXBY&t=5s
 */
class AppsViewModel(application: Application) : AndroidViewModel(application) {
    val getOrderedApps: LiveData<List<Apps>>
    private val repository: AppsRepository

    init {
        val appsDao = AppsRoomDb.getInstance(application).appsDao()
        repository = AppsRepository(appsDao)
        getOrderedApps = repository.getOrderedApps
    }

    //Ensuring db operations are not executed on main UI thread
    fun insertApps(vararg apps: Apps) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertApps(*apps)
        }
    }

    fun removeAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeAll()
        }
    }

    fun removeApp(app: Apps) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeApp(app)
        }
    }
}