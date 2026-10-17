package com.example

import com.example.data.repository.AgilaRepository
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConstitutionRagTest {

    private lateinit var repository: AgilaRepository

    @Before
    fun setUp() {
        val mockEngine = MockEngine { request ->
            val body = request.body.toString()
            if (body.contains("quorum")) {
                respond(
                    "[{\"chunk_id\": \"1\", \"document_id\": \"d1\", \"content\": \"A simple majority (50% + 1) constitutes a quorum.\", \"article_ref\": \"Article IV\", \"section_ref\": \"Section 2\", \"similarity\": 0.9}]",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else if (body.contains("dues")) {
                respond(
                    "[{\"chunk_id\": \"2\", \"document_id\": \"d1\", \"content\": \"Annual dues must be paid within a 60-day grace period.\", \"article_ref\": \"Article VII\", \"section_ref\": \"Section 1\", \"similarity\": 0.9}]",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else if (body.contains("officers elected")) {
                respond(
                    "[{\"chunk_id\": \"3\", \"document_id\": \"d1\", \"content\": \"Officers are elected in November annually.\", \"article_ref\": \"Article V\", \"section_ref\": \"Section 4\", \"similarity\": 0.9}]",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        }
        repository = AgilaRepository(HttpClient(mockEngine) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        })
    }

    @Test
    fun testQuorumQuery_ReturnsGroundedAnswerWithCitations() = runTest {
        val result = repository.queryConstitution("What is the quorum requirement for regular club meetings?")

        assertFalse("Quorum query must not abstain", result.isAbstained)
        assertTrue("Answer must reference majority requirement", result.answer.contains("50% + 1") || result.answer.contains("majority"))
        assertTrue("Result must contain at least one citation", result.citations.isNotEmpty())

        val citation = result.citations.first()
        assertEquals("TFOE-PE Magna Carta & Constitution", citation.documentTitle)
        assertTrue("Citation must specify Article IV", citation.articleRef.contains("Article IV"))
    }

    @Test
    fun testDuesQuery_ReturnsGroundedAnswerWithCitations() = runTest {
        val result = repository.queryConstitution("What are the annual dues and payment grace period?")

        assertFalse("Dues query must not abstain", result.isAbstained)
        assertTrue("Answer must mention 60-day grace period", result.answer.contains("60-day"))
        assertTrue("Result must contain citations", result.citations.isNotEmpty())
    }

    @Test
    fun testElectionQuery_ReturnsGroundedAnswerWithCitations() = runTest {
        val result = repository.queryConstitution("When are club officers elected and what is their term?")

        assertFalse("Election query must not abstain", result.isAbstained)
        assertTrue("Answer must mention annual November elections", result.answer.contains("November"))
        assertTrue("Result must contain citations", result.citations.isNotEmpty())
    }

    @Test
    fun testUnsupportedQuery_SystemAbstainsSafely() = runTest {
        val result = repository.queryConstitution("What is the official recipe for fraternal banquet soup?")

        assertTrue("Unsupported out-of-scope query must abstain", result.isAbstained)
        assertNotNull("Abstention reason must be provided", result.abstentionReason)
        assertTrue("Abstention reason must explain lack of grounded evidence", result.abstentionReason!!.contains("abstained"))
        assertTrue("Citations must be empty when abstained", result.citations.isEmpty())
    }
}
