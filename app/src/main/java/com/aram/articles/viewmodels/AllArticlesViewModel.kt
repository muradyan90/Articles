package com.aram.articles.viewmodels

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.Observable
import androidx.lifecycle.*
import com.aram.articles.R
import com.aram.articles.database.*
import com.aram.articles.network.Article
import com.aram.articles.network.ArticlesApi
import com.aram.articles.network.ImageUrl
import com.aram.articles.network.asArticlesEntity
import com.aram.articles.repository.ArticlesRepository
import com.aram.articles.service.BackgroundTask
import com.aram.articles.ui.TAG
import kotlinx.coroutines.*
import kotlin.properties.Delegates


class AllArticlesViewModel(
    val app: Application,
    val articlesDao: ArticlesDao,
    tappedArticleDao: TappedArticleDao
) : AndroidViewModel(app) {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val repository = ArticlesRepository.getInstance(app, articlesDao)

    var articles = repository.articles

    private var _articlesFromNet: MutableLiveData<List<ArticleEntity>> =
        repository.getFirstPage()
    val articlesFromNet: LiveData<List<ArticleEntity>>
        get() = _articlesFromNet


    private var _status = MutableLiveData<ArticlesApiStatus>()
    val status: LiveData<ArticlesApiStatus>
        get() = _status

    private var _toast = MutableLiveData<Boolean>()
    val toast: LiveData<Boolean>
        get() = _toast

    private var _navigateToSelectedArticle = MutableLiveData<ArticleEntity>()
    val navigateToSelectedArticle: LiveData<ArticleEntity>
        get() = _navigateToSelectedArticle


    private val observer = Observer<ArticlesApiStatus> {
        _status.value = it
    }

    init {
        repository.status.observeForever(observer)
    }

    fun getNextPage() {
        if (checkNetworkStatus(app)) {
            coroutineScope.launch {
                repository.getNextPage()
            }
        } else {
            displayToast()
        }
    }

    fun displayArticleDetails(article: ArticleEntity) {
        _navigateToSelectedArticle.value = article
    }

    fun displayArticleDetailsComplete() {
        _navigateToSelectedArticle.value = null
    }

    fun displayToast() {
        _toast.value = true
    }

    fun displayToastComplete() {
        _toast.value = false
    }

    fun onLikeClick(article: ArticleEntity) {
        repository.saveLikedState(article)
    }

    fun deleteArticle(article: ArticleEntity) {
        repository.deleteArticle(article)
    }

    override fun onCleared() {
        super.onCleared()
        repository.status.removeObserver(observer)
        viewModelJob.cancel()
    }

}

enum class ArticlesApiStatus { LOADING, ERROR, DONE, LOADINGMORE }

// FUNCTION TO CHECK CONNECTION
@Suppress("DEPRECATION")
fun checkNetworkStatus(context: Context): Boolean {
    val connectivityManager: ConnectivityManager =
        context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return (networkInfo != null && networkInfo.isConnected)

}