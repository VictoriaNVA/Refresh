package com.example.refresh.getpackages


import android.graphics.drawable.Drawable

//This data class is used for apps in the list of all non-system installed apps
data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var checked: Boolean
)

