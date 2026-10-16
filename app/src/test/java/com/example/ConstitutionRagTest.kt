package com.example

import com.example.data.repository.AgilaRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConstitutionRagTest {

    private lateinit var repository: AgilaRepository

    @Before
    fun setUp() {
        repository = AgilaRepository()
    }

    @Test
    fun testQuorumQuery_ReturnsGroundedAnswerWithCitations() {
        val result = repository.queryConstitution("What is the quorum requirement for regular club meetings?")

        assertFalse("Quorum query must not abstain", result.isAbstained)
        assertTrue("Answer must reference majority requirement", result.answer.contains("50% + 1") || result.answer.contains("majority"))
        assertTrue("Result must contain at least one citation", result.citations.isNotEmpty())

        val citation = result.citations.first()
        assertEquals("TFOE-PE Magna Carta & Constitution", citation.documentTitle)
        assertTrue("Citation must specify Article IV", citation.articleRef.contains("Article IV"))
    }

    @Test
    fun testDuesQuery_ReturnsGroundedAnswerWithCitations() {
        val result = repository.queryConstitution("What are the annual dues and payment grace period?")

        assertFalse("Dues query must not abstain", result.isAbstained)
        assertTrue("Answer must mention 60-day grace period", result.answer.contains("60-day"))
        assertTrue("Result must contain citations", result.citations.isNotEmpty())
    }

    @Test
    fun testElectionQuery_ReturnsGroundedAnswerWithCitations() {
        val result = repository.queryConstitution("When are club officers elected and what is their term?")

        assertFalse("Election query must not abstain", result.isAbstained)
        assertTrue("Answer must mention annual November elections", result.answer.contains("November"))
        assertTrue("Result must contain citations", result.citations.isNotEmpty())
    }

    @Test
    fun testUnsupportedQuery_SystemAbstainsSafely() {
        val result = repository.queryConstitution("What is the official recipe for fraternal banquet soup?")

        assertTrue("Unsupported out-of-scope query must abstain", result.isAbstained)
        assertNotNull("Abstention reason must be provided", result.abstentionReason)
        assertTrue("Abstention reason must explain lack of grounded evidence", result.abstentionReason!!.contains("abstained"))
        assertTrue("Citations must be empty when abstained", result.citations.isEmpty())
    }
}
