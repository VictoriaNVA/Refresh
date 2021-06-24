package com.example.refresh.explore

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Article(
    @StringRes val titleId: Int,
    @StringRes val timeReadId: Int,
    @StringRes val linkId: Int,
    @DrawableRes val imageId: Int
)
