package com.example.healthmate.screens.profile

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.healthmate.R
import com.example.healthmate.screens.home.Badge
import com.example.healthmate.screens.home.ChatMessage
import com.example.healthmate.screens.home.HomeViewModel
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.components.BubblyTextField
import com.example.healthmate.ui.theme.AmberAccent
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.Divider
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark
import com.example.healthmate.ui.theme.MintGreenLight
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.OceanBlueLight
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val weightKg by viewModel.weightKg.collectAsStateWithLifecycle()
    val heightCm by viewModel.heightCm.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val totalCaloriesBurnedLifetime by viewModel.totalCaloriesBurnedLifetime.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val memberSince by viewModel.memberSince.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val photoUrl by viewModel.photoUrl.collectAsStateWithLifecycle()
    val profileLoaded by viewModel.profileLoaded.collectAsStateWithLifecycle()
    val workoutHistoryEntries by viewModel.workoutHistoryEntries.collectAsStateWithLifecycle()
    val energyChartValues by viewModel.energyChartValues.collectAsStateWithLifecycle()
    val energyComparisonText by viewModel.energyComparisonText.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkouts.collectAsStateWithLifecycle()
    val chatMessage by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val role by viewModel.role.collectAsStateWithLifecycle()

    var showBodyStatsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAiChatDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    /* ── Body stats edit dialog ──────────────────────────────────── */
    if (showBodyStatsDialog) {
        BodyStatsDialog(
            currentWeight = weightKg,
            currentHeight = heightCm,
            onDismiss = { showBodyStatsDialog = false },
            onConfirm = { w, h ->
                viewModel.updateBodyStats(w, h)
                showBodyStatsDialog = false
            }
        )
    }

    /* ── Edit profile dialog ─────────────────────────────── */
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = userName,
            onDismiss = {showEditProfileDialog = false},
            onConfirm = {newName ->
                viewModel.updateUsername(newName)
                showEditProfileDialog = false
            }
        )
    }

    if (showNotificationDialog) {
        NotificationDialog(onDismiss = { showNotificationDialog = false })
    }



    if (showAiChatDialog) {
        HealthAiChatDialog(
            messages = chatMessage,
            isLoading = isAiLoading,
            onSendMessage = { text -> viewModel.sendChatMessage(text) },
            onDismiss = { showAiChatDialog = false }
        )
    }

    if (showAboutDialog) {
        InfoDialog(
            title = stringResource(R.string.profile_about),
            content = "HealthMate\nPhiên bản: 1.0.1\n\nỨng dụng theo dõi sức khỏe và thể chất thông minh, giúp bạn duy trì lối sống lành mạnh mỗi ngày. Được phát triển bởi đội ngũ HealthMate-VKU. Gồm 3 thành viên Huy, Mai, Khánh.",
            icon = "🚀",
            onDismiss = { showAboutDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        /* ── Avatar & name ─────────────────────────────────────────── */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                when {
                    !profileLoaded -> {
                        // Loading placeholder — spinner while Firestore fetch completes
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = OceanBlue,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    !photoUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = stringResource(R.string.profile_change_photo),
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(OceanBlue, MintGreen))
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        val initial = userName.firstOrNull()?.uppercase() ?: "?"
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(OceanBlue, MintGreen))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 40.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = userName.ifBlank { "HealthMate User" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = { showEditProfileDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit profile",
                        modifier = Modifier.size(20.dp),
                        tint = OceanBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userEmail.ifBlank { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = memberSince.ifBlank { stringResource(R.string.profile_member_since) },
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Body stats card ───────────────────────────────────────── */
        Text(
            text = stringResource(R.string.profile_body_stats),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        BubblyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { showBodyStatsDialog = true },
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BodyStatRow(
                        label = stringResource(R.string.onboarding_weight),
                        value = if (weightKg > 0) "%.1f kg".format(weightKg) else "—"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BodyStatRow(
                        label = stringResource(R.string.onboarding_height),
                        value = if (heightCm > 0) "%.0f cm".format(heightCm) else "—"
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OceanBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.profile_edit),
                        tint = OceanBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Achievements row ──────────────────────────────────────── */
        Text(
            text = stringResource(R.string.profile_achievements),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AchievementBubblyCard(
                modifier = Modifier.weight(1f),
                value = "$totalWorkouts",
                label = stringResource(R.string.profile_workouts_done),
                accentColor = OceanBlue,
                shadowColor = OceanBlueDark
            )
            AchievementBubblyCard(
                modifier = Modifier.weight(1f),
                value = "$streak",
                label = stringResource(R.string.profile_streak),
                accentColor = MintGreen,
                shadowColor = MintGreenDark
            )
            AchievementBubblyCard(
                modifier = Modifier.weight(1f),
                value = if (totalCaloriesBurnedLifetime >= 1000) "${totalCaloriesBurnedLifetime / 1000}k" else "$totalCaloriesBurnedLifetime",
                label = stringResource(R.string.profile_calories_burned),
                accentColor = CoralAccent,
                shadowColor = CoralDark
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Energy trend chart ────────────────────────────────────── */
        Text(
            text = stringResource(R.string.profile_energy_trend),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        BubblyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            EnergyChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                dailyCalories = energyChartValues
            )
        }

        if (energyComparisonText.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            BubblyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                cornerRadius = 16.dp,
                shadowHeight = 3.dp,
                surfaceColor = MintGreen.copy(alpha = 0.12f),
                shadowColor = MintGreen.copy(alpha = 0.06f)
            ) {
                Text(
                    text = energyComparisonText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OceanBlue,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Activity history ──────────────────────────────────────── */
        Text(
            text = stringResource(R.string.profile_history),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (workoutHistoryEntries.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_no_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                val colors = listOf(CoralAccent, OceanBlue, MintGreen, AmberAccent)
                workoutHistoryEntries.take(5).forEachIndexed { index, entry ->
                    val timeAgo = getTimeAgo(entry.timestamp)
                    ActivityHistoryItem(
                        day = timeAgo,
                        activity = entry.workoutName,
                        calories = "${entry.calories} calo",
                        accentColor = colors[index % colors.size]
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Badges ────────────────────────────────────────────────── */
        Text(
            text = stringResource(R.string.profile_badges),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        var selectedBadge by remember { mutableStateOf<Badge?>(null) }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            badges.forEach { badge ->
                BadgeCard(badge) { selectedBadge = badge }
            }
        }

        selectedBadge?.let { badge ->
            BadgeDetailDialog(badge = badge, onDismiss = { selectedBadge = null })
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Settings list ─────────────────────────────────────────── */
        Text(
            text = stringResource(R.string.profile_settings),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        BubblyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 20.dp,
            shadowHeight = 5.dp
        ) {
            Column {
                if (role == "admin") {
                    SettingsRow(Icons.Default.Star, "Quản trị viên") {
                        onNavigateToAdmin()
                    }
                    HorizontalDivider(color = Divider, thickness = 0.5.dp)
                }
                SettingsRow(Icons.Default.Notifications, stringResource(R.string.profile_notifications)) {
                    showNotificationDialog = true
                }
                HorizontalDivider(color = Divider, thickness = 0.5.dp)


                SettingsRow(Icons.Default.Info, "Chat với AI Sức khỏe") {
                    showAiChatDialog = true
                }
                HorizontalDivider(color = Divider, thickness = 0.5.dp)

                SettingsRow(Icons.Default.Person, stringResource(R.string.profile_about)) {
                    showAboutDialog = true
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ── Logout button ─────────────────────────────────────────── */
        BubblyButton(
            text = stringResource(R.string.profile_logout),
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            containerColor = CoralAccent,
            shadowColor = CoralDark,
            textColor = Color.White,
            fontSize = 16.sp,
            leadingIcon = Icons.AutoMirrored.Filled.ExitToApp
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/* ──────────────────────────────────────────────────────────────────── */

@Composable
private fun BodyStatRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BodyStatsDialog(
    currentWeight: Double,
    currentHeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (weight: Double, height: Double) -> Unit
) {
    var weight by remember { mutableStateOf(if (currentWeight > 0) currentWeight.toString() else "") }
    var height by remember { mutableStateOf(if (currentHeight > 0) currentHeight.toString() else "") }

    val isValid = weight.toDoubleOrNull() != null && weight.toDouble() > 0
            && height.toDoubleOrNull() != null && height.toDouble() > 0

    Dialog(onDismissRequest = onDismiss) {
        BubblyCard(
            cornerRadius = 28.dp,
            shadowHeight = 6.dp,
            surfaceColor = MaterialTheme.colorScheme.surface,
            shadowColor = OceanBlueDark
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_body_stats),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                BubblyTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = stringResource(R.string.onboarding_weight),
                    keyboardType = KeyboardType.Decimal
                )
                Spacer(modifier = Modifier.height(12.dp))
                BubblyTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = stringResource(R.string.onboarding_height),
                    keyboardType = KeyboardType.Decimal
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BubblyButton(
                        text = "Huỷ",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        shadowColor = Divider,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cornerRadius = 16.dp,
                        fontSize = 15.sp
                    )
                    BubblyButton(
                        text = stringResource(R.string.profile_save),
                        onClick = { onConfirm(weight.toDouble(), height.toDouble()) },
                        modifier = Modifier.weight(1f),
                        enabled = isValid,
                        containerColor = MintGreen,
                        shadowColor = MintGreenDark,
                        cornerRadius = 16.dp,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────── */
@Composable
private fun AchievementBubblyCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accentColor: Color,
    shadowColor: Color
) {
    BubblyCard(
        modifier = modifier.height(100.dp),
        cornerRadius = 20.dp,
        shadowHeight = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge, onBadgeClick: () -> Unit) {
    val alpha = if (badge.unlocked) 1f else 0.4f

    BubblyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onBadgeClick() },
        cornerRadius = 20.dp,
        shadowHeight = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = badge.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (badge.unlocked) {
                Text(text = "✅", fontSize = 20.sp)
            } else {
                Text(
                    text = "🔒",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun BadgeDetailDialog(badge: Badge, onDismiss: () -> Unit) {
    val description = getBadgeDescription(badge.name)
    Dialog(onDismissRequest = onDismiss) {
        BubblyCard(
            cornerRadius = 28.dp,
            shadowHeight = 6.dp,
            surfaceColor = MaterialTheme.colorScheme.surface,
            shadowColor = OceanBlueDark
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = if (badge.unlocked) "🏅" else "🔒", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = badge.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (badge.unlocked)
                        stringResource(R.string.badge_status_unlocked)
                    else
                        stringResource(R.string.badge_status_locked),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (badge.unlocked) MintGreen else CoralAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                BubblyButton(
                    text = stringResource(R.string.badge_detail_dismiss),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = OceanBlue,
                    shadowColor = OceanBlueDark,
                    cornerRadius = 16.dp,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun getBadgeDescription(badgeName: String): String {
    return when {
        badgeName.contains("Đôi chân vàng") ->
            "Đi đủ 8.000 bước chân trong một ngày để mở khóa huy hiệu này! 🚶"
        badgeName.contains("Kiện tướng uống nước") ->
            "Uống đủ 8 ly nước trong một ngày để mở khóa! 💧"
        badgeName.contains("Chiến binh bền bỉ") ->
            "Hoàn thành 3 bài tập trong một ngày để mở khóa! 🔥"
        badgeName.contains("Kẻ hủy diệt Calo") ->
            "Đốt cháy ít nhất 300 calo trong một ngày để mở khóa! ⚡"
        badgeName.contains("Gà Trống Hiếu Học") ->
            "Hoàn thành bài tập đầu tiên của bạn để mở khóa! 🐓"
        badgeName.contains("Bất Diệt") ->
            "Duy trì chuỗi tập luyện 7 ngày liên tiếp để mở khóa! 🛡️"
        else -> "Hoàn thành thử thách để mở khóa huy hiệu này!"
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OceanBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = ">",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun EnergyChart(modifier: Modifier = Modifier, dailyCalories: List<Int> = emptyList()) {
    val barColors = listOf(
        OceanBlue, CoralAccent, MintGreen, AmberAccent,
        OceanBlueLight, CoralDark, MintGreenLight
    )
    val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    // Normalize dailyCalories (last 7 days from Firestore) to 0f..1f
    val values = if (dailyCalories.isEmpty() || dailyCalories.all { it == 0 }) {
        listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
    } else {
        val maxCal = dailyCalories.max().coerceAtLeast(1)
        dailyCalories.takeLast(7).map { it.toFloat() / maxCal }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val barCount = 7
            val gap = 12.dp.toPx()
            val totalGap = gap * (barCount + 1)
            val barWidth = (size.width - totalGap) / barCount
            val maxBarHeight = size.height

            values.forEachIndexed { index, normalized ->
                val barHeight = if (normalized > 0f) maxBarHeight * normalized else 4.dp.toPx()
                val x = gap + index * (barWidth + gap)
                val y = maxBarHeight - barHeight
                val color = if (normalized > 0f) barColors[index] else barColors[index].copy(alpha = 0.2f)

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryItem(
    day: String,
    activity: String,
    calories: String,
    accentColor: Color
) {
    BubblyCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        shadowHeight = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = calories,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = accentColor
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
){
    var name by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        BubblyCard(
            cornerRadius = 28.dp,
            shadowHeight = 6.dp,
            surfaceColor = MaterialTheme.colorScheme.surface,
            shadowColor = OceanBlueDark
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Chỉnh sửa thông tin",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                BubblyTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Tên hiển thị",
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BubblyButton(
                        text = "Huỷ",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        shadowColor = Divider,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cornerRadius = 16.dp,
                        fontSize = 15.sp
                    )
                    BubblyButton(
                        text = stringResource(R.string.profile_save),
                        onClick = { onConfirm(name) },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                        containerColor = MintGreen,
                        shadowColor = MintGreenDark,
                        cornerRadius = 16.dp,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
@Composable
private fun NotificationDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("healthmate_prefs", Context.MODE_PRIVATE) }

    var workoutReminder by remember { mutableStateOf(sharedPrefs.getBoolean("workout_remind", false)) }
    var waterReminder by remember { mutableStateOf(sharedPrefs.getBoolean("water_remind", false)) }

    // Xin quyeen thong bao
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            workoutReminder = false
            waterReminder = false
        }
    }

    fun toggleReminder(type: String, isEnable: Boolean, intervalHours: Long){
        val workManager = WorkManager.getInstance(context)
        val workName = "remind_$type"

        if (isEnable){
            val inputData = Data.Builder().putString("type", type).build()
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalHours, TimeUnit.HOURS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }else{
            workManager.cancelUniqueWork(workName)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        BubblyCard(
            cornerRadius = 28.dp,
            shadowHeight = 6.dp,
            surfaceColor = MaterialTheme.colorScheme.surface,
            shadowColor = OceanBlueDark
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.profile_notifications),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Nhắc nhở tập luyện (Mỗi 24h)", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = workoutReminder,
                        onCheckedChange = { isChecked ->
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            workoutReminder = isChecked
                            sharedPrefs.edit().putBoolean("workout_remind", isChecked).apply()
                            toggleReminder("workout", isChecked, 24)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = OceanBlue)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Công tắc Uống nước
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Nhắc nhở uống nước (Mỗi 2h)", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = waterReminder,
                        onCheckedChange = { isChecked ->
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            waterReminder = isChecked
                            sharedPrefs.edit().putBoolean("water_remind", isChecked).apply()
                            toggleReminder("water", isChecked, 2)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = OceanBlue)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                BubblyButton(
                    text = "Xong",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MintGreen,
                    shadowColor = MintGreenDark,
                    cornerRadius = 16.dp,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun HealthAiChatDialog(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
){
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(messages.size, isLoading) {
        delay(100)
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Dialog(onDismissRequest = onDismiss) {
        BubblyCard(
            cornerRadius = 24.dp,
            shadowHeight = 6.dp,
            surfaceColor = MaterialTheme.colorScheme.surface,
            shadowColor = OceanBlueDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🩺", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Bác Sĩ Sức Khỏe",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = OceanBlue
                        )
                        Text(
                            text = if (isLoading) "AI đang suy nghĩ..." else "Trực tuyến (Chỉ tư vấn y tế)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLoading) CoralAccent else MintGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = OceanBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Divider,
                    thickness = 0.5.dp
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        messages.forEach { msg ->
                            val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                            val bubbleColor =
                                if (msg.isUser) OceanBlue else MaterialTheme.colorScheme.surfaceVariant
                            val textColor =
                                if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            val shape = if (msg.isUser) {
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 2.dp
                                )
                            } else {
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 2.dp,
                                    bottomEnd = 16.dp
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = alignment
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(shape)
                                        .background(bubbleColor)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 2.dp,
                                            bottomEnd = 16.dp
                                        )
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "✍️ Chờ một chút...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Divider,
                    thickness = 0.5.dp
                )
                IsolatedChatInput(
                    isLoading = isLoading,
                    onSendMessage = { text -> onSendMessage(text) }
                )
            }
        }
    }
}

@Composable
private fun InfoDialog(title: String, content: String, icon: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        BubblyCard(
            cornerRadius = 28.dp,
            shadowHeight = 6.dp,
            surfaceColor = MaterialTheme.colorScheme.surface,
            shadowColor = OceanBlueDark
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = icon, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                BubblyButton(
                    text = "Đóng",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = OceanBlue,
                    shadowColor = OceanBlueDark,
                    cornerRadius = 16.dp,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun IsolatedChatInput(
    isLoading: Boolean,
    onSendMessage: (String) -> Unit
) {
    // Trạng thái được cô lập hoàn toàn ở đây
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = {
                    Text(
                        text = "Hỏi về triệu chứng, ăn uống...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    cursorColor = OceanBlue
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
        }

        BubblyButton(
            text = "Gửi",
            onClick = {
                if (messageText.text.isNotBlank() && !isLoading) {
                    onSendMessage(messageText.text)
                    messageText = TextFieldValue("") // Reset sau khi gửi
                }
            },
            containerColor = if (messageText.text.isNotBlank() && !isLoading) OceanBlue else Color.Gray,
            shadowColor = OceanBlueDark,
            cornerRadius = 16.dp,
            fontSize = 14.sp,
            enabled = messageText.text.isNotBlank() && !isLoading,
            modifier = Modifier.width(72.dp)
        )
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000

    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days < 7 -> "$days ngày trước"
        else -> "Lâu rồi"
    }
}
