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
import com.arijit.pomodoro.fragments.TimerFragment
import org.w3c.dom.Text

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
    private lateinit var autoStartSessions: MaterialSwitch
    private lateinit var darkModeToggle: MaterialSwitch
    private lateinit var amoledToggle: MaterialSwitch
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var githubCard: androidx.cardview.widget.CardView
    private lateinit var settingsTxt: TextView
    private lateinit var uiSettingsTxt: TextView
    private lateinit var aboutTheAppTxt: TextView
    private lateinit var runningTimerTxt: TextView
    private lateinit var madeWithLoveTxt: TextView
    private lateinit var ft: TextView
    private lateinit var sb: TextView
    private lateinit var lb: TextView
    private lateinit var s: TextView
    private lateinit var ass: TextView
    private lateinit var dm: TextView
    private lateinit var am: TextView
    private lateinit var gr: TextView
    private lateinit var ri: TextView
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
        settingsTxt = findViewById(R.id.settings_txt)
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
        uiSettingsComponents = findViewById(R.id.ui_settings_components)
        timerSettingsComponents = findViewById(R.id.timer_settings_components)
    }

    private fun loadSavedSettings() {
        focusedTimeSlider.value = sharedPreferences.getInt("focusedTime", 25).toFloat()
        shortBreakSlider.value = sharedPreferences.getInt("shortBreak", 5).toFloat()
        longBreakSlider.value = sharedPreferences.getInt("longBreak", 10).toFloat()
        sessionsSlider.value = sharedPreferences.getInt("sessions", 4).toFloat()
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
            applyDarkModeTextColors()
            applyDarkModeCardBackgrounds()
        } else if (darkMode) {
            mainLayout.setBackgroundColor(resources.getColor(R.color.dark_background))
            applyDarkModeTextColors()
            applyDarkModeCardBackgrounds()
        } else {
            mainLayout.setBackgroundColor(resources.getColor(R.color.offwhite))
            applyLightModeTextColors()
            applyLightModeCardBackgrounds()
        }
    }

    private fun applyDarkModeTextColors() {
        // Apply white color to all text views
        focusedTimeTxt.setTextColor(resources.getColor(R.color.white))
        shortBreakTxt.setTextColor(resources.getColor(R.color.white))
        longBreakTxt.setTextColor(resources.getColor(R.color.white))
        sessionsTxt.setTextColor(resources.getColor(R.color.white))
        settingsTxt.setTextColor(resources.getColor(R.color.white))
        uiSettingsTxt.setTextColor(resources.getColor(R.color.white))
        aboutTheAppTxt.setTextColor(resources.getColor(R.color.white))
        runningTimerTxt.setTextColor(resources.getColor(R.color.white))
        madeWithLoveTxt.setTextColor(resources.getColor(R.color.white))
        backBtn.setImageResource(R.drawable.back_white)
        ft.setTextColor(resources.getColor(R.color.white))
        sb.setTextColor(resources.getColor(R.color.white))
        lb.setTextColor(resources.getColor(R.color.white))
        s.setTextColor(resources.getColor(R.color.white))
        ass.setTextColor(resources.getColor(R.color.white))
        dm.setTextColor(resources.getColor(R.color.white))
        am.setTextColor(resources.getColor(R.color.white))
        gr.setTextColor(resources.getColor(R.color.white))
        ri.setTextColor(resources.getColor(R.color.white))
    }

    private fun applyLightModeTextColors() {
        // Apply default colors
        focusedTimeTxt.setTextColor(resources.getColor(R.color.black))
        shortBreakTxt.setTextColor(resources.getColor(R.color.black))
        longBreakTxt.setTextColor(resources.getColor(R.color.black))
        sessionsTxt.setTextColor(resources.getColor(R.color.black))
        settingsTxt.setTextColor(resources.getColor(R.color.black))
        uiSettingsTxt.setTextColor(resources.getColor(R.color.black))
        aboutTheAppTxt.setTextColor(resources.getColor(R.color.black))
        runningTimerTxt.setTextColor(resources.getColor(R.color.black))
        madeWithLoveTxt.setTextColor(resources.getColor(R.color.black))
        backBtn.setImageResource(R.drawable.back_black)
        ft.setTextColor(resources.getColor(R.color.black))
        sb.setTextColor(resources.getColor(R.color.black))
        lb.setTextColor(resources.getColor(R.color.black))
        s.setTextColor(resources.getColor(R.color.black))
        ass.setTextColor(resources.getColor(R.color.black))
        dm.setTextColor(resources.getColor(R.color.black))
        am.setTextColor(resources.getColor(R.color.black))
        gr.setTextColor(resources.getColor(R.color.black))
        ri.setTextColor(resources.getColor(R.color.black))
    }

    private fun applyDarkModeCardBackgrounds() {
        // Find all card backgrounds and set them to grey
        val cardBackgrounds = listOf(
            findViewById<LinearLayout>(R.id.focused_time_card_bg),
            findViewById<LinearLayout>(R.id.short_break_card_bg),
            findViewById<LinearLayout>(R.id.long_break_card_bg),
            findViewById<LinearLayout>(R.id.sessions_card_bg),
            findViewById<RelativeLayout>(R.id.auto_start_sessions_card_bg),
            findViewById<RelativeLayout>(R.id.dark_mode_card_bg),
            findViewById<RelativeLayout>(R.id.amoled_card_bg),
            findViewById<LinearLayout>(R.id.github_card_bg)
        )

        cardBackgrounds.forEach { card ->
            card.setBackgroundColor(resources.getColor(R.color.dark_card_background))
        }
    }

    private fun applyLightModeCardBackgrounds() {
        // Reset card backgrounds to default
        val cardBackgrounds = listOf(
            findViewById<LinearLayout>(R.id.focused_time_card_bg),
            findViewById<LinearLayout>(R.id.short_break_card_bg),
            findViewById<LinearLayout>(R.id.long_break_card_bg),
            findViewById<LinearLayout>(R.id.sessions_card_bg),
            findViewById<RelativeLayout>(R.id.auto_start_sessions_card_bg),
            findViewById<RelativeLayout>(R.id.dark_mode_card_bg),
            findViewById<RelativeLayout>(R.id.amoled_card_bg),
            findViewById<LinearLayout>(R.id.github_card_bg)
        )

        cardBackgrounds.forEach { card ->
            card.setBackgroundColor(resources.getColor(R.color.white))
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

        autoStartSessions.setOnCheckedChangeListener { _, _ ->
            markTimerSettingsModified()
        }

        darkModeToggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            applyTheme()
        }

        amoledToggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("amoledMode", isChecked).apply()
            applyTheme()
        }

        githubCard.setOnClickListener {
            vibrate()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://github.com/Arijit-05/Minimal-Pomodoro")
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