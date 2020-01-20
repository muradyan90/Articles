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
import com.aram.articles.network.ArticlesApiService
import com.aram.articles.network.ResponsePage
import com.aram.articles.network.asArticlesEntity
import com.aram.articles.viewmodels.ArticlesApiStatus
import com.aram.articles.viewmodels.checkNetworkStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ArticlesRepository(
    private val app: Application,
    private val articlesDao: ArticlesDao
) : KoinComponent {
    private val TAG = "LOG"
    private var repoJob = Job()
    private val coroutineScope = CoroutineScope(repoJob + Dispatchers.IO)
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

    private var articlesFromNet = mutableListOf<ArticleEntity>()
    private var articlesFromNetLiveData: MutableLiveData<List<ArticleEntity>> = MutableLiveData()

    private var queryParams = mutableMapOf<String, String>()
    private val pageSize = 10

    init {
        queryParams["q"] = "sport articles"
        queryParams["show-fields"] = "thumbnail,category"
        queryParams["api-key"] = "1841ef16-e404-452b-bde8-0ff6d3a3e332"
        queryParams["pageSize"] = pageSize.toString()
    }

    // INJECTED BY KOIN
    private val apiService: ArticlesApiService by inject()

    // RxJava CompositeDisposable for cancellation
    val compositeDisposable = CompositeDisposable()
    val compositeDisposableBackgroundTask = CompositeDisposable()

    fun getFirstPageRx(): MutableLiveData<List<ArticleEntity>> {
        Log.d(TAG, "FIRST PAGE")
        if (checkNetworkStatus(app)) {
             try {
            _status.value = ArticlesApiStatus.LOADING

            compositeDisposable.add(

                apiService.getArticles(queryParams)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { responsePage: ResponsePage ->
                            responsePage.apply {

                                savePagesInfo(response.pages, response.pages, response.total)
                                queryParams["page"] = response.pages.toString()

                                apiService.getArticles(queryParams)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                        { responsePage: ResponsePage ->

                                            responsePage.apply {

                                                saveArticlesInDatabase(response.articles)
                                                articlesFromNet.addAll(response.articles.asArticlesEntity())
                                                articlesFromNetLiveData.postValue(articlesFromNet)

                                                _status.value = ArticlesApiStatus.DONE
                                            }
                                        },
                                        { error: Throwable ->
                                            _status.value = ArticlesApiStatus.ERROR
                                        })
                            }
                        },
                        { error: Throwable ->
                            _status.value = ArticlesApiStatus.ERROR
                        })
            )

                } catch (e: Exception) {
                    _status.value = ArticlesApiStatus.ERROR
                }
        }
        return articlesFromNetLiveData
    }

    fun getNextPageRx() {
        val curentPage = sharedPref.getInt(CURENT_PAGE, 0)

        val nextPage = curentPage - 1
        if (nextPage >= 1) {

            queryParams["page"] = nextPage.toString()
            // ArticlesApi.retrofitService.getArticles(queryParams).let {

            if (curentPage == 0) {
                _status.value = ArticlesApiStatus.LOADING
            } else {
                _status.value = ArticlesApiStatus.LOADINGMORE
            }

            compositeDisposable.add(
                apiService.getArticles(queryParams)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { responsePage: ResponsePage ->

                            responsePage.apply {

                                savePagesInfo(
                                    response.currentPage,
                                    response.pages,
                                    response.total
                                )
                                saveArticlesInDatabase(response.articles)

                                articlesFromNet.addAll(response.articles.asArticlesEntity())
                                articlesFromNetLiveData.postValue(articlesFromNet)
                                Log.d(TAG, "${response.articles}")
                                _status.value = ArticlesApiStatus.DONE
                            }
                        },
                        { error: Throwable ->
                            _status.value = ArticlesApiStatus.ERROR
                        })
            )
        }
    }

    fun saveArticlesInDatabase(articlesFromNet: List<Article>) {
        coroutineScope.launch {
            articlesFromNet.asArticlesEntity().forEach { articlesDao.insertArticle(it) }
        }
    }

    fun saveLikedState(article: ArticleEntity) {
        coroutineScope.launch {
            val oldArticle = articlesDao.getArticle(article.id)
            val newArticle = oldArticle.copy(isLiked = !oldArticle.isLiked)
            articlesDao.updateArticle(newArticle)
        }

    }

    fun deleteArticle(article: ArticleEntity) {
        coroutineScope.launch {
            val oldArticle = articlesDao.getArticle(article.id)
            val newArticle = oldArticle.copy(isDeleted = true)
            articlesDao.updateArticle(newArticle)
        }
    }

    suspend fun searchNewArticlesRx(): ArticleEntity? {
        var lastNewArticle: ArticleEntity? = null
        val totalArticles = sharedPref.getInt(TOTAL_ARTICLES, -1)
        if (totalArticles > -1 && checkNetworkStatus(app)) {
            coroutineScope.launch {
                try {

                    compositeDisposableBackgroundTask.add(

                        // TOdo 1 CALL
                        apiService.getArticles(queryParams)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe { responsePage: ResponsePage ->
                                responsePage.apply {
                                    editor.putInt(TOTAL_ARTICLES, response.total).apply()

                                    if (totalArticles > response.total && checkNetworkStatus(app)) {
                                        // TOdo 2 CALL
                                        apiService.getArticles(queryParams)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe { responsePage: ResponsePage ->

                                                responsePage.apply {

                                                    saveArticlesInDatabase(response.articles)
                                                    lastNewArticle =
                                                        response.articles.asArticlesEntity().first()

                                                    Log.d(TAG, " GTAC ARTICLE@ $lastNewArticle")
                                                    articlesFromNet.addAll(response.articles.asArticlesEntity())
                                                    articlesFromNetLiveData.postValue(
                                                        articlesFromNet
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                    )

                } catch (e: Exception) {
                }
            }.join()
        }
        Log.d(TAG, "REPOZITORIAI SEARCH METOD $lastNewArticle")
        return lastNewArticle
    }

    fun savePagesInfo(curentPage: Int, totalPages: Int, totalArticles: Int) {
        editor.apply {
            putInt(CURENT_PAGE, curentPage)
            putInt(TOTAL_PAGES, totalPages)
            putInt(TOTAL_ARTICLES, totalArticles)
        }.apply()
    }

}