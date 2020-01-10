package com.aram.articles

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

class ArticlesApplication : Application() {

    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate() {
        super.onCreate()
        connectivityManager =
            getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chanel1 = NotificationChannel(
                "chanelId1",
                "NotificationsChanel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val chanel2 = NotificationChannel(
                "chanelId2",
                "ForegroundServiceChanel",
                NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(chanel1)
                createNotificationChannel(chanel2)
            }
        }
    }

}