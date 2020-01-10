package com.aram.articles.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
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
import kotlinx.coroutines.*
import kotlin.properties.Delegates


class AllArticlesViewModel(
    val app: Application,
    val articlesDao: ArticlesDao,
    tappedArticleDao: TappedArticleDao
) : AndroidViewModel(app) {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val CURENT_PAGE = "curent_page"
    private val sharedPref: SharedPreferences =
        app.getSharedPreferences(CURENT_PAGE, MODE_PRIVATE)
    private val repository = ArticlesRepository.getInstance(app, articlesDao)

    var articles = repository.articles

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
        val curentPage = sharedPref.getInt(CURENT_PAGE, 0)
        if (curentPage == 0)
            getNextPage()
    }

    fun getNextPage() {
        if (checkNetworkStatus(app)) {
            coroutineScope.launch {
                repository.getNextPage()
                repository.status.observeForever(observer)
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
fun checkNetworkStatus(application: Application): Boolean {
    val connectivityManager: ConnectivityManager =
        application.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return (networkInfo != null && networkInfo.isConnected)

}