package com.example.refresh.getpackages

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.refresh.R

/*RecyclerView Adapter for Packages Activity
 modified from PackageManager API from Digital Wellbeing experiments toolkit
 https://github.com/googlecreativelab/digital-wellbeing-experiments-toolkit/blob/master/
 appInteraction/app-list/app/src/main/java/com/digitalwellbeingexperiments/toolkit/applist/
 PackageManagerExtensions.kt*/

class AppListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<AppItem>()

    //instance of PackagesActivity
    private lateinit var packagesActivity: PackagesActivity

    fun setData(context: Context, newItems: List<ApplicationInfo>) {
        items.clear()
        items.addAll(newItems.map {
            val item = AppItem(
                context.packageManager.getApplicationLabel(it.packageName),
                it.packageName,
                context.packageManager.getApplicationIcon(it), false
            )
            item
        })
        notifyDataSetChanged()
        this.packagesActivity = context as PackagesActivity
    }

    //Inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_add_apps,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val appItem = items[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.app_name).text = appItem.name
            findViewById<ImageView>(R.id.app_icon).setImageDrawable(appItem.icon)
            val checkBox = findViewById<CheckBox>(R.id.checkBox)

            /*Code adapted from Abhijet Pokhrel's article on techdai:
             *"Working with RecyclerView in Android with Kotlin"*/

            //Set the current checked status of the checkbox
            checkBox.isChecked = appItem.checked
            //Listen to check status and update appItem
            //Call relevant methods to add selected items to a list
            //Update counter
            checkBox.setOnClickListener() {
                appItem.checked = checkBox.isChecked
                packagesActivity.startSelection(position)
                packagesActivity.check(it, position)
            }
            //Change state of checkboxes to false after the user pressed back arrow
            if (packagesActivity.toolbarTextView.visibility == View.GONE) {
                checkBox.isChecked = false
                appItem.checked = checkBox.isChecked
            }
        }
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)