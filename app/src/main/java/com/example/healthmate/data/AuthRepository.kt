package com.example.healthmate.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = Firebase.firestore

    val currentUser get() = auth.currentUser

    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Đã xảy ra lỗi")
        }
    }

    suspend fun createUserWithEmailAndPassword(name: String, email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Không thể lấy thông tin người dùng")

            val userData = mapOf(
                "name" to name,
                "email" to email,
                "createdAt" to FieldValue.serverTimestamp(),
                "onboardingCompleted" to false,
                "heightCm" to 0.0,
                "weightKg" to 0.0,
                "totalWorkouts" to 0L,
                "totalCaloriesBurned" to 0L,
                "currentStreak" to 0L
                // photoUrl is NOT set here — populated later by uploadProfileImage() or syncGooglePhotoToFirestore()
            )
            firestore.collection("users").document(uid).set(userData).await()

            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Đã xảy ra lỗi")
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Đã xảy ra lỗi")
        }
    }

    suspend fun fetchUserProfile(uid: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) document.data else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns true if the user document exists, false if it doesn't.
     * THROWS on network errors — callers must handle this to avoid
     * incorrectly treating a network failure as "needs onboarding".
     */
    suspend fun checkUserDocumentExists(uid: String): Boolean {
        return firestore.collection("users").document(uid).get().await().exists()
    }

    fun signOut() {
        auth.signOut()
    }
}
