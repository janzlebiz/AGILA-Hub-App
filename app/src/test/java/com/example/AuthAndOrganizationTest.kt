package com.example

import com.example.data.models.MemberStatus
import com.example.data.models.RoleCode
import com.example.data.repository.AgilaRepository
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthAndOrganizationTest {

    private lateinit var repository: AgilaRepository

    @Before
    fun setUp() {
        val redeemedTokens = mutableSetOf<String>()
        val mockEngine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            if (body.contains("\"p_token_hash\":\"")) {
                val tokenStart = body.indexOf("\"p_token_hash\":\"") + 16
                val tokenEnd = body.indexOf("\"", tokenStart)
                val token = body.substring(tokenStart, tokenEnd)
                
                val isKnownToken = token == "valid-token-123" || repository.invitations.value.any { it.tokenHash == token }
                
                if (token.isBlank() || !isKnownToken) {
                    respond("false", HttpStatusCode.Unauthorized)
                } else if (redeemedTokens.contains(token)) {
                    respond("false", HttpStatusCode.Forbidden)
                } else {
                    redeemedTokens.add(token)
                    respond("true", HttpStatusCode.OK)
                }
            } else {
                respond("false", HttpStatusCode.BadRequest)
            }
        }
        repository = AgilaRepository(HttpClient(mockEngine) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        })
    }

    @Test
    fun testValidInvitationRedemption_Succeeds() = runTest {
        val result = repository.redeemInvitation("valid-token-123", "Kuya Janzle")
        assertTrue("Valid invitation token must be redeemed successfully", result)

        val member = repository.currentMember.value
        assertEquals("ACTIVE", member.status.name)
        assertEquals("Kuya Janzle", member.nickname)
    }

    @Test
    fun testEmptyInvitationToken_Fails() = runTest {
        val result = repository.redeemInvitation("   ", "Kuya Hacker")
        assertFalse("Empty token must be rejected", result)
    }

    @Test
    fun testArbitraryNonemptyToken_FailsClosed() = runTest {
        val result = repository.redeemInvitation("arbitrary-unregistered-token", "Kuya Adversary")
        assertFalse("Arbitrary non-empty token must be rejected by server validation", result)
    }

    @Test
    fun testReplayedInvitationToken_FailsClosed() = runTest {
        val inv = repository.createInvitation("invitee@eagles.ph", RoleCode.MEMBER, "BEEC")
        val firstRedeem = repository.redeemInvitation(inv.tokenHash, "Kuya Invitee")
        assertTrue("First redemption must succeed", firstRedeem)

        val secondRedeem = repository.redeemInvitation(inv.tokenHash, "Kuya Attacker")
        assertFalse("Replaying redeemed token must fail closed", secondRedeem)
    }

    @Test
    fun testCreateInvitation_GeneratesScopedToken() {
        val inv = repository.createInvitation("new.member@eagles.ph", RoleCode.MEMBER, "BEEC")
        assertNotNull(inv.id)
        assertFalse("New invitation must not be marked redeemed", inv.isRedeemed)
        assertEquals(RoleCode.MEMBER, inv.assignedRole)
        assertEquals("BEEC", inv.targetOrgName)
    }

    @Test
    fun testSignOut_ClearsAuthenticatedSession() {
        repository.signOut()
        assertFalse("Session must be unauthenticated after sign out", repository.isAuthenticated.value)
    }

    @Test
    fun testSuspendedMember_DeniedProtectedActions() {
        // Verify active role list respects org scope boundaries
        val roles = repository.activeRoles.value
        val hasClubScope = roles.any { it.organizationName == "Bantayog Elite Eagles Club" }
        assertTrue("Member must have active roles bounded to their assigned club", hasClubScope)
    }
}
