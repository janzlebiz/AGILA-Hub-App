package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BumblebeeYellowSoftAccent
import com.example.ui.theme.EagleBlueLightAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgilaTopBar(
    title: String,
    clubCode: String = "BEEC",
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onAdminClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("agila_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_agila_hub_logo),
                        contentDescription = "AGILA Hub Emblem",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "TFOE-PE • $clubCode",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Minimalist Light / Dark Switcher Button
            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Light/Dark Theme",
                    tint = if (isDarkMode) BumblebeeYellowSoftAccent else EagleBlueLightAccent
                )
            }

            if (onAdminClick != null) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.testTag("admin_portal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Admin Management Portal",
                        tint = BumblebeeYellowSoftAccent
                    )
                }
            }
        }
    )
}
