package com.example.healthmate.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.healthmate.R

/**
 * All navigation destinations in the app.
 *
 * [BottomNavItem] entries appear in the bottom bar;
 * standalone [Screen] entries do not.
 */
sealed class Screen(val route: String) {

    /* ── Standalone screens (no bottom bar) ───────────────────────── */
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Onboarding : Screen("onboarding")
    data object ActiveWorkout : Screen("active_workout/{exerciseName}/{durationMin}/{calories}/{exerciseId}")
    data object Admin : Screen("admin")

    /* ── Bottom-nav destinations ──────────────────────────────────── */
    data object Home : Screen("home"), BottomNavItem {
        override val icon: ImageVector = Icons.Default.Home
        override val labelRes: Int     = R.string.nav_home
    }

    data object Workout : Screen("workout"), BottomNavItem {
        override val icon: ImageVector = Icons.Default.DateRange
        override val labelRes: Int     = R.string.nav_workout
    }

    data object Profile : Screen("profile"), BottomNavItem {
        override val icon: ImageVector = Icons.Default.Person
        override val labelRes: Int     = R.string.nav_profile
    }
}

/**
 * Contract for items shown in [BottomNavBar].
 */
interface BottomNavItem {
    val icon: ImageVector
    @get:StringRes val labelRes: Int
}
