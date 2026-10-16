package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MemberProfile
import com.example.data.models.RoleAssignment
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.BumblebeeYellowTertiary
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary

@Composable
fun ProfileScreen(
    member: MemberProfile,
    roles: List<RoleAssignment>,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onUpdateEaglesId: (front: String?, back: String?) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUploadSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("profile_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Member Profile & Eagles ID",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Light / Dark Theme Switcher Card
        GlassCard(testTag = "theme_switch_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (isDarkMode) BumblebeeYellowTertiary else EagleBluePrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isDarkMode) "Dark Theme Active" else "Pristine White Theme Active",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDarkMode) "High-contrast dark mode surface" else "Clean white major color surface",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    modifier = Modifier.testTag("theme_switch_toggle")
                )
            }
        }

        // Digital Eagles ID Card Display
        GlassCard(testTag = "eagles_id_card_preview") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = EagleBlueDark,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = BumblebeeYellowTertiary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BumblebeeYellowTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PHILIPPINE EAGLES ID",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        VerifiedBadge()
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Text(
                        text = member.fullName.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Nickname: Kuya ${member.nickname}",
                        color = BumblebeeYellowTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Club: ${member.primaryClubName} (${member.primaryClubCode})",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Region: ${member.regionName}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: ${member.status.name}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Security: Private Bucket Signed URL",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Active Roles Matrix Card
        GlassCard(testTag = "roles_matrix_card") {
            Text(
                text = "Assigned Fraternal Roles & Organization Scope",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            roles.forEach { role ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = role.role.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(
                        text = role.organizationName,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Eagles ID Image Upload Action Card
        GlassCard(testTag = "upload_eagles_id_card") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = EagleBluePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload / Refresh Official Eagles ID Card",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Eagles ID cards are stored in private Supabase Storage buckets accessible only through short-lived signed URLs.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (showUploadSuccess) {
                Text(
                    text = "Eagles ID uploaded and verified successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    onUpdateEaglesId("https://example.com/eagles_id_front_new.jpg", "https://example.com/eagles_id_back_new.jpg")
                    showUploadSuccess = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_id_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EagleBluePrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select & Upload Eagles ID Card")
            }
        }

        // Sign Out Action
        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sign_out_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out & Clear Sensitive Cache",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
