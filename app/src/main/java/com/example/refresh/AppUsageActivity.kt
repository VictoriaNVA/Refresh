package com.example.refresh

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.refresh.data.AppsViewModel
import java.util.*
import kotlin.properties.Delegates

@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
class AppUsageActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var appName: TextView
    private lateinit var appIcon: ImageView
    private lateinit var appPackageName: String
    private lateinit var appsViewModel: AppsViewModel

    //usage variables
    private lateinit var usageToday: TextView
    private lateinit var currentWeek: TextView
    private lateinit var dailyAvg: TextView
    private lateinit var weeklyAvg: TextView
    private var currentUsage by Delegates.notNull<Long>()

    //timer variables
    private lateinit var countDownText: TextView
    private lateinit var hourInput: EditText
    private lateinit var minInput: EditText
    private lateinit var setTimer: Button
    private lateinit var timerStartReset: Button
    private lateinit var countDownTimer: CountDownTimer
    private var startTimeMillis: Long = 0
    private var timeLeftMillis: Long = 0
    private var timerRunning: Boolean = false

    /* Countdown timer code adapted from Coding in Flow tutorial
    * Part1: https://codinginflow.com/tutorials/android/countdowntimer/part-1-countdown-timer
    * Part2: https://codinginflow.com/tutorials/android/countdowntimer/part-2-configuration-changes
    * Part4: https://codinginflow.com/tutorials/android/countdowntimer/part-4-time-input*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_usage_activity)

        //Db viewmodel
        appsViewModel = ViewModelProvider(this).get(AppsViewModel::class.java)

        //set up toolbar as action bar
        toolbar = findViewById(R.id.app_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true);

        //Get current app's name and icon to display on toolbar
        appName = findViewById(R.id.app_name)
        appIcon = findViewById(R.id.app_icon)
        appName.text = intent.getStringExtra("name")
        appPackageName = intent.getStringExtra("packageName").toString()
        val current: String = appPackageName
        appIcon.setImageDrawable(this.packageManager.getApplicationIcon(appPackageName))

        //Button even to delete the app
        findViewById<ImageButton>(R.id.delete_app_btn).setOnClickListener {
            Toast.makeText(this, "Successfully removed app", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            appsViewModel.getOrderedApps.observe(this, { app ->
                for (i in app.indices) {
                    if (app[i].packageName == appPackageName)
                        appsViewModel.removeApp(app[i])
                }
            })
        }

        //Usage stats textviews
        usageToday = findViewById(R.id.today_usage_stats)
        currentWeek = findViewById(R.id.week_usage_stats)
        dailyAvg = findViewById(R.id.daily_avg_stats)
        weeklyAvg = findViewById(R.id.weekly_avg_stats)

        //Timer layout elements
        countDownText = findViewById(R.id.countdown_timer)
        hourInput = findViewById(R.id.timer_hour)
        minInput = findViewById(R.id.timer_min)
        setTimer = findViewById(R.id.set_timer)
        timerStartReset = findViewById(R.id.start_reset)

        displayUsageStats()

        if (current == appPackageName) {
            //this code deals with the user input for countdown timer
            setTimer.setOnClickListener {
                val minutes: String = minInput.text.toString()
                val hours: String = hourInput.text.toString()
                if (minutes.isEmpty() || hours.isEmpty()) {
                    Toast.makeText(this, "Timer fields can't be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val minMillis: Long = minutes.toLong() * 60000
                val hMillis: Long = hours.toLong() * 60 * 60000
                val timerSum: Long = minMillis + hMillis

                setTime(timerSum)
                minInput.setText("")
                hourInput.setText("")
                minInput.clearFocus()
            }
            //Handle the start or reset button for the timer
            timerStartReset.setOnClickListener {
                if (timerRunning) {
                    resetTimer()
                } else {
                    startTimer()
                }
            }
        }
    }

    /*Code to hide the edittext keyboard from the screen
    * from Coding In Flow timer tutorial
    * https://codinginflow.com/tutorials/android/countdowntimer/part-4-time-input */
    private fun hideKeyboard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    //This function handles the usage statistics for the current app
    private fun displayUsageStats() {
        val usageStatsManager: UsageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        //Get calendar instance for stats
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)

        var day: Int = calendar.get(Calendar.DAY_OF_WEEK)
        var currentDayNo: Int = 0
        var week: Int = calendar.get(Calendar.WEEK_OF_MONTH)

        when (day) {
            Calendar.SATURDAY -> currentDayNo = 1
            Calendar.SUNDAY -> currentDayNo = 2
            Calendar.MONDAY -> currentDayNo = 3
            Calendar.TUESDAY -> currentDayNo = 4
            Calendar.WEDNESDAY -> currentDayNo = 5
            Calendar.THURSDAY -> currentDayNo = 6
            Calendar.FRIDAY -> currentDayNo = 7
        }
        //start and end times for queryUsageStats
        val startDay = System.currentTimeMillis() - 1000 * 3600 * 24
        val end = System.currentTimeMillis()

        //Query usage statistics for daily, weekly and monthly intervals
        val statsDay: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startDay, end)

        val statsWeek: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_WEEKLY,
            calendar.timeInMillis,
            end
        )
        val statsMonth: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_MONTHLY,
            calendar.timeInMillis,
            end
        )

        /*Get current app's usage for on-going day so far
        * Display in appropriate textview*/
        statsDay.forEach {
            if (appPackageName == it.packageName) {
                usageToday.text = toHourMin(it.totalTimeInForeground.toInt())
                currentUsage = it.totalTimeInForeground
            }
        }
        /*Get current app's usage for current week
        * Calculate daily average based on the week's usage
        * Display each in appropriate textview*/
        statsWeek.forEach {
            if (appPackageName == it.packageName) {
                currentWeek.text = toHourMin(it.totalTimeInForeground.toInt())
                dailyAvg.text = toHourMin(it.totalTimeInForeground.toInt() / currentDayNo)
            }
        }
        /*Get current app's average usage in a week based on month's usage
        * Display in appropriate textview*/
        statsMonth.forEach {
            if (appPackageName == it.packageName) {
                weeklyAvg.text = toHourMin(it.totalTimeInForeground.toInt() / week)
            }
        }

    }

    //Set countdown timer values and hide keyboard
    private fun setTime(milliseconds: Long) {
        startTimeMillis = milliseconds
        timeLeftMillis = startTimeMillis
        updateCountDown()
        hideKeyboard()
    }

    //Start the timer
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateCountDown()
            }

            override fun onFinish() {
                timerRunning = false
                timerStartReset.text = "START"
            }
        }.start()
        timerRunning = true
        //change the button to reset
        timerStartReset.text = "RESET"
    }

    //Resets the timer to default values
    private fun resetTimer() {
        countDownTimer.cancel()
        timerRunning = false
        timerStartReset.text = "START"
        timeLeftMillis = 0
        updateCountDown()
    }

    //Updates the time left
    private fun updateCountDown() {
        val hours = ((timeLeftMillis / 1000) / 3600).toInt()
        val minutes = (((timeLeftMillis / 1000) % 3600) / 60).toInt()
        val seconds = ((timeLeftMillis / 1000) % 60).toInt()

        val timeLeft: String =
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        countDownText.text = timeLeft
    }

    /*Convert millis to hours & minutes
    * Code from StackOverflow answer
    * Could no longer find source link. */
    private fun toHourMin(sum: Int): CharSequence {
        val minutes = sum / 1000 / 60
        val hours = minutes / 60
        var hour = ""
        if (hours >= 1) {
            hour += "${hours}h "
        }
        hour += "${minutes - hours * 60}min"
        return hour
    }


    //Handle the toolbar back arrow being clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
