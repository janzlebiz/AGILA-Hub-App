package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SqlMigrationValidationTest {

    @Test
    fun testDatabaseMigrations_StructuralAndSecurityRulesValid() {
        val migrationsDir = File("supabase/migrations")
        val sqlFiles = (if (migrationsDir.exists()) migrationsDir.listFiles() else File("../supabase/migrations").listFiles())
            ?.filter { it.extension == "sql" } ?: emptyList()

        assertTrue("At least one SQL migration file must be present", sqlFiles.isNotEmpty())

        var totalTablesCreated = 0
        var totalRlsEnabled = 0
        var securityDefinerCount = 0
        var searchPathCount = 0

        for (file in sqlFiles) {
            val content = file.readText()

            // Count CREATE TABLE statements
            val tableMatches = Regex("CREATE TABLE IF NOT EXISTS ([a-zA-Z0-9_]+)", RegexOption.IGNORE_CASE).findAll(content)
            val tables = tableMatches.map { it.groupValues[1] }.toList()
            totalTablesCreated += tables.size

            // Count ENABLE ROW LEVEL SECURITY statements
            val rlsMatches = Regex("ALTER TABLE ([a-zA-Z0-9_]+) ENABLE ROW LEVEL SECURITY", RegexOption.IGNORE_CASE).findAll(content)
            totalRlsEnabled += rlsMatches.count()

            // Verify SECURITY DEFINER functions have SET search_path
            val secDefMatches = Regex("SECURITY DEFINER", RegexOption.IGNORE_CASE).findAll(content)
            securityDefinerCount += secDefMatches.count()

            val searchPathMatches = Regex("SET search_path = public, pg_temp", RegexOption.IGNORE_CASE).findAll(content)
            searchPathCount += searchPathMatches.count()

            // If migration creates functions, verify explicit REVOKE and GRANT
            if (content.contains("CREATE OR REPLACE FUNCTION")) {
                assertTrue("Migration ${file.name} with functions must revoke public execution", content.contains("REVOKE EXECUTE ON FUNCTION"))
                assertTrue("Migration ${file.name} with functions must grant authenticated execution", content.contains("GRANT EXECUTE ON FUNCTION"))
            }

            // Verify 768d vector configuration
            if (content.contains("document_chunks")) {
                assertTrue("Vector column must use 768 dimensions", content.contains("VECTOR(768)"))
            }
        }

        assertTrue("All created tables ($totalTablesCreated) must have Row Level Security enabled ($totalRlsEnabled)", totalRlsEnabled >= totalTablesCreated)
        assertEquals("All SECURITY DEFINER functions must set clean search_path", securityDefinerCount, searchPathCount)
    }

    @Test
    fun testRlsPolicies_EnforceOrganizationalScoping() {
        val foundationFile = File("supabase/migrations/20261016000000_agila_hub_foundation.sql")
        val file = if (foundationFile.exists()) foundationFile else File("../supabase/migrations/20261016000000_agila_hub_foundation.sql")
        assertTrue("Foundation migration file must exist", file.exists())

        val content = file.readText()

        assertTrue("RLS policies must use get_user_org_ids helper function", content.contains("get_user_org_ids()"))
        assertTrue("redeem_invitation RPC must lock row with FOR UPDATE", content.contains("FOR UPDATE"))
        assertTrue("redeem_invitation RPC must verify email match", content.contains("v_auth_email IS NULL OR v_inv.email != v_auth_email"))
        assertTrue("create_invitation_token RPC must restrict role grants", content.contains("Role escalation denied"))
    }
}
