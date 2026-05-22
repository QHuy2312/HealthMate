package com.example.healthmate.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.ui.theme.CardShadow
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.OceanBlue
import com.example.healthmate.ui.theme.OceanBlueDark
import com.example.healthmate.ui.theme.White

/**
 * ─── Duolingo-style 3D Pressable Button ───────────────────────────────
 *
 * Uses the same layering pattern as BubblyCard:
 *  - Shadow layer: a Box with matchParentSize() that fills the parent.
 *  - Surface layer: a Box offset upward to reveal the shadow strip.
 *
 * The caller's [modifier] controls the overall size (fillMaxWidth, wrapContent, etc.).
 * No internal fillMaxWidth — the button is fully flexible.
 */
@Composable
fun BubblyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = OceanBlue,
    shadowColor: Color = OceanBlueDark,
    textColor: Color = White,
    shadowHeight: Dp = 6.dp,
    cornerRadius: Dp = 20.dp,
    fontSize: TextUnit = 18.sp,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isPressed) shadowHeight.value else 0f,
        animationSpec = tween(durationMillis = 60),
        label = "btn_offset"
    )
    val animatedBorder by animateFloatAsState(
        targetValue = if (isPressed) 0f else shadowHeight.value,
        animationSpec = tween(durationMillis = 60),
        label = "btn_border"
    )

    // Outer Box: caller's modifier controls the overall size.
    // propagateMinConstraints forces inner children to inherit the parent's
    // minimum width, so fillMaxWidth() actually stretches the surface.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        propagateMinConstraints = true,
        contentAlignment = Alignment.Center
    ) {
        // Shadow layer — fills exactly the same size as the parent Box.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(if (enabled) shadowColor else shadowColor.copy(alpha = 0.5f))
        )
        // Surface layer — offset upward to reveal the shadow strip below.
        Box(
            modifier = Modifier
                .offset(y = (-animatedOffset).dp)
                .padding(bottom = animatedBorder.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.height(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * ─── Duolingo-style 3D Card ───────────────────────────────────────────
 *
 * A white surface sitting on a subtle darker bottom border,
 * giving the card a tactile, "puffy" appearance.
 */
@Composable
fun BubblyCard(
    modifier: Modifier = Modifier,
    surfaceColor: Color = White,
    shadowColor: Color = CardShadow,
    cornerRadius: Dp = 20.dp,
    shadowHeight: Dp = 5.dp,
    content: @Composable () -> Unit
) {
    // propagateMinConstraints forces inner children to inherit the parent's
    // minimum width, so fillMaxWidth() actually stretches the surface.
    // Without this, the surface layer would fight for unconstrained space in a Row.
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        propagateMinConstraints = true
    ) {
        // Bottom shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor)
        )
        // Surface layer — offset upward to reveal the shadow strip.
        // No fillMaxWidth() here — size is inherited from the root Box.
        Box(
            modifier = Modifier
                .offset(y = (-shadowHeight / 2))
                .padding(bottom = shadowHeight)
                .clip(RoundedCornerShape(cornerRadius))
                .background(surfaceColor)
        ) {
            content()
        }
    }
}

/**
 * ─── Duolingo-style Bubbly TextField ──────────────────────────────────
 *
 * When [isPassword] = true, a trailing eye icon is automatically shown
 * so the user can toggle password visibility.
 */
@Composable
fun BubblyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    cornerRadius: Dp = 16.dp,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null, tint = OceanBlue) }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = OceanBlue.copy(alpha = 0.7f)
                        )
                    }
                }
            } else null,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(cornerRadius),
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OceanBlue,
                unfocusedBorderColor = CardShadow,
                cursorColor = OceanBlue,
                focusedLabelColor = OceanBlue,
                unfocusedLabelColor = Color(0xFFADB5BD),
                errorBorderColor = CoralAccent,
                errorCursorColor = CoralAccent,
                errorLabelColor = CoralAccent
            )
        )
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = CoralAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Reusable email + password form section used by LoginScreen.
 */
@Composable
fun LoginFormFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BubblyTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        BubblyTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Mật khẩu",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
    }
}
