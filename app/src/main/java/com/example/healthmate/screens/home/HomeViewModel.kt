package com.example.healthmate.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.ai.FitnessClassifier
import com.example.healthmate.ai.HealthAdvice
import com.example.healthmate.ai.HealthExpertSystem
import com.example.healthmate.data.FirestoreRepository
import com.example.healthmate.data.await
import com.example.healthmate.data.model.DailyRecord
import com.example.healthmate.data.model.WorkoutHistoryEntry
import com.example.healthmate.sensors.StepCounterManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Badge(
    val name: String,
    val unlocked: Boolean
)

data class WorkoutActivity(
    val name: String,
    val calories: Int,
    val durationMin: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val DEFAULT_AGE = 22
    }

    private val _age = MutableStateFlow(DEFAULT_AGE)
    private val stepManager = StepCounterManager(application)
    private val expertSystem = HealthExpertSystem()
    private val fitnessClassifier = FitnessClassifier(application)

    /* ════════════════════════════════════════════════════════════════════
     *  BADGE QUEUE — sequential dialog display
     * ════════════════════════════════════════════════════════════════════ */

    private val _badgeQueue = MutableStateFlow<List<Badge>>(emptyList())
    val badgeQueue: StateFlow<List<Badge>> = _badgeQueue.asStateFlow()

    fun popBadgeQueue() {
        val current = _badgeQueue.value.toMutableList()
        if (current.isNotEmpty()) current.removeAt(0)
        _badgeQueue.value = current
    }

    private fun enqueueBadge(name: String) {
        if (_badgeQueue.value.none { it.name == name }) {
            _badgeQueue.value = _badgeQueue.value + Badge(name, true)
        }
    }

    /* ════════════════════════════════════════════════════════════════════
     *  UNLOCKED BADGES STATE — persisted from Firestore, survives restart
     * ════════════════════════════════════════════════════════════════════ */

    private val _unlockedBadgeNames = MutableStateFlow<Set<String>>(emptySet())

    private val _badges = MutableStateFlow(BADGE_DEFAULTS)
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    private fun rebuildBadgesList() {
        val unlocked = _unlockedBadgeNames.value
        _badges.value = ALL_BADGE_NAMES.map { name -> Badge(name, name in unlocked) }
    }

    private fun markBadgeUnlocked(name: String, firestoreField: String) {
        if (name in _unlockedBadgeNames.value) return
        _unlockedBadgeNames.value = _unlockedBadgeNames.value + name
        rebuildBadgesList()
        enqueueBadge(name)
        persistBadgeState(firestoreField, true)
    }

    private fun persistBadgeState(field: String, value: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("users").document(uid)
                    .update(field, value).await()
            } catch (_: Exception) { }
        }
    }

    /* ════════════════════════════════════════════════════════════════════
     *  USER PROFILE
     * ════════════════════════════════════════════════════════════════════ */

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _memberSince = MutableStateFlow("")
    val memberSince: StateFlow<String> = _memberSince.asStateFlow()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    private val _profileLoaded = MutableStateFlow(false)
    val profileLoaded: StateFlow<Boolean> = _profileLoaded.asStateFlow()

    fun setUserName(name: String) { _userName.value = name }
    fun setUserEmail(email: String) { _userEmail.value = email }
    fun setMemberSince(date: String) { _memberSince.value = date }

    fun updateUsername(newName: String){
        _userName.value = newName
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("user").document(uid)
                    .update("name", newName).await()
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val doc = Firebase.firestore.collection("users").document(uid).get().await()
                if (!doc.exists()) return@launch

                doc.getString("name")?.let { _userName.value = it }
                doc.getString("email")?.let { _userEmail.value = it }
                doc.getDouble("heightCm")?.let { _heightCm.value = it }
                doc.getDouble("weightKg")?.let { _weightKg.value = it }

                // Photo — Firestore is the ONLY source of truth for the avatar.
                // Do NOT fall back to auth.currentUser?.photoUrl — it resets to the
                // Google provider photo on every token refresh, destroying custom uploads.
                val url = doc.getString("photoUrl")
                android.util.Log.d("HomeVM", "fetchUserProfile: docId=${doc.id}, photoUrl='$url', exists=${doc.exists()}")
                if (!url.isNullOrBlank()) _photoUrl.value = url

                doc.getLong("totalWorkouts")?.let { _totalWorkouts.value = it.toInt() }
                doc.getLong("totalCaloriesBurned")?.let { _totalCaloriesBurnedLifetime.value = it.toInt() }
                doc.getLong("currentStreak")?.let { _streak.value = it.toInt() }

                // Restore ALL badge unlock states from Firestore
                val unlocked = mutableSetOf<String>()
                if (doc.getBoolean("badgeStepsUnlocked") == true)          unlocked.add(BADGE_STEPS)
                if (doc.getBoolean("badgeWaterUnlocked") == true)          unlocked.add(BADGE_WATER)
                if (doc.getBoolean("badgeWarriorUnlocked") == true)        unlocked.add(BADGE_WARRIOR)
                if (doc.getBoolean("badgeCalorieDestroyerUnlocked") == true) unlocked.add(BADGE_CALORIE)
                if (doc.getBoolean("badgeEarlyBirdUnlocked") == true)      unlocked.add(BADGE_EARLY)
                if (doc.getBoolean("badgeImmortalUnlocked") == true)       unlocked.add(BADGE_IMMORTAL)
                _unlockedBadgeNames.value = unlocked
                rebuildBadgesList()

                // Daily badge reset — if the calendar day has changed, clear all badges
                val lastReset = doc.getString("lastBadgeResetDate") ?: ""
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(java.util.Date())
                if (lastReset != todayStr) {
                    Firebase.firestore.collection("users").document(uid).update(
                        mapOf(
                            "badgeStepsUnlocked" to false,
                            "badgeWaterUnlocked" to false,
                            "badgeWarriorUnlocked" to false,
                            "badgeCalorieDestroyerUnlocked" to false,
                            "badgeEarlyBirdUnlocked" to false,
                            "badgeImmortalUnlocked" to false,
                            "lastBadgeResetDate" to todayStr
                        )
                    ).await()
                    _unlockedBadgeNames.value = emptySet()
                    rebuildBadgesList()
                }

                doc.getTimestamp("createdAt")?.let { ts ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = ts.toDate().time
                    val month = cal.get(java.util.Calendar.MONTH) + 1
                    val year = cal.get(java.util.Calendar.YEAR)
                    _memberSince.value = "Tháng $month, $year"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _profileLoaded.value = true
            }
        }
    }

    fun saveOnboardingToFirestore(uid: String, weightKg: Double, heightCm: Double) {
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("users").document(uid).update(
                    mapOf("weightKg" to weightKg, "heightCm" to heightCm, "onboardingCompleted" to true)
                ).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun initializeNewUserDocument(uid: String, weightKg: Double, heightCm: Double) {
        viewModelScope.launch {
            try {
                val userDoc = Firebase.firestore.collection("users").document(uid)
                val snapshot = userDoc.get().await()
                if (!snapshot.exists()) {
                    // Brand-new user — create document with default stats.
                    // This is the ONLY time we read auth.currentUser?.photoUrl:
                    // to seed the initial Firestore photo for Google users.
                    // For email/password users, photoUrl is omitted entirely (stays absent in Firestore).
                    val initialData = mutableMapOf<String, Any>(
                        "name" to _userName.value,
                        "email" to _userEmail.value,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "onboardingCompleted" to true,
                        "heightCm" to heightCm,
                        "weightKg" to weightKg,
                        "totalWorkouts" to 0L,
                        "totalCaloriesBurned" to 0L,
                        "currentStreak" to 0L,
                        "badgeStepsUnlocked" to false,
                        "badgeWaterUnlocked" to false,
                        "badgeWarriorUnlocked" to false,
                        "badgeCalorieDestroyerUnlocked" to false,
                        "badgeEarlyBirdUnlocked" to false,
                        "badgeImmortalUnlocked" to false,
                        "lastBadgeResetDate" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            .format(java.util.Date())
                    )
                    // Seed Google photo ONCE at document creation — never again after this
                    val googlePhoto = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
                    if (!googlePhoto.isNullOrBlank()) {
                        initialData["photoUrl"] = googlePhoto
                    }
                    userDoc.set(initialData).await()
                } else {
                    // Document already exists — only update body stats, never touch photoUrl
                    userDoc.update(mapOf(
                        "weightKg" to weightKg, "heightCm" to heightCm, "onboardingCompleted" to true
                    )).await()
                }
                _weightKg.value = weightKg
                _heightCm.value = heightCm
            } catch (e: Exception) { e.printStackTrace() } finally {
                _profileLoaded.value = true
            }
        }
    }

    /* ════════════════════════════════════════════════════════════════════
     *  FIRESTORE-BACKED DAILY DATA
     * ════════════════════════════════════════════════════════════════════ */

    private val _completedWorkoutsToday = MutableStateFlow<Set<String>>(emptySet())
    val completedWorkoutsToday: StateFlow<Set<String>> = _completedWorkoutsToday.asStateFlow()

    private val _energyChartValues = MutableStateFlow<List<Int>>(List(7) { 0 })
    val energyChartValues: StateFlow<List<Int>> = _energyChartValues.asStateFlow()

    private val _energyComparisonText = MutableStateFlow("")
    val energyComparisonText: StateFlow<String> = _energyComparisonText.asStateFlow()

    private val _workoutHistoryEntries = MutableStateFlow<List<WorkoutHistoryEntry>>(emptyList())
    val workoutHistoryEntries: StateFlow<List<WorkoutHistoryEntry>> = _workoutHistoryEntries.asStateFlow()

    private val _totalWorkouts = MutableStateFlow(0)
    val totalWorkouts: StateFlow<Int> = _totalWorkouts.asStateFlow()

    private val _totalCaloriesBurnedLifetime = MutableStateFlow(0)
    val totalCaloriesBurnedLifetime: StateFlow<Int> = _totalCaloriesBurnedLifetime.asStateFlow()

    fun loadDailyData() {
        // Always fetch the root user document (photoUrl, name, badges, etc.)
        // This guarantees _photoUrl is populated on every startup path,
        // including cold start when the Splash route navigates before fetch completes.
        fetchUserProfile()
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val todayRecord = FirestoreRepository.getTodayRecord(uid)
                if (todayRecord != null) {
                    _waterCups.value = todayRecord.waterCups
                    _completedWorkoutsToday.value = todayRecord.completedWorkoutsToday.toSet()
                    _completedWorkoutCalories.value = todayRecord.caloriesBurnedToday
                    _workoutsTodayCount.value = todayRecord.completedWorkoutsToday.size
                }
                val weekRecords = FirestoreRepository.getCurrentWeekRecords(uid)
                _energyChartValues.value = weekRecords.map { it.caloriesBurnedToday }
                // Compute day-over-day comparison
                val todayIdx = java.time.LocalDate.now().dayOfWeek.value - 1 // 0=Mon, 6=Sun
                val todayCal = weekRecords.getOrNull(todayIdx)?.caloriesBurnedToday ?: 0
                val yesterdayCal = weekRecords.getOrNull(todayIdx - 1)?.caloriesBurnedToday ?: 0
                _energyComparisonText.value = when {
                    yesterdayCal == 0 && todayCal == 0 -> ""
                    yesterdayCal == 0 -> "Hôm nay bạn đã đốt $todayCal calo! Bắt đầu tuyệt vời! 🔥"
                    else -> {
                        val diff = ((todayCal - yesterdayCal).toDouble() / yesterdayCal * 100).toInt()
                        if (diff >= 0) "Hôm nay bạn đã đốt nhiều hơn hôm qua $diff% calo! 🔥"
                        else "Hôm nay ít hơn hôm qua ${-diff}%, cố lên nhé! 💪"
                    }
                }
                _workoutHistoryEntries.value = FirestoreRepository.getWorkoutHistory(uid)
                val streak = FirestoreRepository.computeCurrentStreak(uid)
                _streak.value = streak
                FirestoreRepository.updateCurrentStreak(uid, streak)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    /* ════════════════════════════════════════════════════════════════════
     *  WORKOUT COMPLETION — triggers badge evaluation
     * ════════════════════════════════════════════════════════════════════ */

    private val _workoutsTodayCount = MutableStateFlow(0)

    fun onWorkoutCompleted(exerciseId: String, name: String, calories: Int, durationMin: Int) {
        // 1. Update local state instantly
        _completedWorkoutsToday.value = _completedWorkoutsToday.value + exerciseId
        _completedWorkoutCalories.value += calories
        _totalWorkouts.value += 1
        _totalCaloriesBurnedLifetime.value += calories
        _workoutsTodayCount.value += 1
        _workoutHistoryEntries.value = listOf(
            WorkoutHistoryEntry(name, exerciseId, calories, durationMin, System.currentTimeMillis())
        ) + _workoutHistoryEntries.value

        // 2. Evaluate badges immediately
        evaluateBadgesAfterWorkout(calories)

        // 3. Persist to Firestore in background
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                FirestoreRepository.addCompletedWorkoutToToday(uid, exerciseId, calories)
                FirestoreRepository.addWorkoutHistoryEntry(uid, WorkoutHistoryEntry(
                    workoutName = name, exerciseId = exerciseId,
                    calories = calories, durationMin = durationMin
                ))
                FirestoreRepository.incrementTotalWorkouts(uid)
                FirestoreRepository.incrementTotalCalories(uid, calories)
                val streak = FirestoreRepository.computeCurrentStreak(uid)
                FirestoreRepository.updateCurrentStreak(uid, streak)
                _streak.value = streak
                val weekRecords = FirestoreRepository.getCurrentWeekRecords(uid)
                _energyChartValues.value = weekRecords.map { it.caloriesBurnedToday }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun evaluateBadgesAfterWorkout(calories: Int) {
        // Early Bird: first workout ever
        if (_totalWorkouts.value >= 1) markBadgeUnlocked(BADGE_EARLY, "badgeEarlyBirdUnlocked")
        // Warrior: 3 workouts today
        if (_workoutsTodayCount.value >= 3) markBadgeUnlocked(BADGE_WARRIOR, "badgeWarriorUnlocked")
        // Calorie Destroyer: 300+ kcal burned today
        if (_completedWorkoutCalories.value >= 300) markBadgeUnlocked(BADGE_CALORIE, "badgeCalorieDestroyerUnlocked")
        // Immortal: 7-day streak
        if (_streak.value >= 7) markBadgeUnlocked(BADGE_IMMORTAL, "badgeImmortalUnlocked")
    }

    /* ════════════════════════════════════════════════════════════════════
     *  BODY STATS
     * ════════════════════════════════════════════════════════════════════ */

    private val _weightKg = MutableStateFlow(0.0)
    val weightKg: StateFlow<Double> = _weightKg.asStateFlow()

    private val _heightCm = MutableStateFlow(0.0)
    val heightCm: StateFlow<Double> = _heightCm.asStateFlow()

    fun updateBodyStats(weightKg: Double, heightCm: Double) {
        _weightKg.value = weightKg
        _heightCm.value = heightCm
    }

    /* ════════════════════════════════════════════════════════════════════
     *  STEPS
     * ════════════════════════════════════════════════════════════════════ */

    val steps: StateFlow<Int> = stepManager.stepCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /* ════════════════════════════════════════════════════════════════════
     *  CALORIES BURNED
     * ════════════════════════════════════════════════════════════════════ */

    private val _completedWorkoutCalories = MutableStateFlow(0)

    val caloriesBurned: StateFlow<Int> = combine(steps, _completedWorkoutCalories) { s, wc ->
        (s * 0.04).toInt() + wc
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /* ════════════════════════════════════════════════════════════════════
     *  STREAK
     * ════════════════════════════════════════════════════════════════════ */

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    /* ════════════════════════════════════════════════════════════════════
     *  ML FITNESS LEVEL
     * ════════════════════════════════════════════════════════════════════ */

    private val _fitnessLevel = MutableStateFlow(1)
    val fitnessLevel: StateFlow<Int> = _fitnessLevel.asStateFlow()

    /* ════════════════════════════════════════════════════════════════════
     *  AI HEALTH ADVICE
     * ════════════════════════════════════════════════════════════════════ */

    val advice: StateFlow<HealthAdvice> = combine(steps, _fitnessLevel) { stepCount, fitness ->
        val w = _weightKg.value
        val h = _heightCm.value / 100.0
        if (w <= 0.0 || h <= 0.0) {
            HealthAdvice("Chưa đủ dữ liệu", "Hãy cập nhật chiều cao/cân nặng để AI phân tích nhé! 📏")
        } else {
            val bmi = w / (h * h)
            val bmiStatus = when {
                bmi < 18.5 -> "Thiếu cân"
                bmi < 25.0 -> "Bình thường"
                bmi < 30.0 -> "Thừa cân"
                else -> "Béo phì"
            }
            val fitnessStatus = when (fitness) {
                0 -> "Thể lực yếu"; 1 -> "Thể lực trung bình"; 2 -> "Thể lực khỏe"; else -> "Thể lực trung bình"
            }
            val fitnessAdvice = when (fitness) {
                0 -> "Cảnh báo: Thể lực yếu, cần vận động tích cực hơn! 🏥"
                1 -> "Thể lực ổn định, tiếp tục duy trì tiến độ! 💪"
                2 -> "Thể lực tuyệt vời, chuẩn chiến binh! 🏆"
                else -> "Thể lực ổn định, tiếp tục duy trì tiến độ! 💪"
            }
            val stepAdvice = when {
                stepCount < 3000 -> "Hôm nay mới $stepCount bước, tranh thủ đi thêm nhé! 🚶"
                stepCount < 5000 -> "$stepCount bước rồi, ráng lên 8000 nhé! 🎯"
                stepCount < 8000 -> "$stepCount bước, gần đạt mục tiêu rồi! 💪"
                stepCount < 10000 -> "$stepCount bước, xuất sắc! 🏆"
                else -> "$stepCount bước?! Quá đỉnh! 🌟"
            }
            HealthAdvice(
                status = "$fitnessStatus | BMI %.1f | $bmiStatus".format(bmi),
                message = "$fitnessAdvice $stepAdvice"
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        HealthAdvice("Chưa đủ dữ liệu", "Hãy cập nhật chiều cao/cân nặng để AI phân tích nhé! 📏")
    )

    /* ════════════════════════════════════════════════════════════════════
     *  HEART RATE (simulated)
     * ════════════════════════════════════════════════════════════════════ */

    private val _heartRate = MutableStateFlow(72)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(4_000)
                _heartRate.value = Random.nextInt(71, 79)
            }
        }
        // Step badge — evaluated in ViewModel, NOT in UI composition
        viewModelScope.launch {
            steps.collect { s ->
                if (s >= 8000) markBadgeUnlocked(BADGE_STEPS, "badgeStepsUnlocked")
            }
        }
        // ML fitness classification
        viewModelScope.launch {
            combine(_weightKg, _heightCm, steps) { w, h, s -> Triple(w, h, s) }
                .collect { (w, h, s) ->
                    if (w > 0.0 && h > 0.0) {
                        _fitnessLevel.value = fitnessClassifier.classifyFitness(
                            age = _age.value, heightCm = h, weightKg = w, steps = s
                        )
                    }
                }
        }
    }

    /* ════════════════════════════════════════════════════════════════════
     *  WATER TRACKER — triggers badge evaluation
     * ════════════════════════════════════════════════════════════════════ */

    private val _waterCups = MutableStateFlow(0)
    val waterCups: StateFlow<Int> = _waterCups.asStateFlow()

    fun addWaterCup() {
        if (_waterCups.value >= 8) return
        _waterCups.value++

        // Evaluate badges immediately
        if (_waterCups.value >= 8) markBadgeUnlocked(BADGE_WATER, "badgeWaterUnlocked")

        // Persist to Firestore
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                FirestoreRepository.updateWaterCups(uid, _waterCups.value)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    /* ════════════════════════════════════════════════════════════════════
     *  CLEAR / LOGOUT
     * ════════════════════════════════════════════════════════════════════ */

    fun clearAllData() {
        _userName.value = ""; _userEmail.value = ""; _memberSince.value = ""
        _photoUrl.value = null; _profileLoaded.value = false; _waterCups.value = 0; _completedWorkoutCalories.value = 0
        _completedWorkoutsToday.value = emptySet(); _workoutHistoryEntries.value = emptyList()
        _energyChartValues.value = List(7) { 0 }; _energyComparisonText.value = ""; _totalWorkouts.value = 0
        _totalCaloriesBurnedLifetime.value = 0; _streak.value = 0
        _weightKg.value = 0.0; _heightCm.value = 0.0; _heartRate.value = 72
        _fitnessLevel.value = 1; _age.value = DEFAULT_AGE
        _workoutsTodayCount.value = 0
        _unlockedBadgeNames.value = emptySet()
        _badges.value = BADGE_DEFAULTS
        _badgeQueue.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        fitnessClassifier.close()
    }
}

/* ════════════════════════════════════════════════════════════════════
 *  BADGE CONSTANTS — single source of truth
 * ════════════════════════════════════════════════════════════════════ */

private const val BADGE_STEPS     = "Đôi chân vàng 👟"
private const val BADGE_WATER     = "Kiện tướng uống nước 💧"
private const val BADGE_WARRIOR   = "Chiến binh bền bỉ 🔥"
private const val BADGE_CALORIE   = "Kẻ hủy diệt Calo ⚡"
private const val BADGE_EARLY     = "Gà Trống Hiếu Học 🐓"
private const val BADGE_IMMORTAL  = "Bất Diệt 🛡️"

private val ALL_BADGE_NAMES = listOf(
    BADGE_STEPS, BADGE_WATER, BADGE_WARRIOR, BADGE_CALORIE,
    BADGE_EARLY, BADGE_IMMORTAL
)

private val BADGE_DEFAULTS = ALL_BADGE_NAMES.map { Badge(it, false) }
