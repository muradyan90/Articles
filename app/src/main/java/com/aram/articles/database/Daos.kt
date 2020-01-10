package com.aram.articles.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface ArticlesDao{

    @Query("SELECT * FROM articles_table")
    fun getAllArticles(): List<ArticleEntity>

    @Query("SELECT * FROM articles_table")
    fun getAllLiveArticles(): LiveData<List<ArticleEntity>>

    @Query("SELECT * FROM articles_table WHERE id = :articleID")
    fun getArticle(articleID: String): ArticleEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertArticle(article: ArticleEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateArticle(article: ArticleEntity)

    @Delete
    fun deleteArticle(article: ArticleEntity)
}






















// NOT USED
@Dao
interface TappedArticleDao{

    @Query("SELECT * FROM tapped_articles_table WHERE id = :tappedArticleID")
    fun getTappedArticle(tappedArticleID: String): LiveData<TappedArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTappedArticle(article: TappedArticleEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateArticle(article: TappedArticleEntity)
}