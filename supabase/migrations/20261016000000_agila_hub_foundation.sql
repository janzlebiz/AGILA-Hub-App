-- AGILA Hub Foundation Database Migration
-- Version: 20261016000000
-- Description: Core schema, organizations, memberships, roles, invitations, announcements, events, attendance, contributions, documents, RAG chunks, audit logs, and RLS policies.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enums
CREATE TYPE org_type AS ENUM ('NATIONAL', 'REGION', 'CLUB');
CREATE TYPE member_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'REVOKED');
CREATE TYPE role_code AS ENUM ('MEMBER', 'CLUB_OFFICER', 'FINANCE_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER');
CREATE TYPE announcement_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE attendance_state AS ENUM ('PRESENT', 'ABSENT', 'EXCUSED');
CREATE TYPE contribution_status AS ENUM ('PENDING', 'VERIFIED', 'CORRECTED', 'CANCELLED');
CREATE TYPE doc_visibility AS ENUM ('PUBLIC_MEMBER', 'OFFICER_ONLY', 'RESTRICTED');

-- Organizations
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    code TEXT UNIQUE NOT NULL,
    type org_type NOT NULL,
    parent_id UUID REFERENCES organizations(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Members
CREATE TABLE IF NOT EXISTS members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE,
    full_name TEXT NOT NULL,
    nickname TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    status member_status NOT NULL DEFAULT 'PENDING',
    eagles_id_front_url TEXT,
    eagles_id_back_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Organization Memberships
CREATE TABLE IF NOT EXISTS organization_memberships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(member_id, organization_id)
);

-- Role Assignments
CREATE TABLE IF NOT EXISTS role_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    role role_code NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT true,
    granted_by UUID REFERENCES members(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Invitations
CREATE TABLE IF NOT EXISTS invitations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token_hash TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    target_organization_id UUID NOT NULL REFERENCES organizations(id),
    assigned_role role_code NOT NULL DEFAULT 'MEMBER',
    intended_nickname TEXT,
    is_redeemed BOOLEAN DEFAULT false,
    is_revoked BOOLEAN DEFAULT false,
    expires_at TIMESTAMPTZ NOT NULL,
    created_by UUID REFERENCES members(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Announcements
CREATE TABLE IF NOT EXISTS announcements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    author_id UUID NOT NULL REFERENCES members(id),
    status announcement_status NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Events
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    event_date TIMESTAMPTZ NOT NULL,
    location TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Attendance Records
CREATE TABLE IF NOT EXISTS attendance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    status attendance_state NOT NULL DEFAULT 'PRESENT',
    recorded_by UUID NOT NULL REFERENCES members(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(event_id, member_id)
);

-- Contributions
CREATE TABLE IF NOT EXISTS contributions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES members(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'PHP',
    purpose TEXT NOT NULL,
    effective_date DATE NOT NULL,
    status contribution_status NOT NULL DEFAULT 'VERIFIED',
    recorded_by UUID NOT NULL REFERENCES members(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Documents & RAG Chunks
CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    version TEXT NOT NULL,
    source_checksum TEXT NOT NULL,
    visibility doc_visibility NOT NULL DEFAULT 'PUBLIC_MEMBER',
    organization_id UUID REFERENCES organizations(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS document_chunks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    article_ref TEXT,
    section_ref TEXT,
    page_number INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Audit Log
CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_id UUID REFERENCES members(id),
    action TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    resource_id UUID,
    organization_id UUID REFERENCES organizations(id),
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS on all tables
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE members ENABLE ROW LEVEL SECURITY;
ALTER TABLE organization_memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE role_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE invitations ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE events ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE contributions ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE document_chunks ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_events ENABLE ROW LEVEL SECURITY;

-- Helper Functions
CREATE OR REPLACE FUNCTION check_is_active_member(user_uuid UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM members
        WHERE user_id = user_uuid AND status = 'ACTIVE'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Sample RLS Policy: Active members can view published announcements in their organization scope
CREATE POLICY "Active members view announcements"
ON announcements FOR SELECT
USING (
    check_is_active_member(auth.uid()) AND
    status = 'PUBLISHED'
);

-- Comprehensive RLS Policies for all tables
CREATE POLICY "Public read organizations" ON organizations FOR SELECT USING (true);
CREATE POLICY "Active members view members" ON members FOR SELECT USING (check_is_active_member(auth.uid()));
CREATE POLICY "Members view own membership" ON organization_memberships FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid())
);
CREATE POLICY "Members view own roles" ON role_assignments FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid())
);
CREATE POLICY "Officers view invitations" ON invitations FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM role_assignments ra
        JOIN members m ON m.id = ra.member_id
        WHERE m.user_id = auth.uid() AND ra.is_active = true
        AND ra.role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
    )
);
CREATE POLICY "Active members view events" ON events FOR SELECT USING (check_is_active_member(auth.uid()));
CREATE POLICY "Active members view attendance" ON attendance_records FOR SELECT USING (check_is_active_member(auth.uid()));
CREATE POLICY "Members view own contributions" ON contributions FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid()) OR
    EXISTS (
        SELECT 1 FROM role_assignments ra
        JOIN members m ON m.id = ra.member_id
        WHERE m.user_id = auth.uid() AND ra.is_active = true
        AND ra.role IN ('FINANCE_OFFICER', 'CLUB_ADMIN')
    )
);
CREATE POLICY "Active members view documents" ON documents FOR SELECT USING (
    check_is_active_member(auth.uid()) AND visibility != 'RESTRICTED'
);
CREATE POLICY "Active members view document chunks" ON document_chunks FOR SELECT USING (
    check_is_active_member(auth.uid())
);
CREATE POLICY "Officers view audit events" ON audit_events FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM role_assignments ra
        JOIN members m ON m.id = ra.member_id
        WHERE m.user_id = auth.uid() AND ra.is_active = true
        AND ra.role IN ('CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
    )
);

