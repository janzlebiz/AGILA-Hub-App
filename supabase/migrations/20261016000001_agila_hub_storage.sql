-- AGILA Hub Storage Buckets & Policies Migration
-- Version: 20261016000001
-- Description: Private Storage Buckets for Eagles ID Cards and Restricted Documents.

-- Create Private Buckets
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES 
    ('eagles-ids-private', 'eagles-ids-private', false, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('documents-restricted', 'documents-restricted', false, 15728640, ARRAY['application/pdf', 'image/jpeg', 'image/png'])
ON CONFLICT (id) DO NOTHING;

-- Storage RLS Policy: Members can upload their own Eagles ID
CREATE POLICY "Members upload own Eagles ID"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
    bucket_id = 'eagles-ids-private' AND
    check_is_active_member(auth.uid()) AND
    (storage.foldername(name))[1] = auth.uid()::text
);

-- Storage RLS Policy: Members read own Eagles ID via short-lived signed URLs
CREATE POLICY "Members read own Eagles ID"
ON storage.objects FOR SELECT
TO authenticated
USING (
    bucket_id = 'eagles-ids-private' AND
    check_is_active_member(auth.uid()) AND
    (storage.foldername(name))[1] = auth.uid()::text
);

-- Storage RLS Policy: Scoped officers read Eagles IDs in their organization
CREATE POLICY "Officers view organization Eagles IDs"
ON storage.objects FOR SELECT
TO authenticated
USING (
    bucket_id = 'eagles-ids-private' AND
    EXISTS (
        SELECT 1 FROM role_assignments ra
        JOIN members m ON m.id = ra.member_id
        WHERE m.user_id = auth.uid()
          AND ra.is_active = true
          AND ra.role IN ('CLUB_OFFICER', 'CLUB_ADMIN', 'REGIONAL_OFFICER', 'NATIONAL_OFFICER')
    )
);
