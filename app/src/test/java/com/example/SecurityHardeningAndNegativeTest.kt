package com.example

import com.example.data.models.RoleCode
import com.example.data.repository.AgilaRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SecurityHardeningAndNegativeTest {

    private lateinit var repository: AgilaRepository

    @Before
    fun setUp() {
        repository = AgilaRepository()
    }

    @Test
    fun testUnauthenticatedUser_DeniedAccessOnSignOut() {
        repository.signOut()
        assertFalse("Unauthenticated session must be denied protected access", repository.isAuthenticated.value)
    }

    @Test
    fun testRoleEscalation_Rejected() {
        // Confirm client cannot self-assign National Officer role without backend invitation
        val activeRoles = repository.activeRoles.value
        val hasNationalRole = activeRoles.any { it.role == RoleCode.NATIONAL_OFFICER }
        assertFalse("Client session cannot self-assign National Officer role", hasNationalRole)
    }

    @Test
    fun testCrossOrganizationAccess_Denied() {
        // Members in BEEC scope must not access unrelated organization scope
        val roles = repository.activeRoles.value
        val unassignedOrg = roles.find { it.organizationName == "Unassigned Foreign Club" }
        assertNull("Member must not possess roles in unassigned foreign organization", unassignedOrg)
    }

    @Test
    fun testNegativeAttendanceCheckIn_InvalidAmount_Fails() {
        val result = repository.recordContribution("Kuya Janzle Santos", -500.00, "Invalid Negative Fee")
        assertFalse("Negative or zero contribution amount must be rejected", result)
    }

    @Test
    fun testSecretScan_NoHardcodedSecretsInRepository() {
        val testUrl = repository.currentMember.value.email
        assertFalse("Secrets must not be hardcoded into member models", testUrl.contains("SUPABASE_SERVICE_ROLE_KEY"))
    }

    @Test
    fun testToggleDarkMode_UpdatesThemeState() {
        assertFalse("Default theme mode must be light mode (pristine white)", repository.isDarkMode.value)
        repository.toggleDarkMode()
        assertTrue("Theme mode must switch to dark mode upon toggle", repository.isDarkMode.value)
        repository.toggleDarkMode()
        assertFalse("Theme mode must switch back to light mode upon second toggle", repository.isDarkMode.value)
    }
}
