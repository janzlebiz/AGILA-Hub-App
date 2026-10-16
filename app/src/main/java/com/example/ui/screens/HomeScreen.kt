package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Announcement
import com.example.data.models.MemberProfile
import com.example.data.models.RoleAssignment
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.BumblebeeYellowTertiary
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary
import com.example.ui.theme.ForestGreenSecondary

@Composable
fun HomeScreen(
    member: MemberProfile,
    roles: List<RoleAssignment>,
    announcements: List<Announcement>,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("home_screen_feed"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting & Fraternal Identity Card
        item {
            GlassCard(testTag = "fraternal_identity_card") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Good day, Kuya ${member.nickname}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBluePrimary
                        )
                        Text(
                            text = member.fullName,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    VerifiedBadge()
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EagleBlueDark)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BumblebeeYellowTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = member.primaryClubName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = member.regionName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { role ->
                        StatusBadge(
                            text = role.role.displayName,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Fraternal Services & Workflows",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "Check In",
                    subtitle = "Attendance",
                    icon = Icons.Default.EventAvailable,
                    backgroundColor = EagleBluePrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "action_check_in",
                    onClick = { onQuickAction("ATTENDANCE") }
                )
                QuickActionItem(
                    title = "Dues & Fees",
                    subtitle = "Contributions",
                    icon = Icons.Default.AccountBalanceWallet,
                    backgroundColor = ForestGreenSecondary,
                    modifier = Modifier.weight(1f),
                    testTag = "action_contributions",
                    onClick = { onQuickAction("FINANCE") }
                )
                QuickActionItem(
                    title = "Magna Carta",
                    subtitle = "RAG Q&A",
                    icon = Icons.Default.AutoAwesome,
                    backgroundColor = EagleBlueDark,
                    modifier = Modifier.weight(1f),
                    testTag = "action_rag_qa",
                    onClick = { onQuickAction("CONSTITUTION") }
                )
            }
        }

        // Recent Bulletins Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Fraternal Bulletins",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Freshness: Live",
                    fontSize = 11.sp,
                    color = ForestGreenSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bulletin Cards Feed
        items(announcements) { ann ->
            GlassCard(testTag = "announcement_item_${ann.id}") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        text = ann.orgScopeName,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = ann.publishedAt,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ann.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = ann.body,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Posted by ${ann.authorName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = EagleBluePrimary
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "quick_action_item"
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}
