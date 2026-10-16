# AGILA Hub — Architecture and Specification Deviation Register
**Audit Date:** 2026-10-16  
**Audited Target:** AGILA Hub Implementation vs. Approved Complete From-Scratch Pack Specification

---

## 1. Overview of Deviations

This register documents observed divergences between the approved architecture specification (Phases 00–07) and the actual repository implementation. Each entry identifies the approved requirement, the observed implementation, the security or operational impact, and the required corrective decision.

---

## 2. Itemized Deviation Register

### DEV-01: In-Memory Client Repository vs. Supabase Cloud Persistence
- **Approved Requirement:** The mobile client must authenticate via Supabase Auth and execute authoritative CRUD operations against a Supabase PostgreSQL backend with Row-Level Security.
- **Observed Implementation:** `AgilaRepository.kt` maintains state purely in local `MutableStateFlow` collections pre-populated with static sample records (`mem-101`, `ann-1`, etc.).
- **Impact:** Critical gap in authoritative data durability and multi-user synchronization. Data does not persist across app restarts.
- **Decision Required:** Authorize a follow-up implementation sprint to integrate the Supabase Kotlin SDK / Ktor REST client into `AgilaRepository.kt`.

### DEV-02: In-App Mobile Admin Portal vs. Vercel-Hosted Web Admin Dashboard
- **Approved Requirement:** Administrative officer workflows (regional/national invitations, role assignments, audit stream monitoring) must be hosted as an independent web application on Vercel.
- **Observed Implementation:** Administrative features are bundled directly into the Android mobile app via `AdminDashboardScreen.kt`. No web frontend code (`package.json`, Next.js app router, `vercel.json`) exists in the repository.
- **Impact:** Regional and national officers cannot manage governance from standard desktop browsers as specified.
- **Decision Required:** Commission and deploy a standalone Next.js web application under `/web` pointing to the Supabase backend.

### DEV-03: Keyword Substring RAG vs. pgvector Semantic Search Pipeline
- **Approved Requirement:** Constitution RAG must ingest document PDFs, chunk text, generate vector embeddings, store them in a PostgreSQL `pgvector` table, and perform cosine similarity search with verifiable citations.
- **Observed Implementation:** `AgilaRepository.queryConstitution` uses hardcoded keyword matching (`queryLower.contains("quor")`) returning static strings and canned citations.
- **Impact:** The system is an artificial simulation rather than a functional AI retrieval engine. It cannot ingest new constitutional documents dynamically.
- **Decision Required:** Implement real vector embedding generation via Gemini / Vertex AI and store embeddings in `document_chunks` with `vector(768)` dimensions.

### DEV-04: Incomplete Database RLS Policies
- **Approved Requirement:** All 12 tables and private storage buckets must have strict Row-Level Security policies enforcing least privilege and organization scope.
- **Observed Implementation:** RLS is enabled on 12 tables in migration `20261016000000_agila_hub_foundation.sql`, but active policies are defined for only `announcements`. The other 11 tables and `documents-restricted` storage bucket have zero policies.
- **Impact:** Direct database queries by authenticated users are blocked by default RLS behavior.
- **Decision Required:** Apply comprehensive RLS policies across all tables and storage buckets before production release.

### DEV-05: Unexecuted Disaster Recovery Drill
- **Approved Requirement:** An isolated disaster recovery backup and restore drill must be executed with documented RPO/RTO verification and sanitized integrity logs.
- **Observed Implementation:** `docs/BACKUP_AND_RECOVERY_DRILL.md` provides an operational procedure plan, but no live restore drill has been performed or logged.
- **Impact:** Operational readiness remains unverified.
- **Decision Required:** Provision an isolated Supabase project (`agila-hub-restore-drill`), execute a point-in-time restore, and log integrity metrics.
