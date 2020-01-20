package com.aram.articles.ui

import android.view.View
import android.webkit.WebView
import androidx.appcompat.widget.AppCompatImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aram.articles.R
import com.aram.articles.database.ArticleEntity
import com.aram.articles.viewmodels.ArticlesApiStatus
import com.aram.articles.viewmodels.checkNetworkStatus
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

val TAG = "LOG"

@BindingAdapter("imageUrl")
fun bindeImage(imageView: ImageView, imgUrl: String?) {
    if (imgUrl == null) {
        imageView.setImageResource(R.drawable.ic_broken_image)
        return
    }
    imgUrl.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imageView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(imageView)
    }
}

@BindingAdapter("itemsList", "itemsListNet")
fun bindRecyclerView(
    recyclerView: RecyclerView,
    dataDB: List<ArticleEntity>?,
    dataNet: List<ArticleEntity>?
) {
    val adapter = recyclerView.adapter as AllArticlesAdapter
    if (checkNetworkStatus(recyclerView.context) && dataDB != null) {
        adapter.setList(
            dataNet?.map { articleNet ->
                var article = articleNet
                dataDB?.forEach { articleDb ->
                    if (articleNet.id == articleDb.id) {
                        article = articleDb
                    }
                }
                article
            }?.filter { !it.isDeleted } ?: mutableListOf())
    } else if (checkNetworkStatus(recyclerView.context )) {
        adapter.setList(
            dataNet?.map { articleNet ->
                var article = articleNet
                dataDB?.forEach { articleDb ->
                    if (articleNet.id == articleDb.id) {
                        article = articleDb
                    }
                }
                article
            }?.filter { !it.isDeleted } ?: dataDB ?: mutableListOf()
        )
    } else {
        adapter.setList(dataDB?.filter { !it.isDeleted } ?: mutableListOf())
    }
}

@BindingAdapter("articleApiStatus")
fun bindStatus(statusImageView: ImageView, status: ArticlesApiStatus?) {
    when (status) {
        ArticlesApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        ArticlesApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        ArticlesApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
        ArticlesApiStatus.LOADINGMORE -> {
            statusImageView.visibility = View.GONE
        }
    }
}

@BindingAdapter("loadMoreStatus")
fun bindLoadMoreStatus(loadingLeyout: LinearLayout, status: ArticlesApiStatus?) {
    when (status) {
        ArticlesApiStatus.LOADINGMORE -> {
            loadingLeyout.visibility = View.VISIBLE
        }
        else -> {
            loadingLeyout.visibility = View.GONE
        }
    }
}

@BindingAdapter("loadUrl", "connection")
fun bindWebView(webView: WebView, webUrl: String?, networkConnection: Boolean) {
    when (networkConnection) {
        true -> {
            webView.visibility = View.VISIBLE
            webView.loadUrl(webUrl)
        }
        false -> {
            webView.visibility = View.GONE
        }
    }

}

@BindingAdapter("isLiked")
fun bindLikeButton(likeImageButton: AppCompatImageButton, isLiked: Boolean) {
    when (isLiked) {
        true -> {
            likeImageButton.setImageResource(R.drawable.ic_favorite_24px)
        }
        else -> {
            likeImageButton.setImageResource(R.drawable.ic_favorite_border_24px)
        }

    }
}

//@BindingAdapter("itemsList")
//fun bindRecyclerView(recyclerView: RecyclerView, data: List<ArticleEntity>?) {
//
//    (recyclerView.adapter as AllArticlesAdapter).apply {
//        setList(data?.filter { !it.isDeleted } ?: mutableListOf())
//    }
//}