package com.example.healthmate.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.data.UserProfileData
import com.example.healthmate.ui.theme.AmberAccent
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var userToToggle by remember { mutableStateOf<UserProfileData?>(null) }
    var userToDelete by remember { mutableStateOf<UserProfileData?>(null) }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    // Confirm disable/enable dialog
    userToToggle?.let { user ->
        val isDisabled = user.disabled
        AlertDialog(
            onDismissRequest = { userToToggle = null },
            title = { Text(if (isDisabled) "Mở khóa người dùng" else "Khóa người dùng") },
            text = { Text("Bạn có chắc muốn ${if (isDisabled) "mở khóa" else "khóa"} ${user.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleUserDisabled(user.uid, isDisabled)
                    userToToggle = null
                }) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { userToToggle = null }) { Text("Hủy") }
            }
        )
    }

    // Confirm delete dialog
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Xóa người dùng") },
            text = { Text("Bạn có chắc muốn xóa ${user.name}? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user.uid)
                    userToDelete = null
                }) { Text("Xóa", color = CoralAccent) }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản trị viên", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OceanBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Stats cards
                item {
                    Text("Thống kê", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Tổng users", "${stats.totalUsers}", OceanBlue, OceanBlueDark, Modifier.weight(1f))
                        StatCard("Đang hoạt động", "${stats.activeUsers}", MintGreen, MintGreenDark, Modifier.weight(1f))
                        StatCard("Bị khóa", "${stats.disabledUsers}", CoralAccent, CoralDark, Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Avg Streak", "%.1f ngày".format(stats.avgStreak), AmberAccent, AmberAccent.copy(alpha = 0.7f), Modifier.weight(1f))
                        StatCard("Avg Calo", "%.0f".format(stats.avgCalories), CoralAccent, CoralDark, Modifier.weight(1f))
                        StatCard("Avg Workout", "%.1f".format(stats.avgWorkouts), MintGreen, MintGreenDark, Modifier.weight(1f))
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
                item {
                    Text("Danh sách người dùng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${users.size} người dùng", color = TextSecondary, fontSize = 13.sp)
                }

                items(users, key = { it.uid }) { user ->
                    UserCard(
                        user = user,
                        onToggleDisable = { userToToggle = user },
                        onDelete = { userToDelete = user }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UserCard(
    user: UserProfileData,
    onToggleDisable: () -> Unit,
    onDelete: () -> Unit
) {
    val isDisabled = user.disabled
    val isAdmin = user.role == "admin"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDisabled) Color(0xFFF8F9FA) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (isAdmin) listOf(AmberAccent, CoralAccent)
                            else if (isDisabled) listOf(TextSecondary, TextSecondary)
                            else listOf(OceanBlue, MintGreen)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    if (isAdmin) {
                        Spacer(Modifier.width(6.dp))
                        Text("ADMIN", fontSize = 10.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
                    }
                    if (isDisabled) {
                        Spacer(Modifier.width(6.dp))
                        Text("KHÓA", fontSize = 10.sp, color = CoralAccent, fontWeight = FontWeight.Bold)
                    }
                }
                Text(user.email, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row {
                    Text("${user.totalWorkouts} bài tập", fontSize = 11.sp, color = TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text("${user.currentStreak} streak", fontSize = 11.sp, color = TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text("${user.totalCaloriesBurned} calo", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // Actions
            if (!isAdmin) {
                IconButton(onClick = onToggleDisable) {
                    Icon(
                        if (isDisabled) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = if (isDisabled) "Mở khóa" else "Khóa",
                        tint = if (isDisabled) MintGreen else AmberAccent
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Xóa", tint = CoralAccent)
                }
            }
        }
    }
}
