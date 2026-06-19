package com.example.healthmate.data

import com.example.healthmate.data.model.DailyRecord
import com.example.healthmate.data.model.WorkoutHistoryEntry
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.healthmate.data.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.DayOfWeek
import java.util.Calendar
import java.util.Locale

data class UserProfileData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val photoUrl: String? = null,
    val totalWorkouts: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val currentStreak: Int = 0,
    val memberSince: String = "",
    val role: String = "user",
    val disabled: Boolean = false,
    val badgeStepsUnlocked: Boolean = false,
    val badgeWaterUnlocked: Boolean = false,
    val badgeWarriorUnlocked: Boolean = false,
    val badgeCalorieDestroyerUnlocked: Boolean = false,
    val badgeEarlyBirdUnlocked: Boolean = false,
    val badgeImmortalUnlocked: Boolean = false,
    val needsBadgeReset: Boolean = false
)

object FirestoreRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun todayKey(): String = dateFormat.format(Calendar.getInstance().time)

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    private fun dailyRecordsCol(uid: String) = userDoc(uid).collection("daily_records")

    private fun workoutHistoryCol(uid: String) = userDoc(uid).collection("workout_history")

    // ── Daily Record CRUD ──────────────────────────────────────────────

    suspend fun getTodayRecord(uid: String): DailyRecord? {
        return try {
            val doc = dailyRecordsCol(uid).document(todayKey()).get().await()
            if (doc.exists()) {
                DailyRecord(
                    date = doc.getString("date") ?: todayKey(),
                    steps = (doc.getLong("steps") ?: 0L).toInt(),
                    waterCups = (doc.getLong("waterCups") ?: 0L).toInt(),
                    caloriesBurnedToday = (doc.getLong("caloriesBurnedToday") ?: 0L).toInt(),
                    completedWorkoutsToday = (doc.get("completedWorkoutsToday") as? List<String>) ?: emptyList()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createOrUpdateTodayRecord(uid: String, record: DailyRecord) {
        try {
            dailyRecordsCol(uid).document(todayKey()).set(record).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addCompletedWorkoutToToday(uid: String, exerciseId: String, calories: Int) {
        try {
            val todayRef = dailyRecordsCol(uid).document(todayKey())
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(todayRef)
                if (!snapshot.exists()) {
                    transaction.set(todayRef, mapOf(
                        "date" to todayKey(),
                        "steps" to 0,
                        "waterCups" to 0,
                        "caloriesBurnedToday" to calories,
                        "completedWorkoutsToday" to listOf(exerciseId)
                    ))
                } else {
                    val existing = (snapshot.get("completedWorkoutsToday") as? List<String>) ?: emptyList()
                    if (exerciseId !in existing) {
                        transaction.update(todayRef, "completedWorkoutsToday", existing + exerciseId)
                        transaction.update(todayRef, "caloriesBurnedToday",
                            (snapshot.getLong("caloriesBurnedToday") ?: 0L) + calories)
                    }
                }
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateWaterCups(uid: String, cups: Int) {
        try {
            val todayRef = dailyRecordsCol(uid).document(todayKey())
            val doc = todayRef.get().await()
            if (!doc.exists()) {
                todayRef.set(mapOf(
                    "date" to todayKey(),
                    "steps" to 0,
                    "waterCups" to cups,
                    "caloriesBurnedToday" to 0,
                    "completedWorkoutsToday" to emptyList<String>()
                )).await()
            } else {
                todayRef.update("waterCups", cups).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateSteps(uid: String, steps: Int) {
        try {
            val todayRef = dailyRecordsCol(uid).document(todayKey())
            val doc = todayRef.get().await()
            if (!doc.exists()) {
                todayRef.set(mapOf(
                    "date" to todayKey(),
                    "steps" to steps,
                    "waterCups" to 0,
                    "caloriesBurnedToday" to 0,
                    "completedWorkoutsToday" to emptyList<String>()
                )).await()
            } else {
                todayRef.update("steps", steps).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getLast7DaysRecords(uid: String): List<DailyRecord> {
        return try {
            val calendar = Calendar.getInstance()
            val dates = (0..6).map { offset ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -offset)
                dateFormat.format(cal.time)
            }
            val records = mutableListOf<DailyRecord>()
            for (date in dates) {
                val doc = dailyRecordsCol(uid).document(date).get().await()
                if (doc.exists()) {
                    records.add(DailyRecord(
                        date = doc.getString("date") ?: date,
                        steps = (doc.getLong("steps") ?: 0L).toInt(),
                        waterCups = (doc.getLong("waterCups") ?: 0L).toInt(),
                        caloriesBurnedToday = (doc.getLong("caloriesBurnedToday") ?: 0L).toInt(),
                        completedWorkoutsToday = (doc.get("completedWorkoutsToday") as? List<String>) ?: emptyList()
                    ))
                } else {
                    records.add(DailyRecord(date = date))
                }
            }
            records.reversed() // oldest first
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Current Week Records (Mon→Sun) ─────────────────────────────────

    suspend fun getCurrentWeekRecords(uid: String): List<DailyRecord> {
        return try {
            val today = LocalDate.now()
            val monday = today.with(DayOfWeek.MONDAY)
            val dates = (0L..6L).map { offset ->
                monday.plusDays(offset).toString() // "YYYY-MM-DD"
            }
            val records = mutableListOf<DailyRecord>()
            for (date in dates) {
                val doc = dailyRecordsCol(uid).document(date).get().await()
                if (doc.exists()) {
                    records.add(DailyRecord(
                        date = doc.getString("date") ?: date,
                        steps = (doc.getLong("steps") ?: 0L).toInt(),
                        waterCups = (doc.getLong("waterCups") ?: 0L).toInt(),
                        caloriesBurnedToday = (doc.getLong("caloriesBurnedToday") ?: 0L).toInt(),
                        completedWorkoutsToday = (doc.get("completedWorkoutsToday") as? List<String>) ?: emptyList()
                    ))
                } else {
                    records.add(DailyRecord(date = date))
                }
            }
            records
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Workout History CRUD ───────────────────────────────────────────

    suspend fun addWorkoutHistoryEntry(uid: String, entry: WorkoutHistoryEntry) {
        try {
            val data = mapOf(
                "workoutName" to entry.workoutName,
                "exerciseId" to entry.exerciseId,
                "calories" to entry.calories,
                "durationMin" to entry.durationMin,
                "timestamp" to FieldValue.serverTimestamp()
            )
            workoutHistoryCol(uid).add(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getWorkoutHistory(uid: String): List<WorkoutHistoryEntry> {
        return try {
            val snapshot = workoutHistoryCol(uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                WorkoutHistoryEntry(
                    workoutName = doc.getString("workoutName") ?: "",
                    exerciseId = doc.getString("exerciseId") ?: "",
                    calories = (doc.getLong("calories") ?: 0L).toInt(),
                    durationMin = (doc.getLong("durationMin") ?: 0L).toInt(),
                    timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── User Profile Aggregate Updates ─────────────────────────────────

    suspend fun incrementTotalWorkouts(uid: String) {
        try {
            userDoc(uid).update("totalWorkouts", FieldValue.increment(1)).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun incrementTotalCalories(uid: String, calories: Int) {
        try {
            userDoc(uid).update("totalCaloriesBurned", FieldValue.increment(calories.toLong())).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateCurrentStreak(uid: String, streak: Int) {
        try {
            userDoc(uid).update("currentStreak", streak).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Streak Computation ─────────────────────────────────────────────

    suspend fun computeCurrentStreak(uid: String): Int {
        return try {
            val snapshot = dailyRecordsCol(uid)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val activeDates = snapshot.documents
                .filter { doc ->
                    val workouts = (doc.get("completedWorkoutsToday") as? List<*>) ?: emptyList<Any>()
                    workouts.isNotEmpty()
                }
                .mapNotNull { it.getString("date") }
                .toSortedSet(compareByDescending { it })

            if (activeDates.isEmpty()) return 0

            val calendar = Calendar.getInstance()
            var streakCount = 0
            val today = dateFormat.format(calendar.time)

            // If today has no workout, start checking from yesterday
            val checkDate = if (today !in activeDates) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                dateFormat.format(calendar.time)
            } else {
                today
            }

            calendar.time = dateFormat.parse(checkDate) ?: return 0

            while (true) {
                val dateStr = dateFormat.format(calendar.time)
                if (dateStr in activeDates) {
                    streakCount++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
            streakCount
        } catch (e: Exception) {
            0
        }
    }

    // ── Daily Badge Reset ──────────────────────────────────────────────

    suspend fun resetDailyBadges(uid: String) {
        val todayStr = dateFormat.format(Calendar.getInstance().time)
        userDoc(uid).update(
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
    }

    // ── Document Existence Check ───────────────────────────────────────

    suspend fun checkUserDocumentExists(uid: String): Boolean {
        return try {
            userDoc(uid).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    // ── User Profile (moved from HomeViewModel) ───────────────────────

    suspend fun updateBadgeField(uid: String, field: String, value: Boolean) {
        try {
            userDoc(uid).update(field, value).await()
        } catch (_: Exception) { }
    }

    suspend fun updateUsername(uid: String, newName: String) {
        try {
            userDoc(uid).update("name", newName).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchUserProfile(uid: String): UserProfileData? {
        return try {
            val doc = userDoc(uid).get().await()
            if (!doc.exists()) return null

            val todayStr = dateFormat.format(java.util.Date())
            val lastReset = doc.getString("lastBadgeResetDate") ?: ""
            val needsReset = lastReset != todayStr

            if (needsReset) {
                userDoc(uid).update(
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
            }

            val memberSince = doc.getTimestamp("createdAt")?.let { ts ->
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = ts.toDate().time
                val month = cal.get(java.util.Calendar.MONTH) + 1
                val year = cal.get(java.util.Calendar.YEAR)
                "Tháng $month, $year"
            } ?: ""

            UserProfileData(
                uid = uid,
                name = doc.getString("name") ?: "",
                heightCm = doc.getDouble("heightCm") ?: 0.0,
                weightKg = doc.getDouble("weightKg") ?: 0.0,
                photoUrl = doc.getString("photoUrl"),
                totalWorkouts = (doc.getLong("totalWorkouts") ?: 0L).toInt(),
                totalCaloriesBurned = (doc.getLong("totalCaloriesBurned") ?: 0L).toInt(),
                currentStreak = (doc.getLong("currentStreak") ?: 0L).toInt(),
                memberSince = memberSince,
                role = doc.getString("role") ?: "user",
                disabled = doc.getBoolean("disabled") == true,
                badgeStepsUnlocked = doc.getBoolean("badgeStepsUnlocked") == true,
                badgeWaterUnlocked = doc.getBoolean("badgeWaterUnlocked") == true,
                badgeWarriorUnlocked = doc.getBoolean("badgeWarriorUnlocked") == true,
                badgeCalorieDestroyerUnlocked = doc.getBoolean("badgeCalorieDestroyerUnlocked") == true,
                badgeEarlyBirdUnlocked = doc.getBoolean("badgeEarlyBirdUnlocked") == true,
                badgeImmortalUnlocked = doc.getBoolean("badgeImmortalUnlocked") == true,
                needsBadgeReset = needsReset
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveOnboarding(uid: String, weightKg: Double, heightCm: Double) {
        try {
            userDoc(uid).update(
                mapOf("weightKg" to weightKg, "heightCm" to heightCm, "onboardingCompleted" to true)
            ).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun initializeNewUserDocument(
        uid: String, name: String, email: String,
        weightKg: Double, heightCm: Double, googlePhotoUrl: String?
    ) {
        try {
            val docRef = userDoc(uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val initialData = mutableMapOf<String, Any>(
                    "name" to name,
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "onboardingCompleted" to true,
                    "heightCm" to heightCm,
                    "weightKg" to weightKg,
                    "totalWorkouts" to 0L,
                    "totalCaloriesBurned" to 0L,
                    "currentStreak" to 0L,
                    "role" to "user",
                    "badgeStepsUnlocked" to false,
                    "badgeWaterUnlocked" to false,
                    "badgeWarriorUnlocked" to false,
                    "badgeCalorieDestroyerUnlocked" to false,
                    "badgeEarlyBirdUnlocked" to false,
                    "badgeImmortalUnlocked" to false,
                    "lastBadgeResetDate" to dateFormat.format(java.util.Date())
                )
                if (!googlePhotoUrl.isNullOrBlank()) {
                    initialData["photoUrl"] = googlePhotoUrl
                }
                docRef.set(initialData).await()
            } else {
                docRef.update(mapOf(
                    "weightKg" to weightKg, "heightCm" to heightCm, "onboardingCompleted" to true
                )).await()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Admin: Fetch all users ────────────────────────────────────────

    suspend fun getAllUsers(): List<UserProfileData> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                val memberSince = doc.getTimestamp("createdAt")?.let { ts ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = ts.toDate().time
                    val month = cal.get(java.util.Calendar.MONTH) + 1
                    val year = cal.get(java.util.Calendar.YEAR)
                    "Tháng $month, $year"
                } ?: ""
                UserProfileData(
                    uid = doc.id,
                    name = doc.getString("name") ?: "",
                    heightCm = doc.getDouble("heightCm") ?: 0.0,
                    weightKg = doc.getDouble("weightKg") ?: 0.0,
                    photoUrl = doc.getString("photoUrl"),
                    totalWorkouts = (doc.getLong("totalWorkouts") ?: 0L).toInt(),
                    totalCaloriesBurned = (doc.getLong("totalCaloriesBurned") ?: 0L).toInt(),
                    currentStreak = (doc.getLong("currentStreak") ?: 0L).toInt(),
                    memberSince = memberSince,
                    role = doc.getString("role") ?: "user",
                    disabled = doc.getBoolean("disabled") == true,
                    badgeStepsUnlocked = doc.getBoolean("badgeStepsUnlocked") == true,
                    badgeWaterUnlocked = doc.getBoolean("badgeWaterUnlocked") == true,
                    badgeWarriorUnlocked = doc.getBoolean("badgeWarriorUnlocked") == true,
                    badgeCalorieDestroyerUnlocked = doc.getBoolean("badgeCalorieDestroyerUnlocked") == true,
                    badgeEarlyBirdUnlocked = doc.getBoolean("badgeEarlyBirdUnlocked") == true,
                    badgeImmortalUnlocked = doc.getBoolean("badgeImmortalUnlocked") == true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun setUserDisabled(uid: String, disabled: Boolean) {
        try {
            userDoc(uid).update("disabled", disabled).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteUserDocument(uid: String) {
        try {
            userDoc(uid).delete().await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun isUserDisabled(uid: String): Boolean {
        return try {
            val doc = userDoc(uid).get().await()
            doc.getBoolean("disabled") == true
        } catch (e: Exception) {
            false
        }
    }
}
