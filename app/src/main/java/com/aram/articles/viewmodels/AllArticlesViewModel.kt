package com.aram.articles.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.aram.articles.database.*
import com.aram.articles.repository.ArticlesRepository
import org.koin.core.KoinComponent
import org.koin.core.inject


class AllArticlesViewModel(
    val app: Application,
    val articlesDao: ArticlesDao,
    tappedArticleDao: TappedArticleDao
) : AndroidViewModel(app), KoinComponent {

    // INJECTED BY KOIN
    val repository: ArticlesRepository by inject()


    var articles = repository.articles

    private var _articlesFromNet: MutableLiveData<List<ArticleEntity>> =
        repository.getFirstPageRx()
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
            repository.getNextPageRx()
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
        repository.compositeDisposable.clear()
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