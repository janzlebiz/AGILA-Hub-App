package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    ANNOUNCEMENTS("Bulletins", Icons.Filled.Campaign, Icons.Outlined.Campaign, "tab_announcements"),
    ATTENDANCE("Attendance", Icons.Filled.Event, Icons.Outlined.Event, "tab_attendance"),
    FINANCE("Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "tab_finance"),
    CONSTITUTION("RAG Q&A", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "tab_constitution"),
    PROFILE("Eagles ID", Icons.Filled.Badge, Icons.Outlined.Badge, "tab_profile")
}

@Composable
fun AgilaBottomNav(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("agila_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        BottomTab.values().forEach { tab ->
            val isSelected = tab == currentTab
            NavigationBarItem(
                modifier = Modifier.testTag(tab.testTag),
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
