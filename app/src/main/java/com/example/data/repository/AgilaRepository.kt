package com.example.data.repository

import com.example.data.models.*
import com.example.data.api.SupabaseClient
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
    private val _isAuthenticated = MutableStateFlow(true)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentMember = MutableStateFlow(
        MemberProfile(
            id = "mem-101",
            userId = "usr-888",
            fullName = "Janzle B. Santos",
            nickname = "Janzle",
            email = "janzle.business@gmail.com",
            status = MemberStatus.ACTIVE,
            primaryClubName = "Bantayog Elite Eagles Club",
            primaryClubCode = "BEEC",
            regionName = "Bicol Camarines Norte Bantayog Region 1 (BCNBR-1)",
            eaglesIdFrontUrl = "https://example.com/eagles_id_front.jpg",
            eaglesIdBackUrl = "https://example.com/eagles_id_back.jpg"
        )
    )
    val currentMember: StateFlow<MemberProfile> = _currentMember.asStateFlow()

    private val _activeRoles = MutableStateFlow(
        listOf(
            RoleAssignment("role-1", RoleCode.MEMBER, "org-beec", "Bantayog Elite Eagles Club", true),
            RoleAssignment("role-2", RoleCode.CLUB_OFFICER, "org-beec", "Bantayog Elite Eagles Club", true),
            RoleAssignment("role-3", RoleCode.FINANCE_OFFICER, "org-beec", "Bantayog Elite Eagles Club", true)
        )
    )
    val activeRoles: StateFlow<List<RoleAssignment>> = _activeRoles.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow(
        listOf(
            Announcement(
                id = "ann-1",
                title = "Monthly General Assembly & Fellowship",
                body = "Brothers, our upcoming Monthly Regional Assembly will be hosted at the Bantayog Social Hall on Saturday, October 25, 2026. Uniform: Official Eagles Polo.",
                authorName = "Kuya Admin",
                orgScopeName = "BEEC / BCNBR-1",
                status = AnnouncementStatus.PUBLISHED,
                publishedAt = "2026-10-14 09:00"
            ),
            Announcement(
                id = "ann-2",
                title = "Community Medical Mission Drive",
                body = "In line with our Fraternal Service commitment, BEEC is organizing a health drive in Daet. Volunteers please register with the Secretariat.",
                authorName = "Kuya Secretariat",
                orgScopeName = "Bantayog Elite Eagles Club",
                status = AnnouncementStatus.PUBLISHED,
                publishedAt = "2026-10-10 14:30"
            )
        )
    )
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Events & Attendance
    private val _events = MutableStateFlow(
        listOf(
            EventItem("evt-1", "Regional Directorate Conference", "Q4 Strategic Planning", "2026-10-20", "BCNBR-1", 42),
            EventItem("evt-2", "BEEC Regular Meeting & Elections Prep", "Monthly Business Meeting", "2026-10-28", "BEEC", 28)
        )
    )
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private val _attendanceRecords = MutableStateFlow(
        listOf(
            AttendanceRecord("att-1", "evt-1", "Regional Directorate Conference", "mem-101", "Janzle B. Santos", AttendanceState.PRESENT, "Kuya Recorder", "2026-10-20 08:45"),
            AttendanceRecord("att-2", "evt-2", "BEEC Regular Meeting & Elections Prep", "mem-101", "Janzle B. Santos", AttendanceState.PRESENT, "Kuya Recorder", "2026-10-28 19:15")
        )
    )
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

    // Contributions / Finance
    private val _contributions = MutableStateFlow(
        listOf(
            ContributionRecord("con-1", "mem-101", "Janzle B. Santos", 1500.00, "PHP", "Annual Membership Dues 2026", "2026-01-15", ContributionStatus.VERIFIED, "Kuya Treasurer"),
            ContributionRecord("con-2", "mem-101", "Janzle B. Santos", 500.00, "PHP", "Medical Mission Relief Fund", "2026-06-10", ContributionStatus.VERIFIED, "Kuya Treasurer"),
            ContributionRecord("con-3", "mem-101", "Janzle B. Santos", 1000.00, "PHP", "Regional Convention Building Assessment", "2026-09-01", ContributionStatus.VERIFIED, "Kuya Treasurer")
        )
    )
    val contributions: StateFlow<List<ContributionRecord>> = _contributions.asStateFlow()

    // Documents
    private val _documents = MutableStateFlow(
        listOf(
            DocItem("doc-1", "TFOE-PE Magna Carta & Constitution", "2025 Revised Ed.", "Governing Rules"),
            DocItem("doc-2", "BCNBR-1 Regional By-Laws & Resolutions", "2024 Amendments", "Regional Rules"),
            DocItem("doc-3", "BEEC Standard Operating Guidelines", "v2.1", "Club Guidelines")
        )
    )
    val documents: StateFlow<List<DocItem>> = _documents.asStateFlow()

    // Invitations Admin
    private val _invitations = MutableStateFlow(
        listOf(
            Invitation("inv-1", "hash-abc12345", "applicant.eagle@gmail.com", "Bantayog Elite Eagles Club", RoleCode.MEMBER, false, "2026-11-01")
        )
    )
    val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

    // Audit Events
    private val _auditLogs = MutableStateFlow(
        listOf(
            AuditEvent("aud-1", "Janzle B. Santos", "SIGN_IN", "AuthSession", "BEEC", "2026-10-15 23:40")
        )
    )
    val auditLogs: StateFlow<List<AuditEvent>> = _auditLogs.asStateFlow()

    // Ingested Constitution Document Chunks with Vector Embeddings (768-d simulation)
    private data class IngestedChunk(
        val documentTitle: String,
        val articleRef: String,
        val sectionRef: String,
        val content: String,
        val excerpt: String,
        val embedding: FloatArray
    )

    private val ingestedChunks: List<IngestedChunk> by lazy {
        listOf(
            IngestedChunk(
                documentTitle = "TFOE-PE Magna Carta & Constitution",
                articleRef = "Article IV - Meetings & Assemblies",
                sectionRef = "Section 2 - Quorum Requirements",
                content = "A simple majority of active members in good standing shall constitute a quorum to transact official business during regular and special club meetings. Official club business cannot proceed without 50% + 1 quorum attendance.",
                excerpt = "A simple majority of active members in good standing shall constitute a quorum to transact official business during regular and special club meetings.",
                embedding = generateEmbedding("quorum requirement majority 50% + 1 attend meeting regular special club assembly transact business")
            ),
            IngestedChunk(
                documentTitle = "TFOE-PE Magna Carta & Constitution",
                articleRef = "Article VII - Financial Obligations",
                sectionRef = "Section 1 - Membership Dues",
                content = "All regular members shall pay annual dues. Non-payment after sixty (60) calendar days from due date shall result in administrative suspension of voting and participation rights.",
                excerpt = "All regular members shall pay annual dues. Non-payment after sixty (60) calendar days from due date shall result in administrative suspension.",
                embedding = generateEmbedding("annual dues fees payment 60-day grace period financial contribution obligations assessment suspension")
            ),
            IngestedChunk(
                documentTitle = "TFOE-PE Magna Carta & Constitution",
                articleRef = "Article I - Name, Seal and Objectives",
                sectionRef = "Section 3 - Fraternal Motto and Objectives",
                content = "The core motto of The Fraternal Order of Eagles (Philippine Eagles) is 'Service Through Strong Brotherhood' (Serbisyo sa Pamamagitan ng Matibay na Kapatiran). The Order is dedicated to humanitarian service and civic unity.",
                excerpt = "The motto of the Order is 'Service Through Strong Brotherhood' (Serbisyo sa Pamamagitan ng Matibay na Kapatiran).",
                embedding = generateEmbedding("motto purpose principles service through strong brotherhood objectives core seal fraternal unity")
            ),
            IngestedChunk(
                documentTitle = "TFOE-PE Magna Carta & Constitution",
                articleRef = "Article V - Executive Officers & Elections",
                sectionRef = "Section 4 - Election Procedure & Tenure",
                content = "Officers shall be elected by majority vote of members present during the annual meeting in November and shall assume office on January 1 for a term of one calendar year.",
                excerpt = "Officers shall be elected by majority vote of members present during the annual meeting in November and shall assume office on January 1.",
                embedding = generateEmbedding("president officers elected tenure elections november annual meeting secret ballot term calendar year")
            )
        )
    }

    // Helper: Cosine vector embedding generator (768 dimensions)
    private fun generateEmbedding(text: String): FloatArray {
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

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dot = 0.0f
        for (i in v1.indices) dot += v1[i] * v2[i]
        return dot
    }

    // Theme Switcher Action
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        logAudit("TOGGLE_DARK_MODE", "ThemePreference", if (_isDarkMode.value) "DARK" else "LIGHT")
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        logAudit("SET_DARK_MODE", "ThemePreference", if (enabled) "DARK" else "LIGHT")
    }

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
                _isAuthenticated.value = true
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

    fun signOut() {
        _isAuthenticated.value = false
        logAudit("SIGN_OUT", "AuthSession", _currentMember.value.id)
    }

    fun createAnnouncement(title: String, body: String, scope: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newAnn = Announcement(
            id = "ann-" + UUID.randomUUID().toString().take(6),
            title = title,
            body = body,
            authorName = "Kuya ${_currentMember.value.nickname}",
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

        // Duplicate prevention check
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
            recordedBy = "Kuya ${_currentMember.value.nickname}",
            recordedAt = dateStr
        )
        _attendanceRecords.value = listOf(newRecord) + _attendanceRecords.value

        // Update event attendee count
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
            recordedBy = "Kuya ${_currentMember.value.nickname}"
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

    suspend fun uploadEaglesId(isFront: Boolean, imageBytes: ByteArray): String? {
        val fileName = "${_currentMember.value.id}_${if (isFront) "front" else "back"}.jpg"
        val bucketName = "eagles-id-cards"
        val path = "private/$fileName"

        return try {
            val response: HttpResponse = httpClient.post("/storage/v1/object/$bucketName/$path") {
                contentType(ContentType.Image.JPEG)
                setBody(imageBytes)
                // In a real app, we'd include the user's JWT. 
                // For this remediation, we ensure the request structure is correct.
            }

            if (response.status.value in 200..299) {
                val objectPath = "$bucketName/$path"
                if (isFront) {
                    _currentMember.value = _currentMember.value.copy(eaglesIdFrontUrl = objectPath)
                } else {
                    _currentMember.value = _currentMember.value.copy(eaglesIdBackUrl = objectPath)
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
        _currentMember.value = _currentMember.value.copy(
            eaglesIdFrontUrl = frontUrl ?: _currentMember.value.eaglesIdFrontUrl,
            eaglesIdBackUrl = backUrl ?: _currentMember.value.eaglesIdBackUrl
        )
        logAudit("UPDATE_EAGLES_ID", "MemberProfile", _currentMember.value.id)
    }

    // Constitution RAG Query Processor via pgvector-style semantic retrieval
    suspend fun queryConstitution(prompt: String): ConstitutionQueryResult {
        val queryEmbedding = generateEmbedding(prompt)
        
        return try {
            val response: List<com.example.data.api.SearchConstitutionResponse> = httpClient.post("/rest/v1/rpc/search_constitution") {
                contentType(ContentType.Application.Json)
                setBody(com.example.data.api.SearchConstitutionRequest(
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
                    abstentionReason = "Query did not match verified passages in the ingested TFOE-PE Magna Carta or By-Laws. To prevent hallucinated or unverified fraternal guidance, the system has abstained from generating an ungrounded answer."
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

    private fun logAudit(action: String, resourceType: String, resourceId: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val log = AuditEvent(
            id = "aud-" + UUID.randomUUID().toString().take(6),
            actorName = "Kuya ${_currentMember.value.nickname}",
            action = action,
            resourceType = resourceType,
            orgScope = "BEEC",
            timestamp = dateStr
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
