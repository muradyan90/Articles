package com.aram.articles.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aram.articles.R
import com.aram.articles.database.ArticleEntity
import com.aram.articles.databinding.ItemLayoutBinding
import com.aram.articles.databinding.LiveblogItemLayoutBinding

class AllArticlesAdapter(private val ClickListener: OnClickListener) :
    ListAdapter<ArticleEntity, RecyclerView.ViewHolder>(DiffCallback) {

    var listItemCount = 0
    val TAG = "LOG"

    // SOMETIMES PASSING OF LIST FROM BINDING ADAPTER IS GETTING LATE
    // AND IT CAUSES A IndexOutOfBoundsException
    // fun bindRecyclerView() from BindingAdapters file
    override fun getItemViewType(position: Int): Int {
        Log.d(TAG, "RV - item type: $position | $listItemCount")
        var article = getItem(0)
        try {
            article = getItem(position % listItemCount)
        } catch (e: IndexOutOfBoundsException) {
        }
        return when (article.type) {
            "liveblog" -> R.layout.liveblog_item_layout
            else -> R.layout.item_layout
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.liveblog_item_layout -> LiveblogArticleViewHolder(
                LiveblogItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context)
                ), ClickListener
            )
            else -> ArticleViewHolder(
                ItemLayoutBinding.inflate(LayoutInflater.from(parent.context)),
                ClickListener
            )
        }
    }

    override fun getItemCount(): Int {
        return listItemCount * 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var article = getItem(0)
        try {
            article = getItem(position % listItemCount)
        } catch (e: IndexOutOfBoundsException) {
        }
        when (holder) {
            is ArticleViewHolder -> holder.bind(article)
            is LiveblogArticleViewHolder -> holder.bind(article)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ArticleEntity>() {
        override fun areItemsTheSame(oldItem: ArticleEntity, newItem: ArticleEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ArticleEntity, newItem: ArticleEntity): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun getItemByPosition(position: Int): ArticleEntity {
        return getItem(position)
    }

    // ARTICLE HOLDER
    inner class ArticleViewHolder(
        private var binding: ItemLayoutBinding,
        private val ClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleEntity) {
            binding.article = article
            binding.executePendingBindings()

            //SHARED ELEMENT
            ViewCompat.setTransitionName(binding.itemViewImg, "imageView-$adapterPosition")

            if (itemCount / 2 - adapterPosition == 1) {
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return
                ClickListener.scrollListener(adapterPosition)
            }

            binding.root.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return@setOnClickListener
                ClickListener.onItemClick(article, binding.itemViewImg)
            }
            binding.favoriteImgBtn.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return@setOnClickListener
                ClickListener.onLikeClick(article)
            }
        }
    }

    //  LIVE BLOG HOLDER
    inner class LiveblogArticleViewHolder(
        private var binding: LiveblogItemLayoutBinding,
        private val ClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleEntity) {
            binding.article = article

            //SHARED ELEMENT
            ViewCompat.setTransitionName(binding.itemViewImg, "imageView-$adapterPosition")

            if (itemCount / 2 - adapterPosition == 1) {
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return
                ClickListener.scrollListener(adapterPosition)
            }
            binding.root.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return@setOnClickListener
                ClickListener.onItemClick(article, binding.itemViewImg)
            }
            binding.favoriteImgBtn.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return@setOnClickListener
                ClickListener.onLikeClick(article)
            }
            binding.executePendingBindings()
        }
    }

    interface OnClickListener {
        fun onItemClick(article: ArticleEntity, imageView: ImageView)
        fun onLikeClick(article: ArticleEntity)
        fun scrollListener(position: Int)
    }
}