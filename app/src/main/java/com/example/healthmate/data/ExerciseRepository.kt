package com.example.healthmate.data

import com.example.healthmate.R
import com.example.healthmate.data.model.ExerciseItem
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark
import com.example.healthmate.ui.theme.MintGreenLight
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.OceanBlueLight

object ExerciseRepository {

    private val allExercises = listOf(
        // ── Beginner (difficulty = 1) ──────────────────────────────────
        ExerciseItem(
            id = "yoga", nameRes = R.string.workout_yoga_title, name = "Yoga thư giãn 🧘",
            calories = 150, durationMin = 45, difficulty = 1,
            gradient = listOf(MintGreen, MintGreenLight), shadowColor = MintGreenDark
        ),
        ExerciseItem(
            id = "stretch", nameRes = R.string.workout_stretch_title, name = "Kéo giãn thả lỏng 🤸",
            calories = 80, durationMin = 15, difficulty = 1,
            gradient = listOf(OceanBlueLight, OceanBlue), shadowColor = OceanBlueDark
        ),
        ExerciseItem(
            id = "walking_meditation", nameRes = R.string.workout_walking_meditation, name = "Thiền đi bộ 🚶",
            calories = 100, durationMin = 20, difficulty = 1,
            gradient = listOf(MintGreenLight, MintGreen.copy(0.7f)), shadowColor = MintGreenDark
        ),
        ExerciseItem(
            id = "light_cycling", nameRes = R.string.workout_light_cycling, name = "Đạp xe nhẹ 🚴",
            calories = 120, durationMin = 25, difficulty = 1,
            gradient = listOf(OceanBlueLight.copy(0.8f), OceanBlue.copy(0.7f)), shadowColor = OceanBlueDark
        ),

        // ── Intermediate (difficulty = 2) ──────────────────────────────
        ExerciseItem(
            id = "cardio", nameRes = R.string.workout_cardio_title, name = "Cardio đốt cháy 🔥",
            calories = 280, durationMin = 30, difficulty = 2,
            gradient = listOf(CoralAccent, CoralAccent.copy(0.7f)), shadowColor = CoralDark
        ),
        ExerciseItem(
            id = "pilates", nameRes = R.string.workout_pilates_title, name = "Pilates dẻo dai 🌸",
            calories = 180, durationMin = 35, difficulty = 2,
            gradient = listOf(MintGreenLight, MintGreen), shadowColor = MintGreenDark
        ),
        ExerciseItem(
            id = "dance_fitness", nameRes = R.string.workout_dance_fitness, name = "Nhảy fitness 💃",
            calories = 220, durationMin = 30, difficulty = 2,
            gradient = listOf(CoralAccent.copy(0.8f), MintGreen), shadowColor = CoralDark
        ),
        ExerciseItem(
            id = "swimming", nameRes = R.string.workout_swimming, name = "Bơi lội 🏊",
            calories = 250, durationMin = 30, difficulty = 2,
            gradient = listOf(OceanBlue, OceanBlueLight), shadowColor = OceanBlueDark
        ),

        // ── Advanced (difficulty = 3) ──────────────────────────────────
        ExerciseItem(
            id = "hiit", nameRes = R.string.workout_hiit_title, name = "HIIT cực mạnh 💥",
            calories = 320, durationMin = 20, difficulty = 3,
            gradient = listOf(OceanBlue, OceanBlueLight), shadowColor = OceanBlueDark
        ),
        ExerciseItem(
            id = "strength", nameRes = R.string.workout_strength_title, name = "Tăng cơ bắp 💪",
            calories = 250, durationMin = 40, difficulty = 3,
            gradient = listOf(OceanBlue, MintGreen), shadowColor = OceanBlueDark
        ),
        ExerciseItem(
            id = "crossfit", nameRes = R.string.workout_crossfit, name = "CrossFit cực đỉnh 🔥",
            calories = 350, durationMin = 25, difficulty = 3,
            gradient = listOf(CoralAccent, OceanBlue), shadowColor = CoralDark
        ),
        ExerciseItem(
            id = "boxing", nameRes = R.string.workout_boxing, name = "Boxing tung đấm 🥊",
            calories = 300, durationMin = 20, difficulty = 3,
            gradient = listOf(CoralDark, CoralAccent), shadowColor = CoralDark
        )
    )

    fun getAllExercises(): List<ExerciseItem> = allExercises

    fun getExercisesByDifficulty(difficulty: Int): List<ExerciseItem> {
        return if (difficulty == 0) allExercises
        else allExercises.filter { it.difficulty == difficulty }
    }

    fun getExerciseById(id: String): ExerciseItem? {
        return allExercises.find { it.id == id }
    }
}
