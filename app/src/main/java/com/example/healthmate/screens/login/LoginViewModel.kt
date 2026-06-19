package com.example.healthmate.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.data.AuthRepository
import com.example.healthmate.data.AuthResult
import com.example.healthmate.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    private val _needsOnboarding = MutableStateFlow(false)
    val needsOnboarding: StateFlow<Boolean> = _needsOnboarding.asStateFlow()

    fun login(email: String, password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            when (val result = repository.signInWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null && FirestoreRepository.isUserDisabled(uid)) {
                        FirebaseAuth.getInstance().signOut()
                        _authError.value = "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."
                    } else {
                        _authSuccess.value = true
                    }
                }
                is AuthResult.Error -> _authError.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            when (val result = repository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    val uid = repository.currentUser?.uid
                    if (uid != null) {
                        if (FirestoreRepository.isUserDisabled(uid)) {
                            FirebaseAuth.getInstance().signOut()
                            _authError.value = "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."
                        } else {
                            _needsOnboarding.value = try {
                                !repository.checkUserDocumentExists(uid)
                            } catch (_: Exception) {
                                false
                            }
                            _authSuccess.value = true
                        }
                    } else {
                        _authSuccess.value = true
                    }
                }
                is AuthResult.Error -> _authError.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun clearNeedsOnboarding() {
        _needsOnboarding.value = false
    }

    fun clearError() {
        _authError.value = null
    }

    fun clearSuccess() {
        _authSuccess.value = false
    }
}
