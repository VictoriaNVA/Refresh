package com.example.refresh

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.refresh.data.Apps
import com.example.refresh.getpackages.getApplicationLabel

class MainActivityAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val apps = ArrayList<Apps>()
    private lateinit var mainActivity: MainActivity

    //This function takes the details of the apps saved in the database
    fun setData(context: Context, savedApps: List<Apps>): ArrayList<Apps> {
        apps.clear()
        apps.addAll(savedApps.map {
            val item = Apps(
                0,
                context.packageManager.getApplicationLabel(it.packageName),
                it.packageName
            )
            item
        })
        notifyDataSetChanged()
        //Setting context for launching new activity later
        this.mainActivity = context as MainActivity
        return apps
    }

    //Inflate the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cardview_apps,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val appItem = apps[position]
        //Display app name and icon for on card view
        holder.itemView.apply {
            findViewById<TextView>(R.id.name).text = appItem.name
            findViewById<ImageView>(R.id.icon).setImageDrawable(
                context.packageManager.getApplicationIcon(
                    appItem.packageName
                )
            )
            val appName = findViewById<TextView>(R.id.name)
            //Click listener for card view creates an intent to open AppUsageActivity
            //Also "saves" the selected app's name and package name to use in next Activity
            findViewById<CardView>(R.id.card_view).setOnClickListener {
                val intent = Intent(mainActivity, AppUsageActivity::class.java)
                intent.putExtra("name", appName.text)
                if (appItem.name == appName.text) {
                    intent.putExtra("packageName", appItem.packageName)
                }
                mainActivity.startActivity(intent)
            }
        }
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)