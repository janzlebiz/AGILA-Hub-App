# AGILA Hub — Remediation Plan
**Audit Date:** 2026-10-16  
**Target:** Corrective Actions to Reach Production Release Readiness

---

## 1. Remediation Roadmap by Priority & Dependency

### Priority 1: Security & Data Integrity (Blockers for Staging)
1. **Complete Database RLS Policies (SEC-CRIT-01):**
   - *Action:* Write granular `CREATE POLICY` statements for `organizations`, `members`, `organization_memberships`, `role_assignments`, `invitations`, `events`, `attendance_records`, `contributions`, `documents`, `document_chunks`, and `audit_events` enforcing active member status and club/regional scope.
   - *Verification:* Execute test suite against Supabase local emulator verifying unauthorized cross-tenant access is rejected.
2. **Secure Private Storage Buckets (SEC-HIGH-01):**
   - *Action:* Add storage RLS policies for `documents-restricted` ensuring only authorized officers can read/write.
   - *Verification:* Test authenticated officer vs. member download permissions on storage objects.

### Priority 2: Backend Cloud Integration (Blockers for Production)
3. **Wire Supabase Client in Android Repository (DEV-01, XREQ-DAT-01):**
   - *Action:* Integrate Supabase Kotlin SDK / Ktor client into `AgilaRepository.kt` to replace in-memory `MutableStateFlow` states with live PostgreSQL queries and mutations.
   - *Verification:* Run integration tests against a staging Supabase instance verifying data persistence across client re-launches.
4. **Implement Vercel Web Admin Portal (DEV-02, XREQ-WEB-01):**
   - *Action:* Scaffold a Next.js / React web admin portal under `/web` supporting officer authentication, invitation generation, and audit stream review; deploy to Vercel.
   - *Verification:* Smoke test Vercel deployment against staging Supabase backend.

### Priority 3: AI & Operations (Release Polish & Compliance)
5. **Implement pgvector Constitution RAG Pipeline (DEV-03, P05-RAG-02):**
   - *Action:* Add `extension vector` to Supabase migration, create embedding generation edge function, and replace keyword substring matcher in `AgilaRepository.queryConstitution` with semantic vector similarity search.
   - *Verification:* Execute evaluation test set verifying grounding precision and abstention on out-of-scope queries.
6. **Execute Disaster Recovery Drill (P07-OPS-02):**
   - *Action:* Provision an isolated test database, apply migrations, restore point-in-time snapshot, and verify data integrity metrics.
   - *Verification:* Attach dated drill log and integrity report to `docs/BACKUP_AND_RECOVERY_DRILL.md`.
7. **Configure Production Release Signing & R8 (P06-HRD-05):**
   - *Action:* Add release signing block and code shrinking configuration to `app/build.gradle.kts`.
   - *Verification:* Compile release APK and verify cryptographic signature.
