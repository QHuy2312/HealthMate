package com.example.healthmate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.healthmate.ui.theme.CardShadow
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.TextSecondary
import com.example.healthmate.ui.theme.White

/** Routes that display the bottom bar. */
private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Workout.route,
    Screen.Profile.route
)

/**
 * Returns `true` when the current destination should show the bottom bar.
 */
fun shouldShowBottomBar(currentRoute: String?): Boolean =
    currentRoute in bottomBarRoutes

/**
 * Bubbly bottom navigation bar with a Duolingo-style thick shadow strip.
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(Screen.Home, Screen.Workout, Screen.Profile)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box {
        /* ── Thick bottom shadow strip ─────────────────────────────── */
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .background(CardShadow)
        )

        /* ── Main bar ──────────────────────────────────────────────── */
        NavigationBar(
            containerColor = White,
            tonalElevation = 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(screen.labelRes)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(screen.labelRes),
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    selected = selected,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = OceanBlue,
                        selectedTextColor   = OceanBlue,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor      = MintGreen.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
