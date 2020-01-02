package com.aram.articles.service

import android.app.PendingIntent
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aram.articles.MainActivity
import com.aram.articles.R

class TerminateService : Service() {
    private val JOB_ID = 1
    val TAG = "LOG"
    private val CHANEL_ID = "chanelId2"
    private val notId = 2


    override fun onCreate() {
        super.onCreate()
        // creating ongoing notification for api level equal or higher than 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationIntent = Intent(application, MainActivity::class.java)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent = PendingIntent.getActivity(
                this,
                1,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this, CHANEL_ID)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getString(R.string.searching))
                .build()

            startForeground(notId, notification)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val jobService = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val component = ComponentName(application, BackgroundTask::class.java)
        val job = JobInfo.Builder(JOB_ID, component)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        jobService.schedule(job)
        return START_STICKY
    }

    // service method called by system when user has removed a task.
    // We can use this one if we want to check new articles only when app is closed
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "TermService: - onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }
}