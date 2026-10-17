-- AGILA Hub Foundation Database Migration
-- Version: 20261016000000
-- Description: Core schema, organizations, memberships, roles, invitations, announcements, events, attendance, contributions, documents, RAG chunks, audit logs, RPCs, and RLS policies.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Enums
DO $$ BEGIN
    CREATE TYPE org_type AS ENUM ('NATIONAL', 'REGION', 'CLUB');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE member_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'REVOKED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE role_code AS ENUM ('MEMBER', 'CLUB_OFFICER', 'FINANCE_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE announcement_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE attendance_state AS ENUM ('PRESENT', 'ABSENT', 'EXCUSED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE contribution_status AS ENUM ('PENDING', 'VERIFIED', 'CORRECTED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE doc_visibility AS ENUM ('PUBLIC_MEMBER', 'OFFICER_ONLY', 'RESTRICTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

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

-- Documents & RAG Chunks (768 dimensions for embedding)
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
    embedding VECTOR(768),
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
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp;

-- Helper Function: Get all organization IDs a user belongs to
CREATE OR REPLACE FUNCTION get_user_org_ids()
RETURNS SETOF UUID AS $$
BEGIN
    RETURN QUERY
    SELECT organization_id
    FROM organization_memberships om
    JOIN members m ON m.id = om.member_id
    WHERE m.user_id = auth.uid() AND m.status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp;

-- Comprehensive RLS Policies for all tables with Organizational Scoping
DROP POLICY IF EXISTS "Public read organizations" ON organizations;
CREATE POLICY "Public read organizations" ON organizations FOR SELECT USING (true);

DROP POLICY IF EXISTS "Members view members in same org" ON members;
CREATE POLICY "Members view members in same org" ON members FOR SELECT USING (
    id IN (
        SELECT member_id FROM organization_memberships 
        WHERE organization_id IN (SELECT get_user_org_ids())
    ) OR user_id = auth.uid()
);

DROP POLICY IF EXISTS "Members view own membership" ON organization_memberships;
CREATE POLICY "Members view own membership" ON organization_memberships FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid()) OR
    organization_id IN (SELECT get_user_org_ids())
);

DROP POLICY IF EXISTS "Members view own roles" ON role_assignments;
CREATE POLICY "Members view own roles" ON role_assignments FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid()) OR
    organization_id IN (SELECT get_user_org_ids())
);

DROP POLICY IF EXISTS "Officers view invitations for their org" ON invitations;
CREATE POLICY "Officers view invitations for their org" ON invitations FOR SELECT USING (
    target_organization_id IN (SELECT get_user_org_ids()) AND
    EXISTS (
        SELECT 1 FROM role_assignments ra
        JOIN members m ON m.id = ra.member_id
        WHERE m.user_id = auth.uid() AND ra.is_active = true
        AND ra.role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
    )
);

DROP POLICY IF EXISTS "Members view announcements for their org" ON announcements;
CREATE POLICY "Members view announcements for their org" ON announcements FOR SELECT USING (
    organization_id IN (SELECT get_user_org_ids()) AND
    status = 'PUBLISHED'
);

DROP POLICY IF EXISTS "Members view events for their org" ON events;
CREATE POLICY "Members view events for their org" ON events FOR SELECT USING (
    organization_id IN (SELECT get_user_org_ids())
);

DROP POLICY IF EXISTS "Members view own attendance" ON attendance_records;
CREATE POLICY "Members view own attendance" ON attendance_records FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid()) OR
    EXISTS (
        SELECT 1 FROM events e
        WHERE e.id = attendance_records.event_id
          AND e.organization_id IN (SELECT get_user_org_ids())
    )
);

DROP POLICY IF EXISTS "Members view own contributions" ON contributions;
CREATE POLICY "Members view own contributions" ON contributions FOR SELECT USING (
    member_id IN (SELECT id FROM members WHERE user_id = auth.uid()) OR
    (
        organization_id IN (SELECT get_user_org_ids()) AND
        EXISTS (
            SELECT 1 FROM role_assignments ra
            JOIN members m ON m.id = ra.member_id
            WHERE m.user_id = auth.uid() AND ra.is_active = true
            AND ra.role IN ('FINANCE_OFFICER', 'CLUB_ADMIN')
        )
    )
);

DROP POLICY IF EXISTS "Members view documents for their org" ON documents;
CREATE POLICY "Members view documents for their org" ON documents FOR SELECT USING (
    organization_id IN (SELECT get_user_org_ids()) AND
    (
        visibility = 'PUBLIC_MEMBER' OR
        (visibility = 'OFFICER_ONLY' AND EXISTS (
            SELECT 1 FROM role_assignments ra
            JOIN members m ON m.id = ra.member_id
            WHERE m.user_id = auth.uid() AND ra.is_active = true
            AND ra.role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
        ))
    )
);

DROP POLICY IF EXISTS "Members view document chunks for their org" ON document_chunks;
CREATE POLICY "Members view document chunks for their org" ON document_chunks FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM documents d
        WHERE d.id = document_chunks.document_id
          AND (d.organization_id IS NULL OR d.organization_id IN (SELECT get_user_org_ids()))
          AND (
              d.visibility = 'PUBLIC_MEMBER' OR
              (d.visibility = 'OFFICER_ONLY' AND EXISTS (
                  SELECT 1 FROM role_assignments ra
                  JOIN members m ON m.id = ra.member_id
                  WHERE m.user_id = auth.uid() AND ra.is_active = true
                  AND ra.role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
              ))
          )
    )
);

DROP POLICY IF EXISTS "Officers view audit events for their org" ON audit_events;
CREATE POLICY "Officers view audit events for their org" ON audit_events FOR SELECT USING (
    organization_id IN (SELECT get_user_org_ids()) AND
    EXISTS (
        SELECT 1 FROM role_assignments ra
        JOIN members m ON m.id = ra.member_id
        WHERE m.user_id = auth.uid() AND ra.is_active = true
        AND ra.role IN ('CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
    )
);

-- Trusted Server RPC: Create Invitation Token
CREATE OR REPLACE FUNCTION create_invitation_token(p_email TEXT, p_target_org_id UUID, p_role role_code)
RETURNS TABLE (invitation_id UUID, raw_token TEXT, expires_at TIMESTAMPTZ) AS $$
DECLARE
    v_actor_member_id UUID;
    v_acting_role role_code;
    v_raw_token TEXT;
    v_token_hash TEXT;
    v_expires_at TIMESTAMPTZ;
    v_new_id UUID;
BEGIN
    -- Verify actor is authenticated
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Authentication required to create invitations.';
    END IF;

    -- Verify actor is an active member
    SELECT id INTO v_actor_member_id 
    FROM members 
    WHERE user_id = auth.uid() AND status = 'ACTIVE';

    IF v_actor_member_id IS NULL THEN
        RAISE EXCEPTION 'Only active members can generate invitations.';
    END IF;

    -- Verify actor holds officer authority in target org
    SELECT role INTO v_acting_role
    FROM role_assignments
    WHERE member_id = v_actor_member_id
      AND organization_id = p_target_org_id
      AND is_active = true
      AND role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER');

    IF v_acting_role IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: Acting user does not hold officer authority for the target organization.';
    END IF;

    -- Restrict role grants to officer authority
    IF p_role IN ('REGIONAL_OFFICER', 'NATIONAL_OFFICER') AND v_acting_role NOT IN ('REGIONAL_OFFICER', 'NATIONAL_OFFICER') THEN
        RAISE EXCEPTION 'Role escalation denied: Cannot grant higher authority role than acting officer holds.';
    END IF;

    -- Generate cryptographically secure token & hash
    v_raw_token := encode(gen_random_bytes(32), 'hex');
    v_token_hash := encode(digest(v_raw_token, 'sha256'), 'hex');
    v_expires_at := NOW() + INTERVAL '7 days';

    INSERT INTO invitations (
        token_hash, email, target_organization_id, assigned_role, expires_at, created_by
    ) VALUES (
        v_token_hash, p_email, p_target_org_id, p_role, v_expires_at, v_actor_member_id
    ) RETURNING id INTO v_new_id;

    -- Record server-side audit event
    INSERT INTO audit_events (actor_id, action, resource_type, resource_id, organization_id, details)
    VALUES (v_actor_member_id, 'CREATE_INVITATION', 'INVITATION', v_new_id, p_target_org_id, jsonb_build_object('recipient', p_email, 'role', p_role));

    RETURN QUERY SELECT v_new_id, v_raw_token, v_expires_at;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp;

-- Secure Server-Validated Invitation Redemption RPC Function
CREATE OR REPLACE FUNCTION redeem_invitation(p_token_hash TEXT, p_nickname TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    v_inv invitations%ROWTYPE;
    v_member_id UUID;
    v_auth_email TEXT;
BEGIN
    -- Verify non-null authenticated identity
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Authentication required to redeem invitation.';
    END IF;

    -- Get authenticated user email from JWT
    v_auth_email := auth.jwt() ->> 'email';

    -- Validate token exists, unredeemed, unrevoked, and not expired (using FOR UPDATE to prevent race condition/concurrent redemption)
    SELECT * INTO v_inv
    FROM invitations
    WHERE token_hash = p_token_hash
      AND is_redeemed = false
      AND is_revoked = false
      AND expires_at > NOW()
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Invalid, expired, revoked, or already redeemed invitation token.';
    END IF;

    -- Verify authenticated user email matches invitation email
    IF v_auth_email IS NULL OR v_inv.email != v_auth_email THEN
        RAISE EXCEPTION 'Authenticated user email does not match invitation recipient email.';
    END IF;

    -- Mark invitation redeemed atomically
    UPDATE invitations
    SET is_redeemed = true
    WHERE id = v_inv.id;

    -- Create or update active member profile for current user (prevent account reassignment)
    INSERT INTO members (user_id, full_name, nickname, email, status)
    VALUES (auth.uid(), v_inv.email, p_nickname, v_inv.email, 'ACTIVE')
    ON CONFLICT (email) DO UPDATE
    SET status = 'ACTIVE', nickname = p_nickname, user_id = auth.uid()
    WHERE members.user_id IS NULL OR members.user_id = auth.uid()
    RETURNING id INTO v_member_id;

    IF v_member_id IS NULL THEN
        RAISE EXCEPTION 'Account reassignment error: Member email is bound to another user account.';
    END IF;

    -- Assign organization membership and role securely server-side
    INSERT INTO organization_memberships (member_id, organization_id, is_primary)
    VALUES (v_member_id, v_inv.target_organization_id, true)
    ON CONFLICT (member_id, organization_id) DO NOTHING;

    INSERT INTO role_assignments (member_id, role, organization_id, is_active)
    VALUES (v_member_id, v_inv.assigned_role, v_inv.target_organization_id, true);

    -- Audit log
    INSERT INTO audit_events (actor_id, action, resource_type, resource_id, organization_id, details)
    VALUES (v_member_id, 'REDEEM_INVITATION', 'INVITATION', v_inv.id, v_inv.target_organization_id, jsonb_build_object('email', v_inv.email, 'role', v_inv.assigned_role));

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp;

-- Constitution pgvector Semantic Search RPC Function (768 dimensions)
CREATE OR REPLACE FUNCTION search_constitution(query_embedding VECTOR(768), match_threshold FLOAT, match_count INT)
RETURNS TABLE (
    chunk_id UUID,
    document_id UUID,
    content TEXT,
    article_ref TEXT,
    section_ref TEXT,
    similarity FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        dc.id,
        dc.document_id,
        dc.content,
        dc.article_ref,
        dc.section_ref,
        1 - (dc.embedding <=> query_embedding) AS similarity
    FROM document_chunks dc
    JOIN documents d ON d.id = dc.document_id
    WHERE check_is_active_member(auth.uid())
      AND (
          d.visibility = 'PUBLIC_MEMBER' OR
          (d.visibility = 'OFFICER_ONLY' AND EXISTS (
              SELECT 1 FROM role_assignments ra
              JOIN members m ON m.id = ra.member_id
              WHERE m.user_id = auth.uid() AND ra.is_active = true
              AND ra.role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
          ))
      )
      AND (d.organization_id IS NULL OR d.organization_id IN (SELECT get_user_org_ids()))
      AND 1 - (dc.embedding <=> query_embedding) > match_threshold
    ORDER BY dc.embedding <=> query_embedding ASC
    LIMIT match_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp;

-- Explicit Function Execution Permissions Grant & Revoke
REVOKE EXECUTE ON FUNCTION get_user_org_ids FROM PUBLIC;
GRANT EXECUTE ON FUNCTION get_user_org_ids TO authenticated;

REVOKE EXECUTE ON FUNCTION check_is_active_member FROM PUBLIC;
GRANT EXECUTE ON FUNCTION check_is_active_member TO authenticated;

REVOKE EXECUTE ON FUNCTION create_invitation_token FROM PUBLIC;
GRANT EXECUTE ON FUNCTION create_invitation_token TO authenticated;

REVOKE EXECUTE ON FUNCTION redeem_invitation FROM PUBLIC;
GRANT EXECUTE ON FUNCTION redeem_invitation TO authenticated;

REVOKE EXECUTE ON FUNCTION search_constitution FROM PUBLIC;
GRANT EXECUTE ON FUNCTION search_constitution TO authenticated;
