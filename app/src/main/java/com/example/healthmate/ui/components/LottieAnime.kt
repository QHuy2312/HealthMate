package com.example.healthmate.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.healthmate.R
import com.example.healthmate.ui.theme.OceanBlue

/**
 * Reusable Lottie wrapper that loads from res/raw and loops forever.
 *
 * Usage:
 *   LottieRawAnimation(
 *       resId    = R.raw.streak_fire,
 *       size     = 140.dp,
 *       modifier = Modifier.align(Alignment.CenterHorizontally)
 *   )
 */
@Composable
fun LottieRawAnimation(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = OceanBlue,
                strokeWidth = 2.dp
            )
        }
    }
}


/**
 * Cute waving mascot — shown on the Login screen.
 * Asset: res/raw/lottie_anime.json
 */
@Composable
fun LoginMascotAnimation(modifier: Modifier = Modifier) {
    LottieRawAnimation(
        resId    = R.raw.lottie_anime,
        size     = 180.dp,
        modifier = modifier
    )
}

/**
 * Fire streak flame — shown on the Home screen.
 * Loads fire.json when the user has completed workouts today (active streak),
 * or fire_black.json when the streak is inactive (gray fire).
 */
@Composable
fun HomeStreakAnimation(hasWorkoutsToday: Boolean, modifier: Modifier = Modifier) {
    LottieRawAnimation(
        resId    = if (hasWorkoutsToday) R.raw.fire else R.raw.fire_black,
        size     = 140.dp,
        modifier = modifier
    )
}
