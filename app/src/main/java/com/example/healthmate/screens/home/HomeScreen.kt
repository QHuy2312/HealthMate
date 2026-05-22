package com.example.healthmate.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.R
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.components.HomeStreakAnimation
import com.example.healthmate.ui.theme.AmberAccent
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenLight
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.OceanBlueLight

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onQuickWorkout: (Int) -> Unit = {}
) {
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val advice by viewModel.advice.collectAsStateWithLifecycle()
    val waterCups by viewModel.waterCups.collectAsStateWithLifecycle()
    val heartRate by viewModel.heartRate.collectAsStateWithLifecycle()
    val caloriesBurned by viewModel.caloriesBurned.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val fitnessLevel by viewModel.fitnessLevel.collectAsStateWithLifecycle()
    val completedToday by viewModel.completedWorkoutsToday.collectAsStateWithLifecycle()

    /* ── Permission launcher ──────────────────────────────────────── */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result ignored — sensor returns 0 until granted */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        viewModel.loadDailyData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        /* ── Header ────────────────────────────────────────────────── */
        Text(
            text = stringResource(R.string.home_greeting, userName.ifBlank { "bạn" }),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        /* ── Lottie streak animation ──────────────────────────────── */
        HomeStreakAnimation(
            hasWorkoutsToday = completedToday.isNotEmpty(),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        /* ── Streak banner ─────────────────────────────────────────── */
        BubblyCard(
            modifier = Modifier.fillMaxWidth(),
            surfaceColor = OceanBlue,
            shadowColor = OceanBlueDark,
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.home_streak, streak),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (completedToday.isNotEmpty()) Color(0xFFFF6D00) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ── Summary section title ─────────────────────────────────── */
        Text(
            text = stringResource(R.string.home_today_summary),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        /* ── Stat cards grid ───────────────────────────────────────── */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBubblyCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Star,
                label = stringResource(R.string.home_calories),
                value = "%,d".format(caloriesBurned),
                gradientColors = listOf(CoralAccent, AmberAccent)
            )
            StatBubblyCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Place,
                label = stringResource(R.string.home_steps),
                value = "%,d".format(steps),
                gradientColors = listOf(OceanBlue, OceanBlueLight)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBubblyCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                label = stringResource(R.string.home_heart_rate),
                value = "$heartRate bpm",
                gradientColors = listOf(CoralAccent, CoralAccent.copy(alpha = 0.7f))
            )
            WaterBubblyCard(
                modifier = Modifier.weight(1f),
                cups = waterCups,
                onAdd = { viewModel.addWaterCup() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ── AI Health advice (ML-powered) ─────────────────────────── */
        val adviceCardColor = when (fitnessLevel) {
            0 -> AmberAccent
            1 -> MintGreen
            2 -> OceanBlue
            else -> MintGreen
        }
        val adviceShadowColor = when (fitnessLevel) {
            0 -> AmberAccent.copy(alpha = 0.7f)
            1 -> Color(0xFF005A36)
            2 -> OceanBlueDark
            else -> Color(0xFF005A36)
        }
        BubblyCard(
            modifier = Modifier.fillMaxWidth(),
            surfaceColor = adviceCardColor,
            shadowColor = adviceShadowColor,
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_ai_advice_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = advice.status,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = advice.message,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Quick-workout chips ───────────────────────────────────── */
        Text(
            text = stringResource(R.string.home_quick_workout),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { QuickWorkoutChip(stringResource(R.string.home_category_yoga), OceanBlue) { onQuickWorkout(1) } }
            item { QuickWorkoutChip(stringResource(R.string.home_category_cardio), CoralAccent) { onQuickWorkout(2) } }
            item { QuickWorkoutChip(stringResource(R.string.home_category_strength), MintGreen) { onQuickWorkout(3) } }
            item { QuickWorkoutChip(stringResource(R.string.home_category_meditation), AmberAccent) { onQuickWorkout(1) } }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────── */

@Composable
private fun StatBubblyCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    gradientColors: List<Color>
) {
    BubblyCard(
        modifier = modifier.height(116.dp),
        cornerRadius = 20.dp,
        shadowHeight = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WaterBubblyCard(
    modifier: Modifier = Modifier,
    cups: Int,
    onAdd: () -> Unit
) {
    BubblyCard(
        modifier = modifier.height(116.dp),
        cornerRadius = 20.dp,
        shadowHeight = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            /* Left: icon + value + label */
            Column {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MintGreen, MintGreenLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$cups/8",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.home_water),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /* Right: '+' button */
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MintGreen)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_water_add),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickWorkoutChip(label: String, color: Color, onClick: () -> Unit) {
    BubblyCard(
        modifier = Modifier.clickable { onClick() },
        surfaceColor = color.copy(alpha = 0.12f),
        shadowColor = color.copy(alpha = 0.06f),
        cornerRadius = 16.dp,
        shadowHeight = 3.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
