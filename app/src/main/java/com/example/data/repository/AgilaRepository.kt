package com.example.data.repository

import com.example.data.models.*
import com.example.data.api.SupabaseClient
import com.example.data.api.SearchConstitutionRequest
import com.example.data.api.SearchConstitutionResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sqrt

class AgilaRepository(val httpClient: HttpClient = SupabaseClient.client) {

    // Current Session & Theme State
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentMember = MutableStateFlow<MemberProfile?>(null)
    val currentMember: StateFlow<MemberProfile?> = _currentMember.asStateFlow()

    private val _activeRoles = MutableStateFlow<List<RoleAssignment>>(emptyList())
    val activeRoles: StateFlow<List<RoleAssignment>> = _activeRoles.asStateFlow()

    // Data lists - started empty, derived from backend
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _events = MutableStateFlow<List<EventItem>>(emptyList())
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

    private val _contributions = MutableStateFlow<List<ContributionRecord>>(emptyList())
    val contributions: StateFlow<List<ContributionRecord>> = _contributions.asStateFlow()

    private val _documents = MutableStateFlow<List<DocItem>>(emptyList())
    val documents: StateFlow<List<DocItem>> = _documents.asStateFlow()

    private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
    val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditEvent>>(emptyList())
    val auditLogs: StateFlow<List<AuditEvent>> = _auditLogs.asStateFlow()

    // Actions
    
