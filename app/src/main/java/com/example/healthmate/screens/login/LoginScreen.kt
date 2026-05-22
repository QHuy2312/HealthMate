package com.example.healthmate.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import com.google.firebase.auth.FirebaseAuth
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmate.R
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyCard
import com.example.healthmate.ui.components.BubblyTextField
import com.example.healthmate.ui.components.LoginMascotAnimation
import com.example.healthmate.ui.theme.CardShadow
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.TextPrimary
import com.example.healthmate.ui.theme.White
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, needsOnboarding: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

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
            else -> null
        }
    }

    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            onLoginSuccess(email.trim(), viewModel.needsOnboarding.value)
            viewModel.clearSuccess()
            viewModel.clearNeedsOnboarding()
        }
    }

    val isFormValid = email.isNotBlank() && password.isNotBlank() && !isLoading
            && emailError == null && passwordError == null
            && EMAIL_REGEX.matches(email.trim())

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
            Spacer(modifier = Modifier.height(48.dp))

            /* ── Lottie mascot animation (sole visual) ────────────── */
            LoginMascotAnimation()

            Spacer(modifier = Modifier.height(20.dp))

            /* ── Title ────────────────────────────────────────────── */
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(28.dp))

            /* ── Form card ─────────────────────────────────────────── */
            BubblyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                shadowHeight = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    BubblyTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validateEmail()
                        },
                        label = stringResource(R.string.login_email_hint),
                        leadingIcon = Icons.Default.Person,
                        keyboardType = KeyboardType.Email,
                        isError = emailError != null,
                        errorMessage = emailError
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BubblyTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            validatePassword()
                        },
                        label = stringResource(R.string.login_password_hint),
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isError = passwordError != null,
                        errorMessage = passwordError
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    /* ── Forgot password ────────────────────────────── */
                    Text(
                        text = stringResource(R.string.login_forgot_password),
                        color = OceanBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val trimmed = email.trim()
                                if (trimmed.isBlank() || !EMAIL_REGEX.matches(trimmed)) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.validation_email_invalid),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    FirebaseAuth.getInstance()
                                        .sendPasswordResetEmail(trimmed)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Đã gửi email khôi phục mật khẩu đến $trimmed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                e.localizedMessage ?: "Gửi email thất bại",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    /* ── Login button ───────────────────────────────── */
                    BubblyButton(
                        text = stringResource(R.string.login_button),
                        onClick = {
                            viewModel.clearError()
                            viewModel.login(email.trim(), password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 17.sp,
                        enabled = isFormValid
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            /* ── Sign-up link ──────────────────────────────────────── */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.login_sign_up),
                    color = MintGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onNavigateToRegister() }
                        .background(MintGreen.copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                Spacer(modifier = Modifier.height(12.dp))
            }

            /* ── Google Sign-In button ──────────────────────────────── */
            BubblyButton(
                text = stringResource(R.string.login_google_button),
                onClick = {
                    viewModel.clearError()
                    scope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential
                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                            }
                        } catch (_: GetCredentialException) {
                            // User cancelled or error
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = White,
                shadowColor = CardShadow,
                textColor = TextPrimary,
                fontSize = 16.sp,
                enabled = !isLoading
            )

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
                CircularProgressIndicator(color = OceanBlue)
            }
        }
    }
}
