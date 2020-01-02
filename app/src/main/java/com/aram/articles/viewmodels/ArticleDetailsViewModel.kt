package com.aram.articles.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aram.articles.database.ArticleEntity

class ArticleDetailsViewModel(selectedArticle: ArticleEntity,val app: Application) :
    AndroidViewModel(app) {

    private val _selectedArticle = MutableLiveData<ArticleEntity>()
    val selectedArticle: LiveData<ArticleEntity>
    get() = _selectedArticle

    init{
        _selectedArticle.value = selectedArticle
    }

    private val _networkConnection = MutableLiveData<Boolean>()
    val networkConnection: LiveData<Boolean>
    get() = _networkConnection

    init{
        _networkConnection.value = checkNetworkStatus(app)
    }


}