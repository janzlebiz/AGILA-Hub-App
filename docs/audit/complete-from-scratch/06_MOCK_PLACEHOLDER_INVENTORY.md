# AGILA Hub — Mock and Placeholder Inventory
**Audit Date:** 2026-10-16  
**Audited Target:** AGILA Hub Repository Codebase

---

## 1. Summary of Mocks and Placeholders

The implementation exhibits substantial reliance on mock in-memory data structures, static placeholder URLs, and simulated backend workflows. This inventory catalogues all observed mock assets, their repository locations, and their remediation paths.

---

## 2. Itemized Mock Inventory

### MOCK-01: In-Memory Seed Member Profile
- **Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:21-35`
- **Observed Content:** Hardcoded `MemberProfile` instance (`id = "mem-101"`, `userId = "usr-888"`, `fullName = "Janzle B. Santos"`, `nickname = "Janzle"`, `email = "janzle.business@gmail.com"`).
- **Classification:** In-Memory Sample State.
- **Remediation:** Replace with authenticated user profile fetch from Supabase `members` table via `auth.uid()`.

### MOCK-02: Pre-Seeded Active Role Assignments
- **Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:38-45`
- **Observed Content:** Static list granting `MEMBER`, `CLUB_OFFICER`, and `FINANCE_OFFICER` roles simultaneously to the local session.
- **Classification:** In-Memory Sample State.
- **Remediation:** Query `role_assignments` table filtered by `member_id` and `is_active = true`.

### MOCK-03: Pre-Seeded Operational Records (Bulletins, Attendance, Contributions)
- **Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:48-97`
- **Observed Content:** Static lists of announcements (`ann-1`, `ann-2`), events (`evt-1`, `evt-2`), attendance records (`att-1`, `att-2`), and treasury contributions (`con-1`, `con-2`, `con-3`).
- **Classification:** In-Memory Seed State.
- **Remediation:** Wire to Supabase PostgreSQL tables `announcements`, `events`, `attendance_records`, and `contributions`.

### MOCK-04: Mock Eagles ID Photo URLs
- **Location:** `app/src/main/java/com/example/ui/screens/ProfileScreen.kt:256`
- **Observed Content:** `onUpdateEaglesId("https://example.com/eagles_id_front_new.jpg", "https://example.com/eagles_id_back_new.jpg")`.
- **Classification:** Mock External Asset URL.
- **Remediation:** Connect to Android system PhotoPicker and upload raw bytes to `eagles-ids-private` bucket via Supabase Storage signed URLs.

### MOCK-05: Keyword Substring Matcher Simulating Vector RAG
- **Location:** `app/src/main/java/com/example/data/repository/AgilaRepository.kt:252-326`
- **Observed Content:** Hardcoded branching logic:
  - `queryLower.contains("quor")` $\rightarrow$ returns hardcoded Article IV quorum excerpt.
  - `queryLower.contains("dues")` $\rightarrow$ returns hardcoded Article VII dues excerpt.
  - `queryLower.contains("motto")` $\rightarrow$ returns hardcoded Article I motto excerpt.
  - `queryLower.contains("president")` $\rightarrow$ returns hardcoded Article V election excerpt.
  - `else` $\rightarrow$ returns hardcoded abstention string.
- **Classification:** Simulated Artificial Intelligence / Vector Pipeline.
- **Remediation:** Implement genuine document chunking, generate embeddings with Gemini / Vertex AI, and execute vector similarity search in PostgreSQL using the `pgvector` extension.

### MOCK-06: In-App Simulated Admin Portal
- **Location:** `app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt`
- **Observed Content:** Administrative token creation and audit log review rendered directly inside the Android mobile app rather than in an independent Vercel web application.
- **Classification:** Client-Side Architectural Substitute for Web Portal.
- **Remediation:** Build standalone Next.js / React administrative web portal deployed to Vercel with dedicated officer authentication.

### MOCK-07: Unpopulated Environment Secrets
- **Location:** `/.env.example`
- **Observed Content:** `YOUR_STAGING_PROJECT.supabase.co`, `YOUR_PUBLIC_PUBLISHABLE_ANON_KEY`, `SERVER_ONLY_PLACEHOLDER`.
- **Classification:** Template Placeholders.
- **Remediation:** Populate real staging environment variables within the secure cloud runtime / secrets manager.
