package com.example.refresh.explore

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.refresh.R


class ExploreListAdapter(
    private val context: Context,
    private val dataset: List<Article>
) : RecyclerView.Adapter<ExploreListAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.item_title)
        val imageView: ImageView = view.findViewById(R.id.item_image)
        val readTime: TextView = view.findViewById(R.id.item_readtime)
        val cardView: CardView = view.findViewById(R.id.article_card_view)
    }

    private lateinit var exploreActivity: ExploreActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // inflate layout
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_explore, parent, false)
        this.exploreActivity = context as ExploreActivity
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]

        //set cardview data with dataset from article list
        holder.title.text = context.resources.getString(item.titleId)
        holder.imageView.setImageResource(item.imageId)
        holder.readTime.text = context.resources.getString(item.timeReadId)

        val url = context.resources.getString(item.linkId)

        //When a cardview is clicked, take user to the full article on browser
        holder.cardView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            exploreActivity.startActivity(intent)
        }

    }

    override fun getItemCount() = dataset.size
}