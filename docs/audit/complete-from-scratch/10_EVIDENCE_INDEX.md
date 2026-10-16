# AGILA Hub — Evidence Index
**Audit Date:** 2026-10-16  
**Repository Scope:** `/` (Project Namespace: `com.example`, Application ID: `com.aistudio.agilahub.kxmpzq`)

---

## 1. Verified Repository Artifacts & Evidence Index

| Artifact Category | Exact File Path / Location | Verification Reference | Status / Notes |
| :--- | :--- | :--- | :--- |
| **Android Build Config** | `/app/build.gradle.kts` | `compile_applet` PASS | Gradle 9.3.1 / AGP 8.7.0, Java 8 compatibility, Compose BOM 2024.10.00 |
| **Root Gradle Config** | `/build.gradle.kts`, `/settings.gradle.kts` | Static Inspection | Version catalogs and plugins configured correctly |
| **Android Manifest** | `/app/src/main/AndroidManifest.xml` | Static Inspection | Custom adaptive icons (`ic_launcher`), device default theme |
| **UI Components** | `/app/src/main/java/com/example/ui/components/` | Code Inspection | `GlassCard.kt`, `TopBar.kt`, `BottomNav.kt`, `Badge.kt` |
| **Screens** | `/app/src/main/java/com/example/ui/screens/` | Code Inspection | Auth, Home, Announcements, Attendance, Finance, ConstitutionRAG, Profile, AdminDashboard |
| **Theme & Typography** | `/app/src/main/java/com/example/ui/theme/` | Code Inspection | `Color.kt` (Eagle Blue, Forest Green, Bumblebee Yellow), `Theme.kt`, `Typography.kt` |
| **Domain Models & Repo** | `/app/src/main/java/com/example/data/` | Code Inspection | `DomainModels.kt`, `AgilaRepository.kt` |
| **Database Migrations** | `/supabase/migrations/` | SQL Inspection | `20261016000000_agila_hub_foundation.sql`, `20261016000001_agila_hub_storage.sql` |
| **Unit & Robolectric Tests**| `/app/src/test/java/com/example/` | Gradle Test Execution | 5 test classes, 20 test methods passing (`AuthAndOrganizationTest`, `ConstitutionRagTest`, `CoreMemberFeaturesTest`, `ExampleRobolectricTest`, `SecurityHardeningAndNegativeTest`) |
| **CI Workflow** | `/.github/workflows/ci.yml` | Static Inspection | GitHub Actions workflow building and testing app on push/PR |
| **Environment Template** | `/.env.example` | Static Inspection | Public/server configuration template with zero embedded secrets |
| **Documentation** | `/docs/` | Static Inspection | `BACKUP_AND_RECOVERY_DRILL.md`, `TRACEABILITY_MATRIX.md` |
| **Platform Metadata** | `/metadata.json` | Static Inspection | App name "AGILA Hub", server-side Gemini capability retained |
| **Audit Deliverables** | `/docs/audit/complete-from-scratch/` | Audit Output Files | 01 through 10 complete audit report artifacts produced successfully |
