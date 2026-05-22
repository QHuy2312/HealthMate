package com.example.healthmate.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.R
import com.example.healthmate.ai.Exercise
import com.example.healthmate.data.model.ExerciseItem
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark
import com.example.healthmate.ui.theme.MintGreenLight
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.OceanBlueLight

/* ── Screen ─────────────────────────────────────────────────────────── */
@Composable
fun WorkoutScreen(
    onStartWorkout: (name: String, duration: Int, calories: Int, exerciseId: String) -> Unit,
    completedIdsFlow: StateFlow<Set<String>> = MutableStateFlow(emptySet()),
    viewModel: WorkoutViewModel = viewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val completedIds by completedIdsFlow.collectAsStateWithLifecycle()
    val categories = listOf(
        R.string.workout_category_all,
        R.string.workout_category_beginner,
        R.string.workout_category_intermediate,
        R.string.workout_category_advanced
    )

    val plan by viewModel.plan.collectAsStateWithLifecycle()

    val filteredExercises = viewModel.getExercisesByDifficulty(selectedCategory)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        /* ── Header ────────────────────────────────────────────────── */
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.workout_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.workout_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ── Category filter chips ─────────────────────────────────── */
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = selectedCategory == index,
                    onClick = { viewModel.selectCategory(index) },
                    label = {
                        Text(
                            text = stringResource(categories[index]),
                            fontWeight = if (selectedCategory == index)
                                FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OceanBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ── Workout cards + AI results ────────────────────────────── */
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            /* ── AI Generate button ────────────────────────────────── */
            item {
                BubblyButton(
                    text = stringResource(R.string.workout_ga_button),
                    onClick = { viewModel.generatePlan() },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MintGreen,
                    shadowColor = MintGreenDark,
                    shadowHeight = 5.dp,
                    cornerRadius = 20.dp,
                    fontSize = 16.sp
                )
            }

            /* ── AI generated plan ─────────────────────────────────── */
            if (plan != null) {
                item {
                    AiPlanSection(
                        plan = plan!!,
                        completedIds = completedIds,
                        onStartWorkout = onStartWorkout
                    )
                }
            }

            /* ── Exercise cards from repository ────────────────────── */
            items(filteredExercises) { exercise ->
                ExerciseBubblyCard(
                    exercise = exercise,
                    isCompleted = exercise.id in completedIds,
                    onStart = { name, duration, calories ->
                        onStartWorkout(name, duration, calories, exercise.id)
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/* ── AI Plan Section ───────────────────────────────────────────────── */
@Composable
private fun AiPlanSection(
    plan: com.example.healthmate.ai.WorkoutPlan,
    completedIds: Set<String>,
    onStartWorkout: (String, Int, Int, String) -> Unit
) {
    BubblyCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = OceanBlue,
        shadowColor = OceanBlueDark,
        cornerRadius = 20.dp,
        shadowHeight = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_ga_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.workout_ga_total, plan.totalCalories),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.workout_ga_count, plan.exercises.size),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    /* ── Generated exercise cards ──────────────────────────────── */
    plan.exercises.forEachIndexed { index, exercise ->
        val exerciseId = "ga_${exercise.id}"
        AiExerciseCard(
            position = index + 1,
            exercise = exercise,
            isCompleted = exerciseId in completedIds,
            onStart = { name, duration, calories, id -> onStartWorkout(name, duration, calories, id) }
        )
        if (index < plan.exercises.lastIndex) {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/* ── Single AI-generated exercise card ─────────────────────────────── */
@Composable
private fun AiExerciseCard(
    position: Int,
    exercise: Exercise,
    isCompleted: Boolean,
    onStart: (String, Int, Int, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        BubblyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* Position number badge */
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(MintGreen, MintGreenLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$position",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                /* Exercise info */
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.workout_calories, exercise.calories),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                /* Start button — always enabled, even if completed */
                BubblyButton(
                    text = stringResource(R.string.workout_start),
                    onClick = { onStart(exercise.name, exercise.durationMin, exercise.calories, "ga_${exercise.id}") },
                    containerColor = OceanBlue,
                    shadowColor = OceanBlueDark,
                    shadowHeight = 4.dp,
                    cornerRadius = 14.dp,
                    fontSize = 13.sp
                )
            }
        }

        /* Green checkmark overlay when completed */
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.workout_completed),
                tint = MintGreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}

/* ── Exercise card from repository ──────────────────────────────────── */
@Composable
private fun ExerciseBubblyCard(
    exercise: ExerciseItem,
    isCompleted: Boolean,
    onStart: (String, Int, Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        BubblyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* Gradient icon box */
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(exercise.gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                /* Text info */
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(exercise.nameRes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = stringResource(R.string.workout_duration, exercise.durationMin),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.workout_calories, exercise.calories),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                /* Start button — always enabled, even if completed */
                val title = stringResource(exercise.nameRes)
                BubblyButton(
                    text = stringResource(R.string.workout_start),
                    onClick = { onStart(title, exercise.durationMin, exercise.calories) },
                    containerColor = OceanBlue,
                    shadowColor = OceanBlueDark,
                    shadowHeight = 4.dp,
                    cornerRadius = 14.dp,
                    fontSize = 13.sp
                )
            }
        }

        /* Green checkmark overlay when completed */
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.workout_completed),
                tint = MintGreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}
