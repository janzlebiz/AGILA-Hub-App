package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.RoleCode
import com.example.data.repository.AgilaRepository
import com.example.ui.components.AgilaBottomNav
import com.example.ui.components.AgilaTopBar
import com.example.ui.components.BottomTab
import com.example.ui.screens.*
import com.example.ui.theme.AgilaHubTheme

class MainActivity : ComponentActivity() {

    private val repository = AgilaRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by repository.isDarkMode.collectAsStateWithLifecycle()

            AgilaHubTheme(darkTheme = isDarkMode) {
                val isAuthenticated by repository.isAuthenticated.collectAsStateWithLifecycle()
                val memberProfile by repository.currentMember.collectAsStateWithLifecycle()
                val activeRoles by repository.activeRoles.collectAsStateWithLifecycle()
                val announcements by repository.announcements.collectAsStateWithLifecycle()
                val events by repository.events.collectAsStateWithLifecycle()
                val attendanceRecords by repository.attendanceRecords.collectAsStateWithLifecycle()
                val contributions by repository.contributions.collectAsStateWithLifecycle()
                val documents by repository.documents.collectAsStateWithLifecycle()
                val invitations by repository.invitations.collectAsStateWithLifecycle()
                val auditLogs by repository.auditLogs.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(BottomTab.HOME) }
                var showAdminDashboard by remember { mutableStateOf(false) }

                val isOfficer = activeRoles.any {
                    it.role in listOf(RoleCode.CLUB_OFFICER, RoleCode.CLUB_ADMIN, RoleCode.REGIONAL_OFFICER, RoleCode.NATIONAL_OFFICER)
                }
                val isTreasurer = activeRoles.any {
                    it.role in listOf(RoleCode.FINANCE_OFFICER, RoleCode.CLUB_ADMIN)
                }

                if (!isAuthenticated) {
                    AuthScreen(
                        onRedeem = { token, nickname ->
                            kotlinx.coroutines.MainScope().launch {
                                repository.redeemInvitation(token, nickname)
                            }
                            true // Temporary: assumes backend success
                        }
                    )
                } else {
                    // Handle Android System Back Button cleanly
                    if (showAdminDashboard) {
                        BackHandler { showAdminDashboard = false }
                    } else if (currentTab != BottomTab.HOME) {
                        BackHandler { currentTab = BottomTab.HOME }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            AgilaTopBar(
                                title = if (showAdminDashboard) "Admin Governance" else "AGILA Hub",
                                clubCode = memberProfile.primaryClubCode,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { repository.toggleDarkMode() },
                                onAdminClick = if (isOfficer) { { showAdminDashboard = !showAdminDashboard } } else null
                            )
                        },
                        bottomBar = {
                            if (!showAdminDashboard) {
                                AgilaBottomNav(
                                    currentTab = currentTab,
                                    onTabSelected = { currentTab = it }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            if (showAdminDashboard) {
                                AdminDashboardScreen(
                                    invitations = invitations,
                                    auditLogs = auditLogs,
                                    onCreateInvitation = { email, role, targetOrg ->
                                        repository.createInvitation(email, role, targetOrg)
                                    }
                                )
                            } else {
                                when (currentTab) {
                                    BottomTab.HOME -> HomeScreen(
                                        member = memberProfile,
                                        roles = activeRoles,
                                        announcements = announcements,
                                        onQuickAction = { tabKey ->
                                            when (tabKey) {
                                                "ATTENDANCE" -> currentTab = BottomTab.ATTENDANCE
                                                "FINANCE" -> currentTab = BottomTab.FINANCE
                                                "CONSTITUTION" -> currentTab = BottomTab.CONSTITUTION
                                            }
                                        }
                                    )
                                    BottomTab.ANNOUNCEMENTS -> AnnouncementsScreen(
                                        announcements = announcements,
                                        isOfficer = isOfficer,
                                        onCreateAnnouncement = { title, body, scope ->
                                            repository.createAnnouncement(title, body, scope)
                                        },
                                        onArchiveAnnouncement = { id ->
                                            repository.archiveAnnouncement(id)
                                        }
                                    )
                                    BottomTab.ATTENDANCE -> AttendanceScreen(
                                        events = events,
                                        records = attendanceRecords,
                                        isOfficer = isOfficer,
                                        onRecordAttendance = { eventId, name, state ->
                                            repository.recordAttendance(eventId, name, state)
                                        }
                                    )
                                    BottomTab.FINANCE -> FinanceScreen(
                                        contributions = contributions,
                                        isTreasurer = isTreasurer,
                                        onRecordContribution = { name, amount, purpose ->
                                            repository.recordContribution(name, amount, purpose)
                                        }
                                    )
                                    BottomTab.CONSTITUTION -> ConstitutionRagScreen(
                                        documents = documents,
                                        onQueryConstitution = { prompt ->
                                            repository.queryConstitution(prompt)
                                        }
                                    )
                                    BottomTab.PROFILE -> ProfileScreen(
                                        member = memberProfile,
                                        roles = activeRoles,
                                        isDarkMode = isDarkMode,
                                        onToggleDarkMode = { repository.toggleDarkMode() },
                                        onUpdateEaglesId = { front, back ->
                                            repository.updateEaglesId(front, back)
                                        },
                                        onSignOut = {
                                            repository.signOut()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
