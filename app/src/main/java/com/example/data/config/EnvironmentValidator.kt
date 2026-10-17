package com.example.data.config

import android.util.Log
import com.example.BuildConfig

data class EnvironmentConfig(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    val isStagingEnvironment: Boolean,
    val isDebuggable: Boolean
)

object EnvironmentValidator {

    private const val TAG = "AgilaEnvValidator"

    fun validateEnvironment(): EnvironmentConfig {
        val isDebug = BuildConfig.DEBUG
        val url = System.getenv("SUPABASE_URL")
        val anonKey = System.getenv("SUPABASE_ANON_KEY")

        // Fail-closed enforcement: In release/production, required credentials cannot be omitted
        if (!isDebug && (url.isNullOrBlank() || anonKey.isNullOrBlank())) {
            throw IllegalStateException("Fail-closed: Production build requires explicit SUPABASE_URL and SUPABASE_ANON_KEY.")
        }

        // Security check: ensure service role key is NEVER present in mobile client context
        val serviceRoleKey = System.getenv("SUPABASE_SERVICE_ROLE_KEY")
        if (!serviceRoleKey.isNullOrBlank()) {
            Log.e(TAG, "CRITICAL SECURITY WARNING: Service role key detected in mobile client context!")
            throw SecurityException("Security violation: Service role key must never be exposed to mobile clients.")
        }

        val finalUrl = url ?: if (isDebug) "https://staging-agila-hub.supabase.co" else ""
        val finalKey = anonKey ?: if (isDebug) "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.staging-anon-key-placeholder" else ""

        val config = EnvironmentConfig(
            supabaseUrl = finalUrl,
            supabaseAnonKey = finalKey,
            isStagingEnvironment = finalUrl.contains("staging"),
            isDebuggable = isDebug
        )

        Log.i(TAG, "Environment validated: URL=${config.supabaseUrl}, Staging=${config.isStagingEnvironment}, Debuggable=${config.isDebuggable}")
        return config
    }
}
