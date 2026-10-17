package com.example

import com.example.data.models.AttendanceState
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

class CoreMemberFeaturesTest {

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
    fun testCreateAnnouncement_AddsToFeed() = kotlinx.coroutines.test.runTest {
        repository.redeemInvitation("valid", "Tester")
        val initialSize = repository.announcements.value.size
        repository.createAnnouncement("Emergency Assembly", "All officers convene at 5 PM", "Bantayog Elite Eagles Club")

        val updatedFeed = repository.announcements.value
        assertEquals(initialSize + 1, updatedFeed.size)
        assertEquals("Emergency Assembly", updatedFeed.first().title)
    }

    @Test
    fun testRecordAttendance_PreventsDuplicates() = kotlinx.coroutines.test.runTest {
        repository.redeemInvitation("valid", "Tester")
        val eventId = "evt-1"
        val memberName = "Kuya Janzle Santos"

        // First check-in should succeed
        val firstCheckIn = repository.recordAttendance(eventId, memberName, AttendanceState.PRESENT)
        assertTrue("First attendance record for event must succeed", firstCheckIn)

        // Duplicate check-in should fail
        val duplicateCheckIn = repository.recordAttendance(eventId, memberName, AttendanceState.PRESENT)
        assertFalse("Duplicate attendance record for same event must be rejected", duplicateCheckIn)
    }

    @Test
    fun testRecordContribution_CalculatesTotalCorrectly() = kotlinx.coroutines.test.runTest {
        repository.redeemInvitation("valid", "Tester")
        val initialSum = repository.contributions.value.sumOf { it.amount }
        val success = repository.recordContribution("Kuya Janzle Santos", 2000.00, "Annual Convention Fee")

        assertTrue("Valid contribution entry must succeed", success)
        val newSum = repository.contributions.value.sumOf { it.amount }
        assertEquals(initialSum + 2000.00, newSum, 0.01)
    }

    @Test
    fun testUpdateEaglesId_UpdatesProfileUrls() = kotlinx.coroutines.test.runTest {
        val newFront = "https://example.com/front_id.png"
        val newBack = "https://example.com/back_id.png"

        // Ensure we have a member first
        repository.redeemInvitation("valid", "Tester")

        repository.updateEaglesId(newFront, newBack)
        val member = repository.currentMember.value!!

        assertEquals(newFront, member.eaglesIdFrontUrl)
        assertEquals(newBack, member.eaglesIdBackUrl)
    }
}
