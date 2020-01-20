package com.aram.articles.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aram.articles.MainActivity
import com.aram.articles.R
import com.aram.articles.database.ArticleDatabase
import com.aram.articles.database.ArticleEntity
import com.aram.articles.database.ArticlesDao
import com.aram.articles.repository.ArticlesRepository
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class BackgroundTask : JobService(), KoinComponent{

    private lateinit var allArticlesDao: ArticlesDao
    private lateinit var notificationManager: NotificationManager
    private lateinit var handler: Handler
    private val delay = 4000L
    private val notId = 1
    private val CURENT_PAGE = "curent_page"
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val CHANEL_ID = "ChanelId"

    // INJECTED BY KOIN
    private val repository: ArticlesRepository by inject()

    // for debug
    val TAG = "LOG"
    var i = 0

    private var serviceJob = Job()
    private val coroutineScope = CoroutineScope(serviceJob + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        init()
        // keeping CPU active bad performance for device battery but anyway for The Guardian Articles demo app it isn`t so critical
        val pm =
            getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Service:Wakelock")
        wl.acquire()
        Log.d(TAG, "backTask: - onCreateService")
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "BackTask: - onStartJob")
        notificationManager.notify(notId, createNotification())
        getArticles()
        return true
    }

    private fun getArticles() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                searchNewArticles()
                Log.d(TAG, "Handler: - ${++i}")
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    private fun searchNewArticles() {
        coroutineScope.launch {
           // val lastNewArticle = repository.searchNewArticles()
            val lastNewArticle = repository.searchNewArticlesRx()
            if (lastNewArticle != null) {
                Log.d(TAG,"article NOTI HAMAR $lastNewArticle")
                notificationManager.notify(
                    Math.random().toInt() * 10,

                    createNotification(lastNewArticle)
                )
            }
        }
    }

    private fun createNotification(newArticle: ArticleEntity? = null): Notification {
        val notificationIntent = Intent(application, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            notId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(this, CHANEL_ID)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
        return when (newArticle) {
            null -> {
                builder
                    .setContentTitle(resources.getString(R.string.searching))
                    .build()
            }
            else -> {
                builder
                    .setContentTitle(resources.getString(R.string.new_articles_found))
                    .setContentText(newArticle.webTitle)
                    .build()
            }
        }
    }

    private fun init() {
        allArticlesDao = ArticleDatabase.getInstance(this).articlesDao
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sharedPref = getSharedPreferences(CURENT_PAGE, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        handler = Handler()
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        notificationManager.cancelAll()
        Log.d(TAG, "BackTask: - onStopJob")
        serviceJob.cancel()
        repository.compositeDisposableBackgroundTask.clear()
        return true
    }
}