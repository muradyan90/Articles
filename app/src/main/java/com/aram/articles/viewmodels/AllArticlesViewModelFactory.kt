package com.aram.articles.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aram.articles.database.ArticlesDao
import com.aram.articles.database.TappedArticleDao
import com.aram.articles.network.Article
import java.lang.IllegalArgumentException

class AllArticlesViewModelFactory(
    private val application: Application,
    private val articlesDao: ArticlesDao,
    private val tappedArticleDao: TappedArticleDao
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
       if(modelClass.isAssignableFrom(AllArticlesViewModel::class.java)){
           return AllArticlesViewModel(application,articlesDao,tappedArticleDao) as T
       }
       throw IllegalArgumentException("Unknown view model class")
    }
}