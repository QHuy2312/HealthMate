package com.example.healthmate.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmate.data.AuthRepository
import com.example.healthmate.data.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    fun register(name: String, email: String, password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            when (val result = repository.createUserWithEmailAndPassword(name, email, password)) {
                is AuthResult.Success -> _authSuccess.value = true
                is AuthResult.Error -> _authError.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _authError.value = null
    }

    fun clearSuccess() {
        _authSuccess.value = false
    }
}
