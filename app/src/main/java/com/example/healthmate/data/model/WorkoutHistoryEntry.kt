package com.example.healthmate.data.model

data class WorkoutHistoryEntry(
    val workoutName: String = "",
    val exerciseId: String = "",
    val calories: Int = 0,
    val durationMin: Int = 0,
    val timestamp: Long = 0L
)
