package com.example.healthmate.data.model

data class DailyRecord(
    val date: String = "",
    val steps: Int = 0,
    val waterCups: Int = 0,
    val caloriesBurnedToday: Int = 0,
    val completedWorkoutsToday: List<String> = emptyList()
)