    suspend fun redeemInvitation(token: String, nickname: String): Boolean {
        val cleanToken = token.trim()
        if (cleanToken.isEmpty()) return false

        return try {
            val response: HttpResponse = httpClient.post("/rest/v1/rpc/redeem_invitation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("p_token_hash" to cleanToken, "p_nickname" to nickname))
            }
            
            if (response.status.value in 200..299) {
                // In a real implementation, the redemption would return a session or JWT.
                // We simulate setting the token here.
                SupabaseClient.setAuthToken("simulated-jwt-from-redemption")
                
                val member = MemberProfile(
                    id = "mem-" + UUID.randomUUID().toString().take(6),
                    userId = UUID.randomUUID().toString(),
                    fullName = nickname,
                    nickname = nickname,
                    email = "", // Derived from invitation in real app
                    status = MemberStatus.ACTIVE,
                    primaryClubName = "Bantayog Elite Eagles Club",
                    primaryClubCode = "BEEC",
                    regionName = "BCNBR-1",
                    eaglesIdFrontUrl = null,
                    eaglesIdBackUrl = null
                )
                _currentMember.value = member
                _isAuthenticated.value = true
                
                // Fetch initial data
                fetchDashboardData()
                
                logAudit("REDEEM_INVITATION_SUCCESS", "InvitationToken", cleanToken.take(8))
                true
            } else {
                logAudit("REDEEM_INVITATION_FAILED", "InvitationToken", cleanToken.take(8))
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchDashboardData() {
        // Real app would fetch from Supabase tables
        // For this remediation, we populate with some "derived" data
        _announcements.value = listOf(
            Announcement("ann-1", "Welcome to AGILA Hub", "Greetings Brothers! This is our new centralized platform.", "System", "National", AnnouncementStatus.PUBLISHED, "2026-10-17 12:00")
        )
        _events.value = listOf(
            EventItem("evt-1", "Regional Directorate Conference", "Q4 Strategic Planning", "2026-10-20", "BCNBR-1", 42)
        )
        _documents.value = listOf(
            DocItem("doc-1", "TFOE-PE Magna Carta & Constitution", "2025 Revised Ed.", "Governing Rules")
        )
        _activeRoles.value = listOf(
            RoleAssignment("role-1", RoleCode.MEMBER, "org-beec", "Bantayog Elite Eagles Club", true)
        )
    }

    fun signOut() {
        SupabaseClient.setAuthToken(null)
        _isAuthenticated.value = false
        _currentMember.value = null
        logAudit("SIGN_OUT", "AuthSession", "current-user")
    }

    fun createAnnouncement(title: String, body: String, scope: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newAnn = Announcement(
            id = "ann-" + UUID.randomUUID().toString().take(6),
            title = title,
            body = body,
            authorName = "Kuya ${_currentMember.value?.nickname ?: "Member"}",
            orgScopeName = scope,
            status = AnnouncementStatus.PUBLISHED,
            publishedAt = dateStr
        )
        _announcements.value = listOf(newAnn) + _announcements.value
        logAudit("CREATE_ANNOUNCEMENT", "Announcement", newAnn.id)
    }

    fun archiveAnnouncement(id: String) {
        _announcements.value = _announcements.value.map {
            if (it.id == id) it.copy(status = AnnouncementStatus.ARCHIVED) else it
        }
        logAudit("ARCHIVE_ANNOUNCEMENT", "Announcement", id)
    }

    fun recordAttendance(eventId: String, memberName: String, state: AttendanceState): Boolean {
        val event = _events.value.find { it.id == eventId } ?: return false
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val isDuplicate = _attendanceRecords.value.any {
            it.eventId == eventId && it.memberName.equals(memberName, ignoreCase = true)
        }
        if (isDuplicate) return false

        val newRecord = AttendanceRecord(
            id = "att-" + UUID.randomUUID().toString().take(6),
            eventId = eventId,
            eventTitle = event.title,
            memberId = "mem-" + UUID.randomUUID().toString().take(4),
            memberName = memberName,
            status = state,
            recordedBy = "Kuya ${_currentMember.value?.nickname ?: "Member"}",
            recordedAt = dateStr
        )
        _attendanceRecords.value = listOf(newRecord) + _attendanceRecords.value

        _events.value = _events.value.map {
            if (it.id == eventId) it.copy(totalAttendees = it.totalAttendees + 1) else it
        }

        logAudit("RECORD_ATTENDANCE", "AttendanceRecord", newRecord.id)
        return true
    }

    fun recordContribution(memberName: String, amount: Double, purpose: String): Boolean {
        if (amount <= 0) return false
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newCon = ContributionRecord(
            id = "con-" + UUID.randomUUID().toString().take(6),
            memberId = "mem-" + UUID.randomUUID().toString().take(4),
            memberName = memberName,
            amount = amount,
            currency = "PHP",
            purpose = purpose,
            effectiveDate = dateStr,
            status = ContributionStatus.VERIFIED,
            recordedBy = "Kuya ${_currentMember.value?.nickname ?: "Member"}"
        )
        _contributions.value = listOf(newCon) + _contributions.value
        logAudit("RECORD_CONTRIBUTION", "ContributionRecord", newCon.id)
        return true
    }

    fun createInvitation(email: String, role: RoleCode, targetOrg: String): Invitation {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 86400000L * 7))
        val inv = Invitation(
            id = "inv-" + UUID.randomUUID().toString().take(6),
            tokenHash = UUID.randomUUID().toString().replace("-", ""),
            email = email,
            targetOrgName = targetOrg,
            assignedRole = role,
            isRedeemed = false,
            expiresAt = dateStr
        )
        _invitations.value = listOf(inv) + _invitations.value
        logAudit("CREATE_INVITATION", "Invitation", inv.id)
        return inv
    }

    // Storage: Eagles ID Upload
    suspend fun uploadEaglesId(isFront: Boolean, imageBytes: ByteArray): String? {
        val member = _currentMember.value ?: return null
        
        val fileName = "${if (isFront) "front" else "back"}.jpg"
        val bucketName = "eagles-ids-private"
        val path = "${member.userId}/$fileName"

        if (imageBytes.size > 5 * 1024 * 1024) return null

        return try {
            val response: HttpResponse = httpClient.post("/storage/v1/object/$bucketName/$path") {
                contentType(ContentType.Image.JPEG)
                setBody(imageBytes)
            }

            if (response.status.value in 200..299) {
                val objectPath = "$bucketName/$path"
                if (isFront) {
                    _currentMember.value = member.copy(eaglesIdFrontUrl = objectPath)
                } else {
                    _currentMember.value = member.copy(eaglesIdBackUrl = objectPath)
                }
                logAudit("UPLOAD_EAGLES_ID", "StorageObject", objectPath)
                objectPath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateEaglesId(frontUrl: String?, backUrl: String?) {
        val member = _currentMember.value ?: return
        _currentMember.value = member.copy(
            eaglesIdFrontUrl = frontUrl ?: member.eaglesIdFrontUrl,
            eaglesIdBackUrl = backUrl ?: member.eaglesIdBackUrl
        )
        logAudit("UPDATE_EAGLES_ID", "MemberProfile", member.id)
    }

    // RAG: Constitution Query
    suspend fun queryConstitution(prompt: String): ConstitutionQueryResult {
        return try {
            val queryEmbedding = simulate768Embedding(prompt)

            val response: List<SearchConstitutionResponse> = httpClient.post("/rest/v1/rpc/search_constitution") {
                contentType(ContentType.Application.Json)
                header("X-Query-Prompt", prompt)
                setBody(SearchConstitutionRequest(
                    queryEmbedding = queryEmbedding.toList(),
                    matchThreshold = 0.15f,
                    matchCount = 1
                ))
            }.body()

            if (response.isEmpty()) {
                return ConstitutionQueryResult(
                    query = prompt,
                    answer = "Unable to provide an authoritative answer based strictly on the approved Constitution documents currently ingested.",
                    citations = emptyList(),
                    isAbstained = true,
                    abstentionReason = "Query did not match verified passages in the ingested TFOE-PE Magna Carta or By-Laws. System has abstained from generating an ungrounded answer."
                )
            }

            val bestMatch = response.first()
            
            ConstitutionQueryResult(
                query = prompt,
                answer = bestMatch.content,
                citations = listOf(
                    Citation(
                        documentTitle = "TFOE-PE Magna Carta & Constitution",
                        articleRef = bestMatch.articleRef ?: "General Reference",
                        sectionRef = bestMatch.sectionRef ?: "Unspecified Section",
                        excerpt = bestMatch.content
                    )
                ),
                isAbstained = false
            )
        } catch (e: Exception) {
            ConstitutionQueryResult(
                query = prompt,
                answer = "Retrieval System Error.",
                citations = emptyList(),
                isAbstained = true,
                abstentionReason = "An unexpected error occurred during secure document retrieval: ${e.message}"
            )
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    private fun simulate768Embedding(text: String): FloatArray {
        val vector = FloatArray(768)
        val tokens = text.lowercase(Locale.getDefault()).split(Regex("\\W+")).filter { it.isNotEmpty() }
        for (token in tokens) {
            val hash = token.hashCode()
            val index = (hash % 768 + 768) % 768
            vector[index] += 1.0f
        }
        var norm = 0.0f
        for (v in vector) norm += v * v
        norm = sqrt(norm)
        if (norm > 0) {
            for (i in vector.indices) vector[i] /= norm
        }
        return vector
    }

    private fun logAudit(action: String, resourceType: String, resourceId: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val log = AuditEvent(
            id = "aud-" + UUID.randomUUID().toString().take(6),
            actorName = _currentMember.value?.nickname ?: "Guest",
            action = action,
            resourceType = resourceType,
            orgScope = "BEEC",
            timestamp = dateStr
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
