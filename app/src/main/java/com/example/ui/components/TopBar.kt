package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BumblebeeYellowTertiary
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgilaTopBar(
    title: String,
    clubCode: String = "BEEC",
    onAdminClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("agila_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = EagleBluePrimary,
            titleContentColor = Color.White
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
                        .background(BumblebeeYellowTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "AGILA Logo Shield",
                        tint = EagleBlueDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "TFOE-PE • $clubCode",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        actions = {
            if (onAdminClick != null) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.testTag("admin_portal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Admin Management Portal",
                        tint = BumblebeeYellowTertiary
                    )
                }
            }
        }
    )
}
