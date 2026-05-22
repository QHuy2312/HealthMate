package com.example.healthmate.screens.activeworkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healthmate.R
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.OceanBlueLight

@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onBack: () -> Unit,
    onWorkoutComplete: (exerciseName: String, calories: Int) -> Unit
) {
    val timeLeft by viewModel.timeLeft.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        /* ── Top bar with back button ──────────────────────────────── */
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.active_workout_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = viewModel.getExerciseName(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        /* ── Lottie mascot placeholder ─────────────────────────────── */
        BubblyCard(
            modifier = Modifier.size(140.dp),
            cornerRadius = 28.dp,
            shadowHeight = 5.dp,
            surfaceColor = OceanBlue,
            shadowColor = OceanBlueDark
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Replace with LottieAnimation(composition, progress) once
                // a workout mascot .json is placed in app/src/main/assets/
                Text(text = "🏋️", fontSize = 64.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        /* ── Timer ring + countdown ────────────────────────────────── */
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = OceanBlue,
                trackColor = OceanBlueLight.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
            Text(
                text = timeLeft,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isRunning) stringResource(R.string.active_workout_exercising)
                   else stringResource(R.string.active_workout_resting),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        /* ── Control buttons ───────────────────────────────────────── */
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BubblyButton(
                text = if (isRunning) stringResource(R.string.active_workout_pause)
                       else stringResource(R.string.active_workout_resume),
                onClick = { viewModel.togglePause() },
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (isRunning) OceanBlue else MintGreen,
                shadowColor = if (isRunning) OceanBlueDark else MintGreenDark,
                cornerRadius = 20.dp,
                fontSize = 18.sp
            )
            BubblyButton(
                text = stringResource(R.string.active_workout_finish),
                onClick = {
                    viewModel.finishWorkout()
                    onWorkoutComplete(viewModel.getExerciseName(), viewModel.getCalories())
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = CoralAccent,
                shadowColor = CoralDark,
                cornerRadius = 20.dp,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
