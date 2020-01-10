package com.aram.articles.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.aram.articles.R
import com.aram.articles.database.ArticleEntity
import com.aram.articles.databinding.ItemLayoutBinding
import com.aram.articles.databinding.LiveblogItemLayoutBinding

class AllArticlesAdapter(private val ClickListener: OnClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG = "LOG"
    var articlesList = mutableListOf<ArticleEntity>()
    var listItemCount = articlesList.size
    var adapterPosition = 0


    fun setList(list: List<ArticleEntity>) {
        articlesList.clear()
        articlesList.addAll(list)
        listItemCount = articlesList.size
        notifyDataSetChanged()
    }



    // List and adapter position set up methods to notify only changed items ,
    // but don't work properly for infinity scrolling when connection turned off.
    // Must notify by adapter position

    /*fun setingAdapterPosition(adapterPosition: Int){
        this.adapterPosition = adapterPosition
    }*/

   /* fun setList(list: List<ArticleEntity>) {
        if (list.size == articlesList.size && list.isNotEmpty()) {
            for (indices in 0 until articlesList.size) {
                if (articlesList[indices] != list[indices]) {
                    articlesList[indices] = list[indices]
                    notifyItemChanged(adapterPosition)
                }
            }
        }

        if (list.size > articlesList.size) {
            articlesList.addAll(list.subList(listItemCount, list.size))
            notifyItemRangeInserted(listItemCount,list.size - listItemCount)
            listItemCount = articlesList.size
        } else if (list.size < articlesList.size) {
            for (indices in 0 until articlesList.size) {
                if (articlesList[indices] != list[indices]) {
                    articlesList.removeAt(indices)
                    listItemCount = articlesList.size
                    notifyItemRemoved(adapterPosition)
                    return
                }
            }
        }
    }*/

    override fun getItemViewType(position: Int): Int {
        val article = articlesList[position % listItemCount]
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
        return when (listItemCount) {
            0 -> 0
            else -> Int.MAX_VALUE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val article = articlesList[position % listItemCount]
        when (holder) {
            is ArticleViewHolder -> holder.bind(article)
            is LiveblogArticleViewHolder -> holder.bind(article)
        }
    }

    fun getItemByPosition(position: Int): ArticleEntity {
        return articlesList[position % listItemCount]
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

            if (listItemCount - adapterPosition % listItemCount == 1
            ) {
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
               // setingAdapterPosition(adapterPosition)
                ClickListener.onLikeClick(article,adapterPosition)
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


            if (listItemCount - adapterPosition % listItemCount == 1) {
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
               // setingAdapterPosition(adapterPosition)
                ClickListener.onLikeClick(article,adapterPosition)
            }
            binding.executePendingBindings()
        }
    }

    interface OnClickListener {
        fun onItemClick(article: ArticleEntity, imageView: ImageView)
        fun onLikeClick(article: ArticleEntity,adapterPosition: Int)
        fun scrollListener(position: Int)
    }
}