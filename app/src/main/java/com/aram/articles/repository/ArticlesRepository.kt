package com.aram.articles.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aram.articles.database.ArticleEntity
import com.aram.articles.database.ArticlesDao
import com.aram.articles.network.Article
import com.aram.articles.network.ArticlesApi
import com.aram.articles.network.asArticlesEntity
import com.aram.articles.viewmodels.ArticlesApiStatus
import com.aram.articles.viewmodels.checkNetworkStatus
import kotlinx.coroutines.*

class ArticlesRepository(
    private val app: Application,
    private val articlesDao: ArticlesDao
) {
    private val TAG = "LOG"
    private var repoJob = Job()
    private val coroutineScope = CoroutineScope(repoJob + Dispatchers.Main)
    private val CURENT_PAGE = "curent_page"
    private val TOTAL_PAGES = "total_page"
    private val TOTAL_ARTICLES = "total_articles"
    private val sharedPref: SharedPreferences =
        app.getSharedPreferences(CURENT_PAGE, Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()

    private var _status = MutableLiveData<ArticlesApiStatus>()
    val status: LiveData<ArticlesApiStatus>
        get() = _status

    var articles = articlesDao.getAllLiveArticles()


    fun getNextPage() {
        val curentPage = sharedPref.getInt(CURENT_PAGE, 0)
        val totalPages = sharedPref.getInt(TOTAL_PAGES, 1)

        coroutineScope.launch {
            val nextPage = curentPage + 1
            if (nextPage <= totalPages) {
                ArticlesApi.retrofitService.getArticles(nextPage).let {
                    try {
                        if (curentPage == 0) {
                            _status.value = ArticlesApiStatus.LOADING
                        } else {
                            _status.value = ArticlesApiStatus.LOADINGMORE
                        }
                        it.await().apply {
                            _status.value = ArticlesApiStatus.DONE
                            savePagesInfo(
                                response.currentPage,
                                response.pages,
                                response.total
                            )
                            saveArticlesInDatabase(response.articles)
                        }
                    } catch (e: Exception) {
                        _status.value = ArticlesApiStatus.ERROR
                    }
                }
            }
        }
    }

    suspend fun saveArticlesInDatabase(articlesFromNet: List<Article>) {
        return withContext(Dispatchers.IO) {
            articlesFromNet.asArticlesEntity().forEach { articlesDao.insertArticle(it) }
        }
    }

    fun saveLikedState(article: ArticleEntity) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val oldArticle = articlesDao.getArticle(article.id)
                val newArticle = oldArticle.copy(isLiked = !oldArticle.isLiked)
                articlesDao.updateArticle(newArticle)
            }
        }

    }

    fun deleteArticle(article: ArticleEntity) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val oldArticle = articlesDao.getArticle(article.id)
                val newArticle = oldArticle.copy(isDeleted = true)
                articlesDao.updateArticle(newArticle)
            }
        }
    }

    suspend fun searchNewArticles(): ArticleEntity? {
        var lastNewArticle: ArticleEntity? = null
        val totalArticles = sharedPref.getInt(TOTAL_ARTICLES, -1)
        if (totalArticles > -1 && checkNetworkStatus(app)) {

            coroutineScope.launch {
                // getting info about new articles from net
                val resultPage = ArticlesApi.retrofitService.getNewArticles(1, 1).await()
                val newTotalArticles = resultPage.response.total
                val lastPage = resultPage.response.pages
                if (newTotalArticles > totalArticles && checkNetworkStatus(app)) {
                    editor.putInt(TOTAL_ARTICLES, newTotalArticles)
                    // getting last page with new articles
                    val getNewArticles = ArticlesApi.retrofitService.getNewArticles(
                        lastPage,
                        (newTotalArticles - totalArticles)
                    )
                    val result = getNewArticles.await()
                    saveArticlesInDatabase(result.response.articles)
                    lastNewArticle = result.response.articles.asArticlesEntity().last()
                }
            }.join()
        }
        return lastNewArticle
    }

    fun savePagesInfo(curentPage: Int, totalPages: Int, totalArticles: Int) {
        editor.apply {
            putInt(CURENT_PAGE, curentPage)
            putInt(TOTAL_PAGES, totalPages)
            putInt(TOTAL_ARTICLES, totalArticles)
        }.apply()
    }

    // BAD PRACTICE MUST BE REPLACED WITH DI
    companion object {
        @Volatile
        private var INSTANCE: ArticlesRepository? = null

        fun getInstance(app: Application, articlesDao: ArticlesDao): ArticlesRepository {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = ArticlesRepository(app, articlesDao)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}