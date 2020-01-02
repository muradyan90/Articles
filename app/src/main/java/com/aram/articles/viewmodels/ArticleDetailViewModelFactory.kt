package com.aram.articles.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aram.articles.database.ArticleEntity
import com.aram.articles.network.Article
import java.lang.IllegalArgumentException

class ArticleDetailViewModelFactory(
    private val article: ArticleEntity,
    private val application: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
       if(modelClass.isAssignableFrom(ArticleDetailsViewModel::class.java)){
           return ArticleDetailsViewModel(article,application) as T
       }
       throw IllegalArgumentException("Unknown view model class")
    }
}