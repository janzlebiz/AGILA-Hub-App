# AGILA Hub — Security Findings Register
**Audit Date:** 2026-10-16  
**Audited Target:** AGILA Hub Android, Database Migrations, and Storage Security Baseline

---

## 1. Summary of Security Findings by Severity

| Severity Level | Open Count | Status Summary |
| :--- | :---: | :--- |
| **CRITICAL** | 2 | Missing RLS policies on 11 tables; In-memory client persistence lacking backend validation |
| **HIGH** | 4 | Missing RLS on restricted storage bucket; Absent vector security guardrails; Keyword RAG spoofability; Lack of server-side PhotoPicker upload validation |
| **MEDIUM** | 3 | Missing server-side rate-limiting / abuse controls; Absence of signed release APK pipeline; Audit logs stored in client memory |
| **LOW** | 2 | Deprecated Gradle / Kotlin compiler DSL warnings; Hardcoded default values in client forms |
| **INFORMATIONAL** | 1 | Zero hardcoded production secrets verified in repository |

---

## 2. Detailed Findings Register

### SEC-CRIT-01: Incomplete Row-Level Security (RLS) Policy Coverage on 11 Core Tables
- **Severity:** **CRITICAL**
- **Affected Trust Boundary:** PostgreSQL Database / Supabase Data API
- **Evidence Location:** `supabase/migrations/20261016000000_agila_hub_foundation.sql:160-191`
- **Description:**  
  While the migration activates RLS on 12 tables (`ALTER TABLE ... ENABLE ROW LEVEL SECURITY;`), it explicitly defines a policy for **only one table** (`announcements` on lines 185–190). The other 11 tables (`organizations`, `members`, `organization_memberships`, `role_assignments`, `invitations`, `events`, `attendance_records`, `contributions`, `documents`, `document_chunks`, `audit_events`) have no `CREATE POLICY` statements.
- **Impact:**  
  Under PostgreSQL default behavior, any table with RLS enabled and zero policies blocks all access to standard authenticated users (`authenticated` role), rendering the backend unusable for legitimate client operations. Conversely, if developers attempt to bypass this by utilizing the `service_role` key in client software, all RLS protections are completely circumvented.
- **Remediation:**  
  Create explicit, granular `SELECT`, `INSERT`, `UPDATE`, and `DELETE` policies on all 11 tables enforcing organization scoping and role validation (e.g., verifying `check_is_active_member()` and club hierarchy).

---

### SEC-CRIT-02: Client-Side State Model Treats In-Memory Data as Authoritative
- **Severity:** **CRITICAL**
- **Affected Trust Boundary:** Android Client Application $\leftrightarrow$ Authoritative Backend
- **Evidence Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:12-124`
- **Description:**  
  The Android app operates against local in-memory `MutableStateFlow` lists pre-seeded with sample records (`mem-101`, `att-1`, `con-1`, `inv-1`). It does not transmit attendance check-ins, financial receipts, invitation redemption, or role updates to a verified server endpoint.
- **Impact:**  
  Any client process termination or reboot discards all newly created fraternal transactions. Because no network layer communicates with the server, server-side business rules, cryptographic token validation, and multi-user data isolation cannot be enforced.
- **Remediation:**  
  Implement a production repository layer using Supabase SDK / Ktor client that executes authenticated REST / GraphQL / RPC calls against the Supabase backend.

---

### SEC-HIGH-01: Missing RLS Security Policies for `documents-restricted` Storage Bucket
- **Severity:** **HIGH**
- **Affected Trust Boundary:** Supabase Object Storage (`storage.objects`)
- **Evidence Location:** `supabase/migrations/20261016000001_agila_hub_storage.sql:1-46`
- **Description:**  
  The migration registers two private buckets: `eagles-ids-private` and `documents-restricted`. However, policies are defined **only** for `eagles-ids-private` (lines 13–45). `documents-restricted` has zero storage RLS policies for read or write.
- **Impact:**  
  Authorized officers cannot download or review restricted organizational documents through signed URLs, or the bucket risks misconfiguration if blanket permissions are applied.
- **Remediation:**  
  Add `SELECT` and `INSERT` policies on `storage.objects` for `bucket_id = 'documents-restricted'` verifying that the requesting user possesses `CLUB_OFFICER`, `REGIONAL_OFFICER`, or `NATIONAL_OFFICER` roles.

---

### SEC-HIGH-02: Absence of Cryptographic Vector Embeddings & Hallucination Guardrails
- **Severity:** **HIGH**
- **Affected Trust Boundary:** Constitution RAG Engine / Knowledge Base Retrieval
- **Evidence Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:252-326`
- **Description:**  
  The Constitution Q&A feature performs substring matching (`queryLower.contains("quor")`) and returns hardcoded text. It does not use pgvector embeddings, cosine similarity thresholds, or an LLM guardrail pipeline.
- **Impact:**  
  The system cannot generalize across varied fraternal inquiries, cannot ingest new constitutional amendments dynamically, and provides an illusion of AI retrieval without genuine grounding or verifiable provenance.
- **Remediation:**  
  Deploy a real pgvector database table with `vector(768)` embeddings, generate embeddings via Gemini / Vertex AI, and perform semantic similarity searches with an explicit similarity distance cutoff.

---

### SEC-HIGH-03: Eagles ID Upload Relies on Hardcoded Mock URLs Without File Validation
- **Severity:** **HIGH**
- **Affected Trust Boundary:** Android Media Picker / Object Storage Upload
- **Evidence Location:** `app/src/main/java/com/example/ui/screens/ProfileScreen.kt:256`
- **Description:**  
  When the user taps "Select & Upload Eagles ID Card", the UI passes static mock URLs (`https://example.com/eagles_id_front_new.jpg`) to the repository instead of launching the zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- **Impact:**  
  Members cannot upload genuine physical credentials, and no client-side MIME-type or file-size validation is performed before initiating storage operations.
- **Remediation:**  
  Integrate `rememberLauncherForActivityResult(PickVisualMedia())` and transmit image bytes to Supabase Storage using authenticated short-lived signed upload URLs.

---

### SEC-MED-01: Audit Event Logs Stored in Client Memory Rather Than Immutable Server Store
- **Severity:** **MEDIUM**
- **Affected Trust Boundary:** Governance / Compliance Audit Trail
- **Evidence Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:328-339`
- **Description:**  
  Audit events generated by administrative actions (invitations, role toggles, attendance) are logged solely to client-side memory (`_auditLogs.value = listOf(log) + _auditLogs.value`).
- **Impact:**  
  Audit logs can be discarded or manipulated on the client side, preventing an authoritative forensic trail for regional and national officer compliance.
- **Remediation:**  
  Insert audit events directly into the PostgreSQL `audit_events` table using a secure Postgres function (`SECURITY DEFINER`).

---

### SEC-MED-02: Android Release Artifact Pipeline and Keystore Configuration Missing
- **Severity:** **MEDIUM**
- **Affected Trust Boundary:** Android Build & Distribution
- **Evidence Location:** `app/build.gradle.kts`, `.build-outputs/app-debug.apk`
- **Description:**  
  The project currently compiles only `debug` builds. No release build variant, ProGuard/R8 minification rules, or production release signing configuration is present in `app/build.gradle.kts`.
- **Impact:**  
  Debug APKs include debuggable flags, unminified symbols, and cannot be published to Google Play or distributed safely to members.
- **Remediation:**  
  Configure a `release` signing config and build variant with R8 code shrinking enabled in `app/build.gradle.kts`.
