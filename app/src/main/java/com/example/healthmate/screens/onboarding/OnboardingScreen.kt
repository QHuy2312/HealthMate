package com.example.healthmate.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmate.R
import com.example.healthmate.ui.components.BubblyButton
import com.example.healthmate.ui.components.BubblyTextField
import com.example.healthmate.ui.theme.CoralAccent
import com.example.healthmate.ui.theme.CoralDark
import com.example.healthmate.ui.theme.MintGreen
import com.example.healthmate.ui.theme.MintGreenDark

@Composable
fun OnboardingScreen(
    onContinue: (weightKg: Double, heightCm: Double) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    val isInputValid = weight.toDoubleOrNull() != null && weight.toDouble() > 0
            && height.toDoubleOrNull() != null && height.toDouble() > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        BubblyTextField(
            value = weight,
            onValueChange = { weight = it },
            label = stringResource(R.string.onboarding_weight),
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Decimal
        )

        Spacer(modifier = Modifier.height(16.dp))

        BubblyTextField(
            value = height,
            onValueChange = { height = it },
            label = stringResource(R.string.onboarding_height),
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Decimal
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        BubblyButton(
            text = stringResource(R.string.onboarding_start),
            onClick = {
                val w = weight.toDouble()
                val h = height.toDouble()
                onContinue(w, h)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isInputValid,
            containerColor = MintGreen,
            shadowColor = MintGreenDark,
            cornerRadius = 20.dp,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
