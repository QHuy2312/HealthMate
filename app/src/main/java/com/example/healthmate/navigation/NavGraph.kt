package com.example.healthmate.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healthmate.R
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthmate.screens.activeworkout.ActiveWorkoutScreen
import com.example.healthmate.screens.activeworkout.ActiveWorkoutViewModel
import com.example.healthmate.screens.home.HomeScreen
import com.example.healthmate.screens.home.HomeViewModel
import com.example.healthmate.screens.login.LoginScreen
import com.example.healthmate.screens.onboarding.OnboardingScreen
import com.example.healthmate.screens.profile.ProfileScreen
import com.example.healthmate.screens.register.RegisterScreen
import com.example.healthmate.screens.splash.SplashScreen
import com.example.healthmate.screens.workout.WorkoutScreen
import kotlinx.coroutines.delay
import com.example.healthmate.screens.workout.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val workoutViewModel: WorkoutViewModel = viewModel()

    val startDestination = Screen.Splash.route

    /* ── Badge dialog overlay — pops over ANY screen ────────────── */
    val badgeQueue by homeViewModel.badgeQueue.collectAsStateWithLifecycle()
    if (badgeQueue.isNotEmpty()) {
        Dialog(onDismissRequest = { homeViewModel.popBadgeQueue() }) {
            BubblyCard(
                cornerRadius = 28.dp,
                shadowHeight = 6.dp,
                surfaceColor = OceanBlue,
                shadowColor = OceanBlueDark
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏅", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.badge_dialog_title),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = badgeQueue.first().name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.badge_dialog_message),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    BubblyButton(
                        text = stringResource(R.string.badge_dialog_dismiss),
                        onClick = { homeViewModel.popBadgeQueue() },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MintGreen,
                        shadowColor = MintGreenDark,
                        shadowHeight = 4.dp,
                        cornerRadius = 16.dp,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        /* ── Splash ────────────────────────────────────────────────── */
        composable(Screen.Splash.route) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(2500)
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    homeViewModel.fetchUserProfile()
                    homeViewModel.loadDailyData()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
        }

        /* ── Login ─────────────────────────────────────────────────── */
        composable(Screen.Login.route) { _ ->
            LoginScreen(
                onLoginSuccess = { email, needsOnboarding ->
                    val user = FirebaseAuth.getInstance().currentUser
                    val nameFromEmail = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    homeViewModel.setUserName(user?.displayName ?: nameFromEmail)
                    homeViewModel.setUserEmail(email)
                    // Do NOT read auth.currentUser?.photoUrl here — Firestore is the
                    // single source of truth. fetchUserProfile() will read the photo from Firestore.
                    val cal = java.util.Calendar.getInstance()
                    val month = cal.get(java.util.Calendar.MONTH) + 1
                    val year = cal.get(java.util.Calendar.YEAR)
                    homeViewModel.setMemberSince("Tháng $month, $year")

                    if (needsOnboarding) {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        homeViewModel.fetchUserProfile()
                        homeViewModel.loadDailyData()
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        /* ── Register ──────────────────────────────────────────────── */
        composable(Screen.Register.route) { _ ->
            RegisterScreen(
                onRegisterSuccess = { userName, email ->
                    homeViewModel.setUserName(userName)
                    homeViewModel.setUserEmail(email)
                    val cal = java.util.Calendar.getInstance()
                    val month = cal.get(java.util.Calendar.MONTH) + 1
                    val year = cal.get(java.util.Calendar.YEAR)
                    homeViewModel.setMemberSince("Tháng $month, $year")
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        /* ── Onboarding ────────────────────────────────────────────── */
        composable(Screen.Onboarding.route) { _ ->
            OnboardingScreen(
                onContinue = { weight, height ->
                    homeViewModel.updateBodyStats(weight, height)
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        homeViewModel.initializeNewUserDocument(uid, weight, height)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        /* ── Home ──────────────────────────────────────────────────── */
        composable(Screen.Home.route) { _ ->
            HomeScreen(
                viewModel = homeViewModel,
                onQuickWorkout = { category ->
                    workoutViewModel.selectCategory(category)
                    navController.navigate(Screen.Workout.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        /* ── Workout ───────────────────────────────────────────────── */
        composable(Screen.Workout.route) { _ ->
            WorkoutScreen(
                onStartWorkout = { name, duration, calories, exerciseId ->
                    navController.navigate(
                        "active_workout/${Uri.encode(name)}/$duration/$calories/${Uri.encode(exerciseId)}"
                    )
                },
                completedIdsFlow = homeViewModel.completedWorkoutsToday,
                viewModel = workoutViewModel
            )
        }

        /* ── Profile ───────────────────────────────────────────────── */
        composable(Screen.Profile.route) { _ ->
            ProfileScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    homeViewModel.clearAllData()
                    workoutViewModel.clearAllData()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = homeViewModel
            )
        }

        /* ── Active Workout ──────────────────────────────────────────── */
        composable(Screen.ActiveWorkout.route) { backStackEntry ->
            val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel()
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            ActiveWorkoutScreen(
                viewModel = activeWorkoutViewModel,
                onBack = { navController.popBackStack() },
                onWorkoutComplete = { exerciseName, calories ->
                    homeViewModel.onWorkoutCompleted(
                        exerciseId = exerciseId,
                        name = exerciseName,
                        calories = calories,
                        durationMin = activeWorkoutViewModel.getDurationMin()
                    )
                    navController.popBackStack()
                }
            )
        }
    }
}
