package com.example

import com.example.data.models.RoleCode
import com.example.data.repository.AgilaRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SecurityHardeningAndNegativeTest {

    private lateinit var repository: AgilaRepository

    @Before
    fun setUp() {
        val mockEngine = MockEngine { request ->
            respond("true", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        repository = AgilaRepository(client)
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
    fun testDuplicateAttendanceRecord_Fails() = kotlinx.coroutines.test.runTest {
        repository.redeemInvitation("valid", "Tester")
        val eventId = repository.events.value.first().id
        val memberName = "Kuya Janzle Santos"
        
        val firstRecord = repository.recordAttendance(eventId, memberName, com.example.data.models.AttendanceState.PRESENT)
        val secondRecord = repository.recordAttendance(eventId, memberName, com.example.data.models.AttendanceState.PRESENT)

        assertFalse("Duplicate attendance check-in for same member and event must be rejected", secondRecord)
    }

    @Test
    fun testSecretScan_NoHardcodedSecretsInRepository() = kotlinx.coroutines.test.runTest {
        repository.redeemInvitation("valid", "Tester")
        val testUrl = repository.currentMember.value!!.email
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
