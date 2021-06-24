package com.example.refresh

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.example.refresh.data.Apps
import com.example.refresh.data.AppsViewModel
import com.example.refresh.explore.ExploreActivity
import com.example.refresh.getpackages.PackagesActivity
import com.example.refresh.getpackages.getApplicationLabel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var totalUsage: TextView
    private lateinit var appsViewModel: AppsViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var pieChart: AnyChartView
    private lateinit var chart: Pie
    private lateinit var bottomNavigationView: BottomNavigationView
    private var deleted: Boolean = false
    private var dataset: MutableList<ValueDataEntry> = mutableListOf()
    private var chartData: ArrayList<DataEntry> = arrayListOf()
    private var statsDay: List<UsageStats> = listOf()
    private var monitoredApps: ArrayList<Apps> = arrayListOf()
    private val appListAdapter = MainActivityAdapter()

    //get calendar instance for stats
    private val calendar: Calendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reqPermission()

        pieChart = findViewById(R.id.pie_chart)
        totalUsage = findViewById(R.id.usage)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.summary
        setupBottomNavigationView()

        //Apps View Model
        appsViewModel = ViewModelProvider(this).get(AppsViewModel::class.java)
        //Get apps from database
        appsViewModel.getOrderedApps.observe(this, { app ->
            appListAdapter.setData(this, app)
            chartApps(app)
        })

        //RecyclerView
        findViewById<RecyclerView>(R.id.rec).apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 2)
            adapter = appListAdapter
            recycler = this
        }

        //Floating action button to open Package Manager activity
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, PackagesActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Delete all apps
        findViewById<ImageButton>(R.id.delete_btn).setOnClickListener {
            pieChart.visibility = View.GONE
            totalUsage.visibility = View.GONE
            deleted = true
            appsViewModel.removeAll()
            Toast.makeText(this, "Successfully deleted all", Toast.LENGTH_SHORT).show()
        }
    }

    //Asking for app usage access if not allowed (opens system settings)
    //This is a special permission, so user can only allow from settings
    private fun reqPermission() {
        if (!checkPermissions(this)) {
            AlertDialog.Builder(this)
                .setTitle("Allow Permissions")
                .setMessage("Please allow usage permissions to enjoy the app's usage tracking features.")
                .setPositiveButton("App Settings", DialogInterface.OnClickListener { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                })
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    //Method checks if usage stats permissions are allowed
    //Return true if allowed, false otherwise
    private fun checkPermissions(activity: AppCompatActivity): Boolean {
        val packageManager: PackageManager = activity.packageManager
        val appInfo: ApplicationInfo =
            packageManager.getApplicationInfo(activity.packageName, 0)
        val appOps: AppOpsManager = activity.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        //Using deprecated checkOpNoThrow because unsafeCheckOpNoThrow alternative requires API 29 minimum
        val mode: Int = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            activity.applicationInfo.uid, activity.applicationInfo.packageName
        )
        return (mode == AppOpsManager.MODE_ALLOWED)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)

    private fun getDailyUsage(pkg: String?): Long {
        //Set calendar
        calendar.add(Calendar.YEAR, -1)
        //Start and end times for queryUsageStats
        val startDay = System.currentTimeMillis() - 1000 * 3600 * 24
        val end = System.currentTimeMillis()

        //Get UsageStatsManager instance
        val usageStatsManager: UsageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        //Query usage statistics for daily, weekly and monthly intervals
        statsDay =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startDay, end)

        //Loop through stats and get usage for the target app if package names match
        var usage: Long = 0
        statsDay.forEach {
            if (pkg == it.packageName) {
                usage = it.totalTimeInForeground
            }
        }
        //Log used for debugging
        Log.d("App_Usage", pkg + usage.toString())

        return usage
    }

    /*AnyChart API Github:
    * https://github.com/AnyChart/AnyChart-Android
    * Tutorial followed for setting up chart:
    * https://www.youtube.com/watch?v=qWBA2ikLJjU*/

    //This method puts the applications in a pie chart based on usage
    private fun chartApps(apps: List<Apps>) {
        //Add apps from the database into a list
        monitoredApps.addAll(apps.map {
            val item = Apps(
                0,
                this.packageManager.getApplicationLabel(it.packageName),
                it.packageName
            )
            item
        })

        //Log used for debugging
        if (monitoredApps.isNotEmpty()) {
            Log.d("CHECK_LISTS", monitoredApps.toString())
        }

        //Loop through list of applications
        //Assign name & daily usage to data set
        for (i in monitoredApps.indices) {
            val name = monitoredApps[i].name
            val usage = getDailyUsage(monitoredApps[i].packageName)
            dataset.add(ValueDataEntry(name, usage))
        }

        //Log used for debugging
        if (dataset.isNotEmpty()) {
            Log.d("CHECK_LISTS", dataset.toString())
        }

        //AnyChart API: setting pie chart and its data
        chart = AnyChart.pie()
        chartData.addAll(dataset)
        chart.data(chartData)
        pieChart.setChart(chart)

        //Get total daily usage for monitored apps
        displayTotalUsage()
    }

    //This gets the total day
    private fun displayTotalUsage() {
        var sum: Long = 0
        var usage: Long = 0

        //Loop through list of apps and list of statistics
        //If package names match, save usage and add it to the total
        for (i in monitoredApps.indices) {
            statsDay.forEach {
                if (monitoredApps[i].packageName == it.packageName) {
                    usage = it.totalTimeInForeground
                    Log.d("CHECK_LISTS", it.totalTimeInForeground.toString())
                }
            }
            sum += usage
        }

        //Convert millis to hours & minutes
        //Code from StackOverflow answer
        //Could no longer find source link.
        val minutes = sum / 1000 / 60
        val hours = minutes / 60
        var hour = ""
        if (hours >= 1) {
            hour += "${hours}h "
        }
        hour += "${minutes - hours * 60}min"

        //Set total usage to the sum in hours & minutes if it's not deleted
        if (!deleted) {
            totalUsage.text = "Total usage today: $hour"
            totalUsage.visibility = View.VISIBLE
            deleted = false
        }
    }

    //Bottom navigation
    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.explore) {
                val intent = Intent(this, ExploreActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            true
        }
    }
}

