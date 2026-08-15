package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.SentryApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SentryForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "sentry_focus_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DURATION_MINUTES = "EXTRA_DURATION_MINUTES"

        fun startService(context: Context, durationMinutes: Int = 25) {
            val intent = Intent(context, SentryForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SentryForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                startForeground(NOTIFICATION_ID, buildNotification("Focus Session Active", "$durationMinutes minutes remaining"))
                startTimer(durationMinutes)
            }
            ACTION_STOP -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationMinutes: Int) {
        timerJob?.cancel()
        var remainingSeconds = durationMinutes * 60
        SentryApp.setStudyModeActive(true)
        SentryApp.setFocusRemainingSeconds(remainingSeconds)

        timerJob = serviceScope.launch {
            while (isActive && remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                SentryApp.setFocusRemainingSeconds(remainingSeconds)

                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                val timeStr = String.format("%02d:%02d remaining", mins, secs)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, buildNotification("Focus Session Active", timeStr))
            }

            // Session completed
            SentryApp.setStudyModeActive(false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        SentryApp.setStudyModeActive(false)
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Social Sentry Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live timer status for active focus blocks"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
