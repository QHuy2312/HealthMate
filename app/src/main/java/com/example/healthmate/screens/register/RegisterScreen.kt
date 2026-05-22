package com.example.healthmate.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.R
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.components.BubblyTextField
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.White

private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")
private val PASSWORD_UPPERCASE = Regex(".*[A-Z].*")
private val PASSWORD_LOWERCASE = Regex(".*[a-z].*")
private val PASSWORD_DIGIT = Regex(".*[0-9].*")
private val PASSWORD_SPECIAL = Regex(".*[@#\$!%^&*()_+\\-=].*")

@Composable
fun RegisterScreen(
    onRegisterSuccess: (userName: String, userEmail: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    fun validateEmail() {
        emailError = when {
            email.isBlank() -> null
            !EMAIL_REGEX.matches(email.trim()) -> context.getString(R.string.validation_email_invalid)
            else -> null
        }
    }

    fun validatePassword() {
        passwordError = when {
            password.isBlank() -> null
            password.length < 8 -> context.getString(R.string.validation_password_weak)
            !PASSWORD_UPPERCASE.matches(password) -> context.getString(R.string.validation_password_weak)
            !PASSWORD_LOWERCASE.matches(password) -> context.getString(R.string.validation_password_weak)
            !PASSWORD_DIGIT.matches(password) -> context.getString(R.string.validation_password_weak)
            !PASSWORD_SPECIAL.matches(password) -> context.getString(R.string.validation_password_weak)
            else -> null
        }
        // Re-validate confirm if it's not empty
        if (confirmPassword.isNotBlank()) {
            confirmPasswordError = when {
                confirmPassword != password -> context.getString(R.string.validation_password_mismatch)
                else -> null
            }
        }
    }

    fun validateConfirmPassword() {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> null
            confirmPassword != password -> context.getString(R.string.validation_password_mismatch)
            else -> null
        }
    }

    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            onRegisterSuccess(name.trim(), email.trim())
            viewModel.clearSuccess()
        }
    }

    val isFormValid = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            && confirmPassword.isNotBlank()
            && emailError == null && passwordError == null && confirmPasswordError == null
            && EMAIL_REGEX.matches(email.trim())
            && password == confirmPassword
            && password.length >= 8
            && !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            /* ── Gradient logo ─────────────────────────────────────── */
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(listOf(MintGreen, OceanBlue))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            /* ── Title ────────────────────────────────────────────── */
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            /* ── Form card ─────────────────────────────────────────── */
            BubblyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                shadowHeight = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    /* Name */
                    BubblyTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = stringResource(R.string.register_name_hint),
                        leadingIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    /* Email */
                    BubblyTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validateEmail()
                        },
                        label = stringResource(R.string.register_email_hint),
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        isError = emailError != null,
                        errorMessage = emailError
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    /* Password */
                    BubblyTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            validatePassword()
                        },
                        label = stringResource(R.string.register_password_hint),
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isError = passwordError != null,
                        errorMessage = passwordError
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    /* Confirm password */
                    BubblyTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            validateConfirmPassword()
                        },
                        label = stringResource(R.string.register_confirm_password_hint),
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    /* Register button */
                    BubblyButton(
                        text = stringResource(R.string.register_button),
                        onClick = {
                            viewModel.clearError()
                            viewModel.register(name.trim(), email.trim(), password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MintGreen,
                        shadowColor = CoralDark,
                        fontSize = 17.sp,
                        enabled = isFormValid
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* ── Login link ────────────────────────────────────────── */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToLogin() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.register_has_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.register_login),
                    color = OceanBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* ── Error message ──────────────────────────────────────── */
            if (authError != null) {
                Text(
                    text = authError!!,
                    color = CoralAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        /* ── Loading overlay ──────────────────────────────────────── */
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MintGreen)
            }
        }
    }
}
