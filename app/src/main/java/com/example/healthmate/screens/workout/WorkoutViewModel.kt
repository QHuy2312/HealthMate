package com.example.healthmate.screens.workout

import androidx.lifecycle.ViewModel
import com.example.healthmate.ai.WorkoutGeneticAlgorithm
import com.example.healthmate.ai.WorkoutPlan
import com.example.healthmate.data.ExerciseRepository
import com.example.healthmate.data.model.ExerciseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutViewModel : ViewModel() {

    private val targetCalories = 500

    private val _plan = MutableStateFlow<WorkoutPlan?>(null)
    val plan: StateFlow<WorkoutPlan?> = _plan.asStateFlow()

    fun generatePlan() {
        val ga = WorkoutGeneticAlgorithm(targetCalories)
        _plan.value = ga.run()
    }

    /* ── Exercise data from repository ──────────────────────────────── */

    val allExercises: List<ExerciseItem> = ExerciseRepository.getAllExercises()

    fun getExercisesByDifficulty(difficulty: Int): List<ExerciseItem> {
        return ExerciseRepository.getExercisesByDifficulty(difficulty)
    }

    /* ── Category filtering ───────────────────────────────────────── */

    private val _selectedCategory = MutableStateFlow(0)
    val selectedCategory: StateFlow<Int> = _selectedCategory.asStateFlow()

    fun selectCategory(index: Int) {
        _selectedCategory.value = index
    }

    fun clearAllData() {
        _plan.value = null
        _selectedCategory.value = 0
    }
}
