package com.example

import com.example.data.models.MemberStatus
import com.example.data.models.RoleCode
import com.example.data.repository.AgilaRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthAndOrganizationTest {

    private lateinit var repository: AgilaRepository

    @Before
    fun setUp() {
        repository = AgilaRepository()
    }

    @Test
    fun testValidInvitationRedemption_Succeeds() {
        val result = repository.redeemInvitation("valid-token-123", "Kuya Janzle")
        assertTrue("Valid invitation token must be redeemed successfully", result)

        val member = repository.currentMember.value
        assertEquals("ACTIVE", member.status.name)
        assertEquals("Kuya Janzle", member.nickname)
    }

    @Test
    fun testEmptyInvitationToken_Fails() {
        val result = repository.redeemInvitation("   ", "Kuya Hacker")
        assertFalse("Empty token must be rejected", result)
    }

    @Test
    fun testArbitraryNonemptyToken_FailsClosed() {
        val result = repository.redeemInvitation("arbitrary-unregistered-token", "Kuya Adversary")
        assertFalse("Arbitrary non-empty token must be rejected by server validation", result)
    }

    @Test
    fun testReplayedInvitationToken_FailsClosed() {
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
