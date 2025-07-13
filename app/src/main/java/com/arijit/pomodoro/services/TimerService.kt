package com.arijit.pomodoro.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.arijit.pomodoro.MainActivity
import com.arijit.pomodoro.R
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.arijit.pomodoro.utils.StatsManager

class TimerService : Service() {
    private lateinit var sharedPreferences: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var statsManager: StatsManager
    private var elapsedSeconds: Int = 0
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (sharedPreferences.getBoolean("timerRunning", false)) {
                val currentTime = sharedPreferences.getLong("timeLeftInMillis", 0)
                if (currentTime > 0) {
                    // Update time in SharedPreferences
                    sharedPreferences.edit().putLong("timeLeftInMillis", currentTime - 1000).apply()
                    elapsedSeconds = sharedPreferences.getInt("elapsedSeconds", 0) + 1
                    sharedPreferences.edit().putInt("elapsedSeconds", elapsedSeconds).apply()
                    // Update stats every minute in background
                    if (elapsedSeconds % 60 == 0) {
                        statsManager.updateStats(1)
                    }
                }
                // Only update notification if app is in background
                if (!sharedPreferences.getBoolean("isAppInForeground", true)) {
                    updateNotification()
                }
                handler.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_START_TIMER = "com.arijit.pomodoro.START_TIMER"
        private const val ACTION_STOP_TIMER = "com.arijit.pomodoro.STOP_TIMER"
        private const val ACTION_APP_OPENED = "com.arijit.pomodoro.APP_OPENED"
        private const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        private const val EXTRA_SESSION_INFO = "session_info"

        fun startTimer(context: Context, fragmentType: String, sessionInfo: String) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START_TIMER
                putExtra(EXTRA_FRAGMENT_TYPE, fragmentType)
                putExtra(EXTRA_SESSION_INFO, sessionInfo)
            }
            context.startForegroundService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP_TIMER
            }
            context.startService(intent)
        }

        fun appOpened(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_APP_OPENED
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        statsManager = StatsManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_START_TIMER -> {
                    val fragmentType = intent.getStringExtra(EXTRA_FRAGMENT_TYPE) ?: "focus"
                    val sessionInfo = intent.getStringExtra(EXTRA_SESSION_INFO) ?: ""
                    startService(fragmentType, sessionInfo)
                }
                ACTION_STOP_TIMER -> {
                    stopService()
                }
                ACTION_APP_OPENED -> {
                    // Mark that app is in foreground
                    sharedPreferences.edit().putBoolean("isAppInForeground", true).apply()
                    // Keep service running but update notification
                    val fragmentType = sharedPreferences.getString("currentFragment", "focus") ?: "focus"
                    val sessionInfo = sharedPreferences.getString("sessionInfo", "") ?: ""
                    updateNotification()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startService(fragmentType: String, sessionInfo: String) {
        // Save current state
        sharedPreferences.edit().apply {
            putString("currentFragment", fragmentType)
            putString("sessionInfo", sessionInfo)
            putBoolean("timerRunning", true)
            putBoolean("isAppInForeground", true)
            apply()
        }

        startForeground(NOTIFICATION_ID, createNotification(fragmentType, sessionInfo))
        handler.post(updateRunnable)
    }

    private fun stopService() {
        handler.removeCallbacks(updateRunnable)
        // On stop, add any remaining seconds as minutes if not already counted
        val leftoverSeconds = sharedPreferences.getInt("elapsedSeconds", 0) % 60
        if (leftoverSeconds > 0) {
            statsManager.updateStats(1)
        }
        sharedPreferences.edit().putInt("elapsedSeconds", 0).apply()
        sharedPreferences.edit().apply {
            putBoolean("timerRunning", false)
            putBoolean("isAppInForeground", false)
            apply()
        }
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing timer status"
                enableVibration(false)
                setSound(null, null)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(fragmentType: String, sessionInfo: String): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragmentType", fragmentType)
            putExtra("sessionInfo", sessionInfo)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeLeftInMillis = sharedPreferences.getLong("timeLeftInMillis", 0)
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val title = when (fragmentType) {
            "focus" -> "Focus Timer"
            "shortBreak" -> "Short Break"
            "longBreak" -> "Long Break"
            else -> "Timer"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(timeText)
            .setSmallIcon(R.drawable.brain)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val fragmentType = sharedPreferences.getString("currentFragment", "focus") ?: "focus"
        val sessionInfo = sharedPreferences.getString("sessionInfo", "") ?: ""
        notificationManager.notify(NOTIFICATION_ID, createNotification(fragmentType, sessionInfo))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
} 