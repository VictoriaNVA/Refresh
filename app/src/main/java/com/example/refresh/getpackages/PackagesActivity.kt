package com.example.refresh.getpackages

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.refresh.MainActivity
import com.example.refresh.R
import com.example.refresh.data.Apps
import com.example.refresh.data.AppsViewModel
import kotlinx.coroutines.launch

/*toolbar as actionbar code adapted from tutorial:
* https://www.youtube.com/watch?v=nC9E9dvw2eY*/

/* Code for selecting checkboxes was created following Java tutorials & examples:
* https://blog.oziomaogbe.com/2017/10/18/android-handling-checkbox-state-in-recycler-views.html
* https://www.youtube.com/watch?v=z9g7G3oX89w
* https://www.codota.com/code/java/methods/android.widget.CheckBox/isChecked*/

class PackagesActivity : AppCompatActivity() {
    private val appListAdapter = AppListAdapter()
    private var apps: List<ApplicationInfo> = listOf()
    private val selectedAppsList: MutableList<ApplicationInfo> = mutableListOf()
    private lateinit var appsViewModel: AppsViewModel
    private var counter = 0
    private lateinit var recycler: RecyclerView
    private lateinit var loading: View
    private lateinit var toolbar: Toolbar
    lateinit var toolbarTextView: TextView
    private lateinit var cancelSelection: ImageButton
    private var isActionMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.packages_activity)
        //RecyclerView
        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = appListAdapter
            recycler = this
        }

        //Get View Model
        appsViewModel = ViewModelProvider(this).get(AppsViewModel::class.java)
        //Loading
        loading = findViewById(R.id.loading)
        //Set up toolbar as actionbar
        toolbar = findViewById(R.id.cab)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true);

        //This shows how many items are selected and is initially hidden
        toolbarTextView = findViewById(R.id.text_cab)
        toolbarTextView.visibility = View.GONE

        //This allows users to cancel selection and is initially hidden
        cancelSelection = findViewById(R.id.cancel)
        cancelSelection.visibility = View.GONE
        cancelSelection.setOnClickListener {
            clearActionMode()
        }
    }

    //This cancels all selected items, dealing with UI changes
    private fun clearActionMode() {
        isActionMode = false
        toolbarTextView.visibility = View.GONE
        toolbarTextView.text = "0 items selected"
        cancelSelection.visibility = View.GONE
        counter = 0
        selectedAppsList.clear()
        toolbar.menu.clear()
        appListAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    //Getting all non-system installed apps
    private fun refreshData() {
        val context = this
        bgScope.launch {
            apps = packageManager.getNonSysPackages(context)
            mainScope.launch {
                appListAdapter.setData(context, apps)
                loading.visibility = View.INVISIBLE
            }
        }
    }

    //Method for when items first get selected
    fun startSelection(index: Int) {
        if (!isActionMode) {
            isActionMode = true
            selectedAppsList.add(apps[index])
            toolbarTextView.visibility = View.VISIBLE
            cancelSelection.visibility = View.VISIBLE
            toolbar.inflateMenu(R.menu.contextual_menu)
            appListAdapter.notifyDataSetChanged()
        }
    }

    /*This method deals with checkboxes (checking/unchecking)
    * And adds/deletes items from the app list accordingly*/
    fun check(v: View, index: Int) {
        val checkBox: CheckBox = v as CheckBox
        if (checkBox.isChecked) {
            selectedAppsList.add(apps[index])
            counter++
            updateToolbarText(counter)

        } else {
            selectedAppsList.remove(apps[index])
            counter--
            updateToolbarText(counter)
        }
    }

    //This updates the number of items selected showing on toolbar
    private fun updateToolbarText(counter: Int) {
        if (counter == 1) {
            toolbarTextView.text = "1 item selected"
        } else {
            toolbarTextView.text = "$counter items selected"
        }
    }

    //This adds selected apps to the database and goes to the Summary page
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        if (item.itemId == R.id.action_confirm && selectedAppsList.isNotEmpty()) {
            insertToDatabase()
            startActivity(intent)
            finish()
        }
        if (item.itemId == android.R.id.home) {
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //Adding the names and package names of selected apps to the database
    private fun insertToDatabase() {
        if (selectedAppsList.isNotEmpty()) {
            val iterator = selectedAppsList.iterator()
            iterator.forEach {
                val app =
                    Apps(0, this.packageManager.getApplicationLabel(it.packageName), it.packageName)
                //Add data to database
                appsViewModel.insertApps(app)
                Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No apps selected", Toast.LENGTH_SHORT).show()
        }
    }
}

