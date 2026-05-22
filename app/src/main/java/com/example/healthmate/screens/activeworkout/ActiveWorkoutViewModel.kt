package com.example.healthmate.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActiveWorkoutViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val exerciseName: String =
        savedStateHandle.get<String>("exerciseName") ?: "Bài tập"
    private val durationMin: Int =
        savedStateHandle.get<String>("durationMin")?.toIntOrNull() ?: 10
    private val calories: Int =
        savedStateHandle.get<String>("calories")?.toIntOrNull() ?: (durationMin * 8)

    private val totalSeconds = durationMin * 60

    private val _timeLeft = MutableStateFlow(formatTime(totalSeconds))
    val timeLeft: StateFlow<String> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private var remainingSeconds = totalSeconds
    private var countdownJob: Job? = null

    fun getExerciseName(): String = exerciseName

    fun getCalories(): Int = calories

    fun getDurationMin(): Int = durationMin

    fun startWorkout() {
        _isRunning.value = true
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive && remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds--
                _timeLeft.value = formatTime(remainingSeconds)
                _progress.value = remainingSeconds.toFloat() / totalSeconds
            }
            if (remainingSeconds <= 0) {
                _isRunning.value = false
            }
        }
    }

    fun togglePause() {
        if (_isRunning.value) {
            countdownJob?.cancel()
            _isRunning.value = false
        } else {
            startWorkout()
        }
    }

    fun finishWorkout() {
        countdownJob?.cancel()
        _isRunning.value = false
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }
}
