package com.arijit.pomodoro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arijit.pomodoro.fragments.TimerFragment
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import android.app.NotificationChannel
import android.app.NotificationManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var settings_btn: ImageView
    private lateinit var frame_layout: FrameLayout
    private lateinit var sessionsTxt: TextView

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == SettingsActivity.RESULT_TIMER_SETTINGS_CHANGED) {
            // Reset session count and reload timer only if timer settings were modified
            val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putInt("currentSession", 1)
                putBoolean("wereTimerSettingsModified", false)
                apply()
            }
            // Reload the timer fragment with new settings
            showTimerFragment()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createNotificationChannel()
        } else {
            // If permission is denied, show settings dialog
            AlertDialog.Builder(this)
                .setTitle("Notification Permission Required")
                .setMessage("This app needs notification permission to alert you when timers complete. Please enable it in Settings.")
                .setPositiveButton("Open Settings") { _, _ ->
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Not Now") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is granted, create notification channel
                    createNotificationChannel()
                }
                else -> {
                    // Request the permission directly
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android versions below 13, create notification channel directly
            createNotificationChannel()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "timer_notifications",
                "Timer Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for timer completion"
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setMainBackgroundForFragment(fragmentType: String) {
        val mainLayout = findViewById<android.widget.RelativeLayout>(R.id.main)
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            mainLayout.setBackgroundColor(resources.getColor(android.R.color.black))
        } else {
            when (fragmentType) {
                "timer" -> mainLayout.setBackgroundResource(if (darkMode) R.color.deep_red else R.color.light_red)
                "short_break" -> mainLayout.setBackgroundResource(if (darkMode) R.color.deep_green else R.color.light_green)
                "long_break" -> mainLayout.setBackgroundResource(if (darkMode) R.color.deep_blue else R.color.light_blue)
            }
        }
    }

    private fun showTimerFragment() {
        setMainBackgroundForFragment("timer")
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, com.arijit.pomodoro.fragments.TimerFragment())
            .commit()
    }

    private fun showShortBreakFragment() {
        setMainBackgroundForFragment("short_break")
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, com.arijit.pomodoro.fragments.ShortBreakFragment())
            .commit()
    }

    private fun showLongBreakFragment() {
        setMainBackgroundForFragment("long_break")
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, com.arijit.pomodoro.fragments.LongBreakFragment())
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check and request notification permission
        checkNotificationPermission()

        sessionsTxt = findViewById(R.id.sessions_txt)
        settings_btn = findViewById(R.id.settings_btn)
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            settings_btn.setImageResource(R.drawable.setting_dark)
            sessionsTxt.setTextColor(resources.getColor(R.color.white))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (darkMode) {
            settings_btn.setImageResource(R.drawable.setting_dark)
            sessionsTxt.setTextColor(resources.getColor(R.color.white))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            settings_btn.setImageResource(R.drawable.settings)
            sessionsTxt.setTextColor(resources.getColor(R.color.black))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        showTimerFragment()

        settings_btn.setOnClickListener {
            vibrate()
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Update theme when returning from settings
        val sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            settings_btn.setImageResource(R.drawable.setting_dark)
            sessionsTxt.setTextColor(resources.getColor(R.color.white))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (darkMode) {
            settings_btn.setImageResource(R.drawable.setting_dark)
            sessionsTxt.setTextColor(resources.getColor(R.color.white))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            settings_btn.setImageResource(R.drawable.settings)
            sessionsTxt.setTextColor(resources.getColor(R.color.black))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(50) // Vibrate for 50 milliseconds
            }
        }
    }
}