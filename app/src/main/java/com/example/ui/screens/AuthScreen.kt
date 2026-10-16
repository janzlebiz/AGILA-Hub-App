package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.BumblebeeYellowTertiary
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary

@Composable
fun AuthScreen(
    onRedeem: (token: String, nickname: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var tokenInput by remember { mutableStateOf("") }
    var nicknameInput by remember { mutableStateOf("Janzle") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EagleBlueDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "AGILA Hub Emblem",
                tint = BumblebeeYellowTertiary,
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = "AGILA HUB ANDROID",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "The Fraternal Order of Eagles\nPhilippine Eagles • BCNBR-1 • BEEC",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            GlassCard(
                modifier = Modifier.padding(top = 12.dp),
                testTag = "auth_invitation_card"
            ) {
                Text(
                    text = "Invitation-Only Member Access",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EagleBluePrimary
                )

                Text(
                    text = "Enter your single-use invitation token provided by your Club Administrator.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it; errorMessage = null },
                    label = { Text("Invitation Token / Code") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invitation_token_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    label = { Text("Preferred Fraternal Nickname (e.g., Kuya Janzle)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nickname_input"),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (tokenInput.isBlank()) {
                            errorMessage = "Please enter your valid invitation token."
                        } else {
                            val success = onRedeem(tokenInput, nicknameInput)
                            if (!success) {
                                errorMessage = "Invalid or expired invitation token."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("redeem_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EagleBluePrimary)
                ) {
                    Text(
                        text = "Redeem Invitation & Sign In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Zero-Trust Security • RLS Enforced • Supabase Auth",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
