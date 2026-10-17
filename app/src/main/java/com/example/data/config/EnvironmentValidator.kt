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
        val url = BuildConfig.SUPABASE_URL
        val anonKey = BuildConfig.SUPABASE_ANON_KEY

        // Fail-closed enforcement: In release/production, required credentials cannot be placeholders
        if (!isDebug && (url.contains("placeholder") || anonKey.contains("placeholder"))) {
            throw IllegalStateException("Fail-closed: Production build requires explicit SUPABASE_URL and SUPABASE_ANON_KEY.")
        }

        val config = EnvironmentConfig(
            supabaseUrl = url,
            supabaseAnonKey = anonKey,
            isStagingEnvironment = url.contains("staging"),
            isDebuggable = isDebug
        )

        Log.i(TAG, "Environment validated: URL=${config.supabaseUrl}, Staging=${config.isStagingEnvironment}, Debuggable=${config.isDebuggable}")
        return config
    }
}
