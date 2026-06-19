package com.example.healthmate.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.data.FirestoreRepository
import com.example.healthmate.data.UserProfileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val disabledUsers: Int = 0,
    val avgStreak: Double = 0.0,
    val avgCalories: Double = 0.0,
    val avgWorkouts: Double = 0.0
)

class AdminViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<UserProfileData>>(emptyList())
    val users: StateFlow<List<UserProfileData>> = _users.asStateFlow()

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allUsers = FirestoreRepository.getAllUsers()
                _users.value = allUsers
                computeStats(allUsers)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun computeStats(users: List<UserProfileData>) {
        if (users.isEmpty()) {
            _stats.value = AdminStats()
            return
        }
        _stats.value = AdminStats(
            totalUsers = users.size,
            activeUsers = users.count { !it.disabled },
            disabledUsers = users.count { it.disabled },
            avgStreak = users.map { it.currentStreak }.average(),
            avgCalories = users.map { it.totalCaloriesBurned }.average(),
            avgWorkouts = users.map { it.totalWorkouts }.average()
        )
    }

    fun toggleUserDisabled(uid: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            try {
                FirestoreRepository.setUserDisabled(uid, !currentlyDisabled)
                _actionMessage.value = if (currentlyDisabled) "Đã mở khóa người dùng" else "Đã khóa người dùng"
                loadAllUsers()
            } catch (e: Exception) {
                _actionMessage.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            try {
                FirestoreRepository.deleteUserDocument(uid)
                _actionMessage.value = "Đã xóa người dùng"
                loadAllUsers()
            } catch (e: Exception) {
                _actionMessage.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
