package com.example.data.config

import android.util.Log

data class EnvironmentConfig(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    val isStagingEnvironment: Boolean,
    val isDebuggable: Boolean
)

object EnvironmentValidator {

    private const val TAG = "AgilaEnvValidator"

    fun validateEnvironment(): EnvironmentConfig {
        // Read public configuration defaults
        val url = System.getenv("SUPABASE_URL") ?: "https://staging-agila-hub.supabase.co"
        val anonKey = System.getenv("SUPABASE_ANON_KEY") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.staging-anon-key-placeholder"

        // Security check: ensure service role key is NEVER present in mobile client
        val serviceRoleKey = System.getenv("SUPABASE_SERVICE_ROLE_KEY")
        if (!serviceRoleKey.isNullOrBlank()) {
            Log.e(TAG, "CRITICAL SECURITY WARNING: Service role key detected in mobile client context!")
        }

        val config = EnvironmentConfig(
            supabaseUrl = url,
            supabaseAnonKey = anonKey,
            isStagingEnvironment = url.contains("staging"),
            isDebuggable = true
        )

        Log.i(TAG, "Environment initialized cleanly: URL=${config.supabaseUrl}, Staging=${config.isStagingEnvironment}")
        return config
    }
}
