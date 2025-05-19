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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import android.content.res.Configuration
import androidx.activity.result.contract.ActivityResultContracts

class SettingsActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var focusedTimeTxt: TextView
    private lateinit var focusedTimeSlider: Slider
    private lateinit var shortBreakTxt: TextView
    private lateinit var shortBreakSlider: Slider
    private lateinit var longBreakTxt: TextView
    private lateinit var longBreakSlider: Slider
    private lateinit var sessionsTxt: TextView
    private lateinit var sessionsSlider: Slider
    private lateinit var alarmTxt: TextView
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

    companion object {
        const val RESULT_TIMER_SETTINGS_CHANGED = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (amoledToggle.isChecked) {
            val isNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            darkModeToggle.isChecked = isNightMode
            sharedPreferences.edit().putBoolean("darkMode", isNightMode).apply()
            AppCompatDelegate.setDefaultNightMode(if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            applyTheme()
        }
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
        uiSettingsComponents = findViewById(R.id.ui_settings_components)
        timerSettingsComponents = findViewById(R.id.timer_settings_components)
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
        } else {
            mainLayout.setBackgroundColor(resources.getColor(R.color.offwhite))
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
            focusedTimeTxt.text = "${value.toInt()} mins"
            markTimerSettingsModified()
        }

        shortBreakSlider.addOnChangeListener { _, value, _ ->
            shortBreakTxt.text = "${value.toInt()} mins"
            markTimerSettingsModified()
        }

        longBreakSlider.addOnChangeListener { _, value, _ ->
            longBreakTxt.text = "${value.toInt()} mins"
            markTimerSettingsModified()
        }

        sessionsSlider.addOnChangeListener { _, value, _ ->
            val sessions = value.toInt()
            sessionsTxt.text = "$sessions sessions"
            markTimerSettingsModified()
        }

        alarmSlider.addOnChangeListener { _, value, _ ->
            alarmTxt.text = "${value.toInt()} seconds"
            markTimerSettingsModified()
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
    }

    private fun markTimerSettingsModified() {
        sharedPreferences.edit().putBoolean("wereTimerSettingsModified", true).apply()
    }

    private fun updateTexts() {
        focusedTimeTxt.text = "${focusedTimeSlider.value.toInt()} mins"
        shortBreakTxt.text = "${shortBreakSlider.value.toInt()} mins"
        longBreakTxt.text = "${longBreakSlider.value.toInt()} mins"
        val sessions = sessionsSlider.value.toInt()
        sessionsTxt.text = "$sessions sessions"
    }

    private fun saveSettings() {
        sharedPreferences.edit().apply {
            putInt("focusedTime", focusedTimeSlider.value.toInt())
            putInt("shortBreak", shortBreakSlider.value.toInt())
            putInt("longBreak", longBreakSlider.value.toInt())
            putInt("sessions", sessionsSlider.value.toInt())
            putInt("alarmDuration", alarmSlider.value.toInt())
            putBoolean("autoStart", autoStartSessions.isChecked)
            apply()
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
                vibrator.vibrate(50)
            }
        }
    }
}