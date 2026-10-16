package com.example.data.models

import java.util.Date

enum class OrgType {
    NATIONAL, REGION, CLUB
}

enum class MemberStatus {
    PENDING, ACTIVE, SUSPENDED, REVOKED
}

enum class RoleCode(val displayName: String) {
    MEMBER("Member"),
    CLUB_OFFICER("Club Officer"),
    FINANCE_OFFICER("Finance Officer / Treasurer"),
    CLUB_ADMIN("Club Administrator"),
    REGIONAL_OFFICER("Regional Officer"),
    NATIONAL_OFFICER("National Officer")
}

enum class AnnouncementStatus {
    DRAFT, PUBLISHED, ARCHIVED
}

enum class AttendanceState {
    PRESENT, ABSENT, EXCUSED
}

enum class ContributionStatus {
    PENDING, VERIFIED, CORRECTED, CANCELLED
}

data class Organization(
    val id: String,
    val name: String,
    val code: String,
    val type: OrgType,
    val parentId: String? = null
)

data class MemberProfile(
    val id: String,
    val userId: String?,
    val fullName: String,
    val nickname: String,
    val email: String,
    val status: MemberStatus,
    val primaryClubName: String = "Bantayog Elite Eagles Club",
    val primaryClubCode: String = "BEEC",
    val regionName: String = "Bicol Camarines Norte Bantayog Region 1",
    val eaglesIdFrontUrl: String? = null,
    val eaglesIdBackUrl: String? = null
)

data class RoleAssignment(
    val id: String,
    val role: RoleCode,
    val organizationId: String,
    val organizationName: String,
    val isActive: Boolean = true
)

data class Invitation(
    val id: String,
    val tokenHash: String,
    val email: String,
    val targetOrgName: String,
    val assignedRole: RoleCode,
    val isRedeemed: Boolean,
    val expiresAt: String
)

data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val authorName: String,
    val orgScopeName: String,
    val status: AnnouncementStatus,
    val publishedAt: String
)

data class EventItem(
    val id: String,
    val title: String,
    val description: String,
    val eventDate: String,
    val orgScopeName: String,
    val totalAttendees: Int = 0
)

data class AttendanceRecord(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val memberId: String,
    val memberName: String,
    val status: AttendanceState,
    val recordedBy: String,
    val recordedAt: String
)

data class ContributionRecord(
    val id: String,
    val memberId: String,
    val memberName: String,
    val amount: Double,
    val currency: String = "PHP",
    val purpose: String,
    val effectiveDate: String,
    val status: ContributionStatus,
    val recordedBy: String
)

data class DocItem(
    val id: String,
    val title: String,
    val version: String,
    val category: String,
    val isRestricted: Boolean = false
)

data class ConstitutionQueryResult(
    val query: String,
    val answer: String,
    val citations: List<Citation>,
    val isAbstained: Boolean,
    val abstentionReason: String? = null
)

data class Citation(
    val documentTitle: String,
    val articleRef: String,
    val sectionRef: String,
    val excerpt: String
)

data class AuditEvent(
    val id: String,
    val actorName: String,
    val action: String,
    val resourceType: String,
    val orgScope: String,
    val timestamp: String
)
