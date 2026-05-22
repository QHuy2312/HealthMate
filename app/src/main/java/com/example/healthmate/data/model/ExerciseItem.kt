package com.example.healthmate.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

data class ExerciseItem(
    val id: String,
    @StringRes val nameRes: Int,
    val name: String,
    val calories: Int,
    val durationMin: Int,
    val difficulty: Int, // 1=beginner, 2=intermediate, 3=advanced
    val gradient: List<Color>,
    val shadowColor: Color
)
