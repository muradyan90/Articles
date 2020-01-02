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
                val getArticlesPage = ArticlesApi.retrofitService.getArticles(nextPage)
                try {
                    _status.value = ArticlesApiStatus.LOADINGMORE
                    val resultPage = getArticlesPage.await()
                    _status.value = ArticlesApiStatus.DONE
                    Log.d(TAG, "next pagi mejic curent - $curentPage | total - $totalPages")
                    savePagesInfo(
                        resultPage.response.currentPage,
                        resultPage.response.pages,
                        resultPage.response.total
                    )
                    saveArticlesInDatabase(resultPage.response.articles)
                } catch (e: Exception) {
                    _status.value = ArticlesApiStatus.ERROR
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
        Log.d(TAG, "search :  REPOSITORIUM   mtav search metod totalarts $totalArticles")
        if (totalArticles > -1 && checkNetworkStatus(app)) {

            Log.d(TAG, "search:   totalArticles > -1")
            coroutineScope.launch {
                // getting info about new articles from net
                val getArticlesPage = ArticlesApi.retrofitService.getNewArticles(1, 1)
                val resultPage = getArticlesPage.await()
                val newTotalArticles = resultPage.response.total
                val lastPage = resultPage.response.pages
                Log.d(TAG, "search:   coroutin lanched - $newTotalArticles")

                if (newTotalArticles > totalArticles && checkNetworkStatus(app)) {
                    Log.d(TAG, "search:   newTotalArticles >= totalArticles")
                    editor.putInt(TOTAL_ARTICLES, newTotalArticles)
                    // getting last page with new articles
                    val getNewArticles = ArticlesApi.retrofitService.getNewArticles(
                        lastPage,
                        (newTotalArticles - totalArticles)
                    )
                    Log.d(
                        TAG,
                        "pages info: lastpage - $lastPage | newTotalArticles - $newTotalArticles | totalArticles - $totalArticles  "
                    )

                    val result = getNewArticles.await()
                    Log.d(TAG, "search: response result vor pagna ekel ${result}")
                    saveArticlesInDatabase(result.response.articles)
                    lastNewArticle = result.response.articles.asArticlesEntity().last()

                }
            }.join()
        }
        return lastNewArticle
    }

    fun savePagesInfo(curentPage: Int, totalPages: Int, totalArticles: Int) {
        editor.putInt(CURENT_PAGE, curentPage).apply()
        editor.putInt(TOTAL_PAGES, totalPages).apply()
        editor.putInt(TOTAL_ARTICLES, totalArticles).apply()
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