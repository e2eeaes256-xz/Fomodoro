package com.arijit.pomodoro.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import com.arijit.pomodoro.R
import android.widget.RelativeLayout

class TimerFragment : Fragment() {
    private lateinit var minTxt: TextView
    private lateinit var secTxt: TextView
    private lateinit var playBtn: CardView
    private lateinit var pauseBtn: CardView
    private lateinit var resetBtn: ImageView
    private lateinit var skipBtn: ImageView
    private lateinit var brain: ImageView
    private lateinit var focusCardBg: LinearLayout
    private lateinit var focusTxt: TextView
    private lateinit var sessionsTxt: TextView
    
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var timerRunning = false
    private var currentSession = 1
    private var totalSessions = 4
    private var autoStart = false
    private var isFromShortBreak = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadSettings()
    }
    
    private fun loadSettings() {
        val sharedPreferences = requireContext().getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        timeLeftInMillis = sharedPreferences.getInt("focusedTime", 25) * 60 * 1000L
        totalSessions = sharedPreferences.getInt("sessions", 4)
        autoStart = sharedPreferences.getBoolean("autoStart", false)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)
        minTxt = view.findViewById(R.id.min_txt)
        secTxt = view.findViewById(R.id.sec_txt)
        playBtn = view.findViewById(R.id.play_btn)
        pauseBtn = view.findViewById(R.id.pause_btn)
        resetBtn = view.findViewById(R.id.reset_btn)
        skipBtn = view.findViewById(R.id.skip_btn)
        focusCardBg = view.findViewById(R.id.focus_card_bg)
        focusTxt = view.findViewById(R.id.focus_txt)
        brain = view.findViewById(R.id.brain)
        sessionsTxt = requireActivity().findViewById(R.id.sessions_txt)

        val parentLayout = requireActivity().findViewById<android.widget.RelativeLayout>(R.id.main)
        val sharedPreferences = requireContext().getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            minTxt.setTextColor(resources.getColor(R.color.light_red))
            secTxt.setTextColor(resources.getColor(R.color.light_red))
            focusTxt.setTextColor(resources.getColor(R.color.deep_red))
            focusCardBg.setBackgroundColor(resources.getColor(R.color.medium_red))
            skipBtn.setImageResource(R.drawable.skip_red_dark)
            resetBtn.setImageResource(R.drawable.reset_red_dark)
            brain.setImageResource(R.drawable.brain_dark_red)
        } else if (darkMode) {
            minTxt.setTextColor(resources.getColor(R.color.light_red))
            secTxt.setTextColor(resources.getColor(R.color.light_red))
            focusTxt.setTextColor(resources.getColor(R.color.deep_red))
            focusCardBg.setBackgroundColor(resources.getColor(R.color.medium_red))
            skipBtn.setImageResource(R.drawable.skip_red_dark)
            resetBtn.setImageResource(R.drawable.reset_red_dark)
            brain.setImageResource(R.drawable.brain_dark_red)
        } else {
            minTxt.setTextColor(resources.getColor(R.color.deep_red))
            secTxt.setTextColor(resources.getColor(R.color.deep_red))
            focusTxt.setTextColor(resources.getColor(R.color.light_red))
            focusCardBg.setBackgroundColor(resources.getColor(R.color.deep_red))
            skipBtn.setImageResource(R.drawable.skip_red)
            resetBtn.setImageResource(R.drawable.reset_red)
            brain.setImageResource(R.drawable.brain)
        }

        updateCountdownText()
        updateSessionsText()
        
        playBtn.setOnClickListener {
            vibrate()
            startTimer()
        }
        
        pauseBtn.setOnClickListener {
            vibrate()
            pauseTimer()
        }
        
        resetBtn.setOnClickListener {
            vibrate()
            resetTimer()
        }
        
        skipBtn.setOnClickListener {
            vibrate()
            loadShortBreakFragment()
        }

        // Auto-start timer only if coming from ShortBreakFragment and auto-start is enabled
        if (autoStart && isFromShortBreak) {
            startTimer()
        }

        return view
    }
    
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                timerRunning = false
                updateTimerState(false)
                if (currentSession < totalSessions) {
                    loadShortBreakFragment()
                } else {
                    Toast.makeText(requireContext(), "All sessions completed", Toast.LENGTH_SHORT).show()
                    loadLongBreakFragment()
                }
            }
        }.start()
        
        timerRunning = true
        updateTimerState(true)
        playBtn.visibility = View.GONE
        pauseBtn.visibility = View.VISIBLE
        resetBtn.visibility = View.VISIBLE
        skipBtn.visibility = View.VISIBLE
    }
    
    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        updateTimerState(false)
        playBtn.visibility = View.VISIBLE
        pauseBtn.visibility = View.GONE
    }
    
    private fun resetTimer() {
        countDownTimer?.cancel()
        loadSettings()
        timerRunning = false
        updateTimerState(false)
        updateCountdownText()
        
        playBtn.visibility = View.VISIBLE
        pauseBtn.visibility = View.GONE
        resetBtn.visibility = View.GONE
        skipBtn.visibility = View.GONE
    }
    
    private fun updateCountdownText() {
        val minutes = (timeLeftInMillis / 1000).toInt() / 60
        val seconds = (timeLeftInMillis / 1000).toInt() % 60
        
        minTxt.text = String.format("%02d", minutes)
        secTxt.text = String.format("%02d", seconds)
    }

    private fun updateSessionsText() {
        sessionsTxt.text = "$currentSession/$totalSessions"
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(50)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun loadShortBreakFragment() {
        val parentLayout = requireActivity().findViewById<RelativeLayout>(R.id.main)
        val sharedPreferences = requireContext().getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            parentLayout.setBackgroundColor(resources.getColor(android.R.color.black))
        } else if (darkMode) {
            parentLayout.setBackgroundResource(R.color.deep_green)
        } else {
            parentLayout.setBackgroundResource(R.color.light_green)
        }

        val fragment = ShortBreakFragment()
        fragment.setSessionInfo(currentSession, totalSessions, autoStart, true)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun loadLongBreakFragment() {
        val parentLayout = requireActivity().findViewById<RelativeLayout>(R.id.main)
        val sharedPreferences = requireContext().getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
        val amoledMode = sharedPreferences.getBoolean("amoledMode", false)

        if (amoledMode) {
            parentLayout.setBackgroundColor(resources.getColor(android.R.color.black))
        } else if (darkMode) {
            parentLayout.setBackgroundResource(R.color.deep_blue)
        } else {
            parentLayout.setBackgroundResource(R.color.light_blue)
        }

        val fragment = LongBreakFragment()
        fragment.setSessionInfo(1, totalSessions) // Reset to first session
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun updateTimerState(isRunning: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("PomodoroSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isTimerRunning", isRunning)
            putBoolean("isBreakActive", false)
            apply()
        }
    }

    companion object {
        fun newInstance(currentSession: Int, totalSessions: Int, autoStart: Boolean, isFromShortBreak: Boolean = false): TimerFragment {
            return TimerFragment().apply {
                this.currentSession = currentSession
                this.totalSessions = totalSessions
                this.autoStart = autoStart
                this.isFromShortBreak = isFromShortBreak
            }
        }
    }
}