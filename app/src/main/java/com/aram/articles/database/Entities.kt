package com.aram.articles.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aram.articles.network.Article
import com.aram.articles.network.ImageUrl
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "articles_table")
data class ArticleEntity (
    @PrimaryKey
    val id: String,
    val type: String,
    @ColumnInfo(name = "web_title")
    val webTitle: String,
    @ColumnInfo(name = "web_url")
    val webUrl: String,
    @ColumnInfo(name = "api_url")
    val apiUrl: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "is_liked")
    val isLiked: Boolean,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean
): Parcelable

































// NOTE USED

fun List<ArticleEntity>.asNetArticle(): List<Article>{
   return map { Article(
       it.id,
       it.type,
       it.webTitle,
       it.webUrl,
       it.webUrl,
       ImageUrl(it.imageUrl)
//       it.isLiked,
//       it.isDeleted
   )
   }
}
@Parcelize
@Entity(tableName = "tapped_articles_table")
data class TappedArticleEntity (
    @PrimaryKey
    val id: String,
    val type: String,
    @ColumnInfo(name = "web_title")
    val webTitle: String,
    @ColumnInfo(name = "web_url")
    val webUrl: String,
    @ColumnInfo(name = "api_url")
    val apiUrl: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String
//    @ColumnInfo(name = "is_liked")
//    val isLiked: Boolean,
//    @ColumnInfo(name = "is_deleted")
//    val isDeleted: Boolean
): Parcelable

