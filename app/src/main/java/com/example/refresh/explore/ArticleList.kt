package com.example.refresh.explore

import com.example.refresh.R

class ArticleList {
    fun loadArticles(): List<Article> {
        return listOf(
            Article(R.string.article1, R.string.time1, R.string.link1, R.drawable.image1),
            Article(R.string.article2, R.string.time2, R.string.link2, R.drawable.image2),
            Article(R.string.article3, R.string.time3, R.string.link3, R.drawable.image3),
            Article(R.string.article4, R.string.time4, R.string.link4, R.drawable.image4),
            Article(R.string.article5, R.string.time5, R.string.link5, R.drawable.image5),
            Article(R.string.article6, R.string.time6, R.string.link6, R.drawable.image6),
            Article(R.string.article7, R.string.time7, R.string.link7, R.drawable.image7)
        )
    }
}