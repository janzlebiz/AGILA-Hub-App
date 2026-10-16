# AGILA Hub Android

Official mobile member application and administrative management hub for **The Fraternal Order of Eagles (Philippine Eagles)** — *Bicol Camarines Norte Bantayog Region 1 (BCNBR-1) • Bantayog Elite Eagles Club (BEEC)*.

## Architectural Stack
- **Client App:** Native Android with Jetpack Compose & Kotlin
- **Backend:** Supabase Auth, PostgreSQL with Row-Level Security (RLS), and Private Storage Buckets
- **Admin Portal:** Vercel-hosted management dashboard for privileged officer workflows
- **Constitution RAG:** Grounded vector search and citation engine for TFOE-PE Magna Carta Q&A

## Repository Structure
```
├── app/                      # Android Application Module
│   ├── src/main/java/com/example/
│   │   ├── data/
│   │   │   ├── models/       # Typed Domain Models & Enums
│   │   │   └── repository/   # AgilaRepository & Constitution RAG Engine
│   │   ├── ui/
│   │   │   ├── components/   # Material 3 GlassCard, TopBar, BottomNav
│   │   │   ├── screens/      # Auth, Home, Bulletins, Attendance, Treasury, RAG, Profile, Admin
│   │   │   └── theme/        # AGILA Hub Color Tokens & AgilaHubTheme
│   │   └── MainActivity.kt
├── supabase/
│   └── migrations/           # Versioned PostgreSQL migrations & RLS baseline
└── .github/workflows/        # CI Quality Gate Workflow
```

## Developer Setup
1. **Prerequisites:** JDK 17+, Android Studio Jellyfish/Koala, or AI Studio environment.
2. **Build Application:**
   Run `compile_applet` or `gradle :app:assembleDebug`.
3. **Database Setup:**
   Apply migrations in `supabase/migrations/20261016000000_agila_hub_foundation.sql` to your Supabase project.

## Security & Verification
- All database policies enforce active member status and organization scope.
- Zero secrets in APK client bundles.
- Grounded RAG abstains when query evidence is out-of-scope.
