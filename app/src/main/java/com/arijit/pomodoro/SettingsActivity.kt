package com.arijit.pomodoro

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import android.content.res.Configuration
import androidx.activity.result.contract.ActivityResultContracts
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class SettingsActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var focusedTimeTxt: EditText
    private lateinit var focusedTimeSlider: Slider
    private lateinit var shortBreakTxt: EditText
    private lateinit var shortBreakSlider: Slider
    private lateinit var longBreakTxt: EditText
    private lateinit var longBreakSlider: Slider
    private lateinit var sessionsTxt: EditText
    private lateinit var sessionsSlider: Slider
    private lateinit var alarmTxt: EditText
    private lateinit var alarmSlider: Slider
    private lateinit var autoStartSessions: MaterialSwitch
    private lateinit var darkModeToggle: MaterialSwitch
    private lateinit var amoledToggle: MaterialSwitch
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var githubCard: androidx.cardview.widget.CardView
    private lateinit var supportCard: androidx.cardview.widget.CardView
    private lateinit var settingsTxt: TextView
    private lateinit var uiSettingsTxt: TextView
    private lateinit var aboutTheAppTxt: TextView
    private lateinit var runningTimerTxt: TextView
    private lateinit var madeWithLoveTxt: TextView
    private lateinit var uiSettingsComponents: LinearLayout
    private lateinit var timerSettingsComponents: LinearLayout
    private lateinit var ri: TextView
    private lateinit var a: TextView
    private lateinit var ft: TextView
    private lateinit var sb: TextView
    private lateinit var lb: TextView
    private lateinit var s: TextView
    private lateinit var ass: TextView
    private lateinit var dm: TextView
    private lateinit var am: TextView
    private lateinit var gr: TextView
    private lateinit var brownNoiseToggle: MaterialSwitch
    private lateinit var whiteNoiseToggle: MaterialSwitch
    private lateinit var rainfallToggle: MaterialSwitch
    private lateinit var lightJazzToggle: MaterialSwitch
    private lateinit var keepScreenAwakeToggle: MaterialSwitch
    private lateinit var hapticFeedbackToggle: MaterialSwitch
    private val CHANNEL_ID = "download_channel"
    private val NOTIFICATION_ID = 1
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    private var isUpdatingSlider = false

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Permissions granted, proceed with download
            handleMusicToggle()
        }
    }

    companion object {
        const val RESULT_TIMER_SETTINGS_CHANGED = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set light mode by default
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        initializeViews()
        loadSavedSettings()
        setupListeners()
        applyTheme()
        checkTimerState()
        createNotificationChannel()
        initializeMusicToggles()
        setupMusicToggleListeners()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Remove system theme handling, only use app preferences
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)
        
        if (amoledMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putBoolean("darkMode", false).apply()
        } else if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putBoolean("amoledMode", false).apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        applyTheme()
    }

    private fun checkTimerState() {
        val isTimerRunning = sharedPreferences.getBoolean("isTimerRunning", false)
        val isBreakActive = sharedPreferences.getBoolean("isBreakActive", false)

        if (isTimerRunning || isBreakActive) {
            timerSettingsComponents.visibility = View.GONE
            uiSettingsTxt.visibility = View.GONE
            uiSettingsComponents.visibility = View.GONE
            runningTimerTxt.visibility = View.VISIBLE
        } else {
            timerSettingsComponents.visibility = View.VISIBLE
            uiSettingsTxt.visibility = View.VISIBLE
            uiSettingsComponents.visibility = View.VISIBLE
            runningTimerTxt.visibility = View.GONE
        }
    }

    private fun initializeViews() {
        backBtn = findViewById(R.id.back_btn)
        focusedTimeTxt = findViewById(R.id.focused_time_txt)
        focusedTimeSlider = findViewById(R.id.slider_focused_time)
        shortBreakTxt = findViewById(R.id.short_break_txt)
        shortBreakSlider = findViewById(R.id.slider_short_break)
        longBreakTxt = findViewById(R.id.long_break_txt)
        longBreakSlider = findViewById(R.id.slider_long_break)
        sessionsTxt = findViewById(R.id.sessions_txt)
        sessionsSlider = findViewById(R.id.slider_sessions)
        autoStartSessions = findViewById(R.id.auto_start_toggle)
        darkModeToggle = findViewById(R.id.dark_mode_toggle)
        amoledToggle = findViewById(R.id.amoled_toggle)
        githubCard = findViewById(R.id.github_card)
        supportCard = findViewById(R.id.support_card)
        settingsTxt = findViewById(R.id.settings_txt)
        alarmTxt = findViewById(R.id.alarm_txt)
        alarmSlider = findViewById(R.id.slider_alarm)
        uiSettingsTxt = findViewById(R.id.ui_settings_txt)
        runningTimerTxt = findViewById(R.id.running_timer_txt)
        madeWithLoveTxt = findViewById(R.id.made_with_love_txt)
        aboutTheAppTxt = findViewById(R.id.about_the_app_txt)
        ft = findViewById(R.id.ft)
        sb = findViewById(R.id.sb)
        lb = findViewById(R.id.lb)
        s = findViewById(R.id.s)
        ass = findViewById(R.id.ass)
        dm = findViewById(R.id.dm)
        am = findViewById(R.id.am)
        gr = findViewById(R.id.gr)
        ri = findViewById(R.id.ri)
        a = findViewById(R.id.a)
        uiSettingsComponents = findViewById(R.id.ui_settings_components)
        timerSettingsComponents = findViewById(R.id.timer_settings_components)
        brownNoiseToggle = findViewById(R.id.brown_noise_toggle)
        whiteNoiseToggle = findViewById(R.id.white_noise_toggle)
        rainfallToggle = findViewById(R.id.rainfall_toggle)
        lightJazzToggle = findViewById(R.id.light_jazz_toggle)
        keepScreenAwakeToggle = findViewById(R.id.keep_screen_awake_toggle)
        hapticFeedbackToggle = findViewById(R.id.haptic_feedback_toggle)
    }

    private fun loadSavedSettings() {
        focusedTimeSlider.value = sharedPreferences.getInt("focusedTime", 25).toFloat()
        shortBreakSlider.value = sharedPreferences.getInt("shortBreak", 5).toFloat()
        longBreakSlider.value = sharedPreferences.getInt("longBreak", 10).toFloat()
        sessionsSlider.value = sharedPreferences.getInt("sessions", 4).toFloat()
        alarmSlider.value = sharedPreferences.getInt("alarmDuration", 3).toFloat()
        autoStartSessions.isChecked = sharedPreferences.getBoolean("autoStart", false)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        darkModeToggle.isChecked = darkMode
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)
        amoledToggle.isChecked = amoledMode
        val keepScreenAwake = sharedPreferences.getBoolean("keepScreenAwake", false)
        keepScreenAwakeToggle.isChecked = keepScreenAwake
        updateWakeLock(keepScreenAwake)
        hapticFeedbackToggle.isChecked = sharedPreferences.getBoolean("hapticFeedback", true)
        updateTexts()
    }

    private fun applyTheme() {
        val mainLayout = findViewById<android.widget.ScrollView>(R.id.main)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            mainLayout.setBackgroundColor(resources.getColor(android.R.color.black))
        } else if (darkMode) {
            mainLayout.setBackgroundColor(resources.getColor(R.color.dark_background))
        }
    }

    private fun setupListeners() {
        backBtn.setOnClickListener {
            saveSettings()
            if (sharedPreferences.getBoolean("wereTimerSettingsModified", false)) {
                setResult(RESULT_TIMER_SETTINGS_CHANGED)
            }
            finish()
            vibrate()
        }

        focusedTimeSlider.addOnChangeListener { _, value, _ ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                focusedTimeTxt.setText("${value.toInt()}")
                isUpdatingSlider = false
            }
            markTimerSettingsModified()
        }

        focusedTimeTxt.addTextChangedListener { text ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                val value = text.toString().toIntOrNull()
                if (value != null) {
                    val clampedValue = value.coerceIn(focusedTimeSlider.valueFrom.toInt(), focusedTimeSlider.valueTo.toInt())
                    focusedTimeSlider.value = clampedValue.toFloat()
                    if (value != clampedValue) {
                        focusedTimeTxt.setText(clampedValue.toString())
                        focusedTimeTxt.setSelection(focusedTimeTxt.text.length)
                    }
                    markTimerSettingsModified()
                }
                isUpdatingSlider = false
            }
        }

        shortBreakSlider.addOnChangeListener { _, value, _ ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                shortBreakTxt.setText("${value.toInt()}")
                isUpdatingSlider = false
            }
            markTimerSettingsModified()
        }

        shortBreakTxt.addTextChangedListener { text ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                val value = text.toString().toIntOrNull()
                if (value != null) {
                    val clampedValue = value.coerceIn(shortBreakSlider.valueFrom.toInt(), shortBreakSlider.valueTo.toInt())
                    shortBreakSlider.value = clampedValue.toFloat()
                    if (value != clampedValue) {
                        shortBreakTxt.setText(clampedValue.toString())
                        shortBreakTxt.setSelection(shortBreakTxt.text.length)
                    }
                    markTimerSettingsModified()
                }
                isUpdatingSlider = false
            }
        }

        longBreakSlider.addOnChangeListener { _, value, _ ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                longBreakTxt.setText("${value.toInt()}")
                isUpdatingSlider = false
            }
            markTimerSettingsModified()
        }

        longBreakTxt.addTextChangedListener { text ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                val value = text.toString().toIntOrNull()
                if (value != null) {
                    val clampedValue = value.coerceIn(longBreakSlider.valueFrom.toInt(), longBreakSlider.valueTo.toInt())
                    longBreakSlider.value = clampedValue.toFloat()
                    if (value != clampedValue) {
                        longBreakTxt.setText(clampedValue.toString())
                        longBreakTxt.setSelection(longBreakTxt.text.length)
                    }
                    markTimerSettingsModified()
                }
                isUpdatingSlider = false
            }
        }

        sessionsSlider.addOnChangeListener { _, value, _ ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                sessionsTxt.setText("${value.toInt()}")
                isUpdatingSlider = false
            }
            markTimerSettingsModified()
        }

        sessionsTxt.addTextChangedListener { text ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                val value = text.toString().toIntOrNull()
                if (value != null) {
                    val clampedValue = value.coerceIn(sessionsSlider.valueFrom.toInt(), sessionsSlider.valueTo.toInt())
                    sessionsSlider.value = clampedValue.toFloat()
                    if (value != clampedValue) {
                        sessionsTxt.setText(clampedValue.toString())
                        sessionsTxt.setSelection(sessionsTxt.text.length)
                    }
                    markTimerSettingsModified()
                }
                isUpdatingSlider = false
            }
        }

        alarmSlider.addOnChangeListener { _, value, _ ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                alarmTxt.setText("${value.toInt()}")
                isUpdatingSlider = false
            }
            markTimerSettingsModified()
        }

        alarmTxt.addTextChangedListener { text ->
            if (!isUpdatingSlider) {
                isUpdatingSlider = true
                val value = text.toString().toIntOrNull()
                if (value != null) {
                    val clampedValue = value.coerceIn(alarmSlider.valueFrom.toInt(), alarmSlider.valueTo.toInt())
                    alarmSlider.value = clampedValue.toFloat()
                    if (value != clampedValue) {
                        alarmTxt.setText(clampedValue.toString())
                        alarmTxt.setSelection(alarmTxt.text.length)
                    }
                    markTimerSettingsModified()
                }
                isUpdatingSlider = false
            }
        }

        autoStartSessions.setOnCheckedChangeListener { _, _ ->
            markTimerSettingsModified()
        }

        darkModeToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                amoledToggle.isChecked = false
                sharedPreferences.edit().apply {
                    putBoolean("darkMode", true)
                    putBoolean("amoledMode", false)
                    apply()
                }
            } else {
                sharedPreferences.edit().putBoolean("darkMode", false).apply()
            }
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            applyTheme()
        }

        amoledToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                darkModeToggle.isChecked = false
                sharedPreferences.edit().apply {
                    putBoolean("amoledMode", true)
                    putBoolean("darkMode", false)
                    apply()
                }
            } else {
                sharedPreferences.edit().putBoolean("amoledMode", false).apply()
            }
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            applyTheme()
        }

        githubCard.setOnClickListener {
            vibrate()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://github.com/Arijit-05/Minimal-Pomodoro")
            startActivity(intent)
        }

        supportCard.setOnClickListener {
            vibrate()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://ko-fi.com/arijitroy05/goal?g=0")
            startActivity(intent)
        }

        keepScreenAwakeToggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("keepScreenAwake", isChecked).apply()
            updateWakeLock(isChecked)
        }

        hapticFeedbackToggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("hapticFeedback", isChecked).apply()
        }
    }

    private fun markTimerSettingsModified() {
        sharedPreferences.edit().apply {
            putBoolean("wereTimerSettingsModified", true)
            putString("focusText", "Focus")  // Reset focus text when any setting is modified
            apply()
        }
    }

    private fun updateTexts() {
        focusedTimeTxt.setText("${focusedTimeSlider.value.toInt()}")
        shortBreakTxt.setText("${shortBreakSlider.value.toInt()}")
        longBreakTxt.setText("${longBreakSlider.value.toInt()}")
        val sessions = sessionsSlider.value.toInt()
        sessionsTxt.setText("$sessions")
        alarmTxt.setText("${alarmSlider.value.toInt()}")
    }

    private fun saveSettings() {
        sharedPreferences.edit().apply {
            putInt("focusedTime", focusedTimeSlider.value.toInt())
            putInt("shortBreak", shortBreakSlider.value.toInt())
            putInt("longBreak", longBreakSlider.value.toInt())
            putInt("sessions", sessionsSlider.value.toInt())
            putInt("alarmDuration", alarmSlider.value.toInt())
            putBoolean("autoStart", autoStartSessions.isChecked)
            putBoolean("hapticFeedback", hapticFeedbackToggle.isChecked)
            putString("focusText", "Focus")  // Reset focus text when settings are changed
            putBoolean("wereTimerSettingsModified", true)
            apply()
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        if (!hapticFeedbackToggle.isChecked) return // Don't vibrate if haptic feedback is disabled
        
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(50)
            }
        }
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
        } else {
            storagePermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for audio downloads"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDownloadNotification(musicName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading Audio")
            .setContentText("Downloading $musicName audio")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun dismissDownloadNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun getMusicFile(musicName: String): File {
        val musicDir = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "pomodoro_music")
        if (!musicDir.exists()) {
            musicDir.mkdirs()
        }
        return File(musicDir, "${musicName}.mp3")
    }

    private suspend fun downloadMusic(url: String, musicName: String) {
        withContext(Dispatchers.IO) {
            try {
                val musicFile = getMusicFile(musicName)
                if (!musicFile.exists()) {
                    showDownloadNotification(musicName)
                    val connection = URL(url).openConnection()
                    connection.connect()
                    val inputStream = connection.getInputStream()
                    val outputStream = FileOutputStream(musicFile)
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    inputStream.close()
                }
                dismissDownloadNotification()
            } catch (e: Exception) {
                e.printStackTrace()
                dismissDownloadNotification()
            }
        }
    }

    private fun handleMusicToggle() {
        val musicName = when {
            brownNoiseToggle.isChecked -> "brown_noise"
            whiteNoiseToggle.isChecked -> "white_noise"
            rainfallToggle.isChecked -> "rainfall"
            lightJazzToggle.isChecked -> "light_jazz"
            else -> null
        }

        if (musicName != null) {
            val url = when (musicName) {
                "brown_noise" -> "https://github.com/Arijit-05/fomodoro_assets/releases/download/brown-noise/brown_noise.mp3"
                "white_noise" -> "https://github.com/Arijit-05/fomodoro_assets/releases/download/white-noise/white_noise.mp3"
                "rainfall" -> "https://github.com/Arijit-05/fomodoro_assets/releases/download/rainfall/rainfall.mp3"
                "light_jazz" -> "https://github.com/Arijit-05/fomodoro_assets/releases/download/light-jazz/light_jazz.mp3"
                else -> null
            }

            if (url != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    downloadMusic(url, musicName)
                }
            }
        }
    }

    private fun initializeMusicToggles() {
        // Load saved toggle states
        val savedMusic = sharedPreferences.getString("selected_music", null)
        brownNoiseToggle.isChecked = savedMusic == "brown_noise"
        whiteNoiseToggle.isChecked = savedMusic == "white_noise"
        rainfallToggle.isChecked = savedMusic == "rainfall"
        lightJazzToggle.isChecked = savedMusic == "light_jazz"
    }

    private fun setupMusicToggleListeners() {
        val toggleListener = { toggle: MaterialSwitch, musicName: String ->
            if (toggle.isChecked) {
                // Uncheck other toggles
                brownNoiseToggle.isChecked = toggle == brownNoiseToggle
                whiteNoiseToggle.isChecked = toggle == whiteNoiseToggle
                rainfallToggle.isChecked = toggle == rainfallToggle
                lightJazzToggle.isChecked = toggle == lightJazzToggle

                // Save selection
                sharedPreferences.edit().putString("selected_music", musicName).apply()

                // Check permissions and download if needed
                if (checkStoragePermissions()) {
                    handleMusicToggle()
                } else {
                    requestStoragePermissions()
                }
            } else {
                sharedPreferences.edit().remove("selected_music").apply()
            }
        }

        brownNoiseToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) toggleListener(brownNoiseToggle, "brown_noise")
        }

        whiteNoiseToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) toggleListener(whiteNoiseToggle, "white_noise")
        }

        rainfallToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) toggleListener(rainfallToggle, "rainfall")
        }

        lightJazzToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) toggleListener(lightJazzToggle, "light_jazz")
        }
    }

    private fun updateWakeLock(enable: Boolean) {
        if (enable) {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                wakeLock = powerManager.newWakeLock(
                    android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    android.os.PowerManager.ON_AFTER_RELEASE,
                    "MinimalPomodoro::ScreenWakeLock"
                )
                wakeLock?.acquire(10*60*1000L /*10 minutes*/)
            }
        } else {
            wakeLock?.release()
            wakeLock = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        wakeLock = null
    }
}