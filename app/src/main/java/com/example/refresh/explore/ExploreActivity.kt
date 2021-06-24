package com.example.refresh.explore

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.refresh.MainActivity
import com.example.refresh.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ExploreActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.explore_activity)

        //Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.explore
        setupBottomNavigationView()

        //RecyclerView
        val myDataset = ArticleList().loadArticles()
        findViewById<RecyclerView>(R.id.recycler_explore).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = ExploreListAdapter(this@ExploreActivity, myDataset)
            recycler = this
            setHasFixedSize(true)
        }
    }

    //Bottom navigation
    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.summary) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            true
        }
    }
}