# AGILA Hub — Backup, Isolated Restore Drill & Operational Incident Response Plan

## 1. Covered Data & Assets
- **Database Schema & Data:** PostgreSQL database (tables: `organizations`, `members`, `organization_memberships`, `role_assignments`, `invitations`, `announcements`, `events`, `attendance_records`, `contributions`, `documents`, `document_chunks`, `audit_events`).
- **Private Storage Buckets:** `eagles-ids-private` (Eagles ID cards) and `documents-restricted` (Restricted document PDFs).
- **Security & RLS Policies:** RLS policy definitions, security definer functions, and grant permissions.

## 2. Backup Retention & Targets
- **Database Point-in-Time Recovery (PITR):** 7-day retention on Supabase staging/production.
- **Storage Object Mirroring:** Daily cross-region bucket replication.
- **Recovery Point Objective (RPO):** $\le 1$ hour.
- **Recovery Time Objective (RTO):** $\le 2$ hours.

## 3. Isolated Restore Drill Verification Protocol
1. Export point-in-time PostgreSQL dump and storage manifest from primary project.
2. Provision isolated disposable target database environment (`agila-hub-restore-drill`).
3. Apply schema baseline `supabase/migrations/20261016000000_agila_hub_foundation.sql` and storage migration `20261016000001_agila_hub_storage.sql`.
4. Restore data tables and execute integrity checks:
   - Verify active member record count and organization membership linkages.
   - Verify role assignments and RLS policy enforcement.
   - Verify fixed-precision treasury sum calculation.
   - Verify RAG document chunk citation mapping.
5. Record restore execution duration and log deviations.

## 4. Operational Incident Response & Key Rotation
- **Credential Leak Event:** Immediately revoke affected token/key, rotate `SUPABASE_ANON_KEY` or `SUPABASE_SERVICE_ROLE_KEY` in server secret manager, re-deploy Vercel server-side environment, and invalidate active sessions.
- **Unverified Content Removal:** Remove unapproved documents from `documents` and `document_chunks` table, re-index vector store, and log audit event.
