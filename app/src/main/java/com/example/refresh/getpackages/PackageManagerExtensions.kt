package com.example.refresh.getpackages

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/* This code is from the Package Manager API from Google's Digital Wellbeing experiments
* Modified to the app's needs (only getting non system apps, their pkgs name, label & icon)*/
fun PackageManager.getNonSysPackages(context: Context): List<ApplicationInfo> {
    return queryIntentActivities(
        Intent(
            Intent.ACTION_MAIN,
            null
        ).addCategory(Intent.CATEGORY_LAUNCHER), 0
    )
        .filter { it.activityInfo.packageName != context.packageName }
        .distinctBy { it.activityInfo.packageName }
        .map { getApplicationInfo(it.activityInfo.packageName, 0) }
        .sortedBy { this.getApplicationLabel(it).toString() }

}

fun PackageManager.getApplicationLabel(packageName: String): String {
    val info = getApplicationInfo(packageName, 0)
    return getApplicationLabel(info).toString()
}