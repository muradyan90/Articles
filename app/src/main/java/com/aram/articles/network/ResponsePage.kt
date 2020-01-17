package com.aram.articles.network

import android.os.Parcelable
import com.aram.articles.database.ArticleEntity
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

data class ResponsePage(
    val response: Response
)

data class Response(
    val pages: Int,
    val currentPage: Int,
    val total: Int,
    @Json(name = "results")
    val articles: List<Article>

)
@Parcelize
data class Article(
    val id: String,
    val type: String,
    val webTitle: String,
    val webUrl: String,
    val apiUrl: String,
    @Json(name = "fields")
    val imageUrl: ImageUrl?
): Parcelable

@Parcelize
data class ImageUrl(
    @Json(name = "thumbnail")
    val imgUrl:String?
): Parcelable

fun List<Article>.asArticlesEntity(): List<ArticleEntity>{
    return map {
        ArticleEntity(
            it.id,
            it.type,
            it.webTitle,
            it.webUrl,
            it.webUrl,
            it.imageUrl?.imgUrl,
            false,
            false
        )
    }
}