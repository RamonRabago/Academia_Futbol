package com.escuelafutbol.academia

import android.app.Application
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration.Companion.seconds

class AcademiaApplication : Application() {

    val database: AcademiaDatabase by lazy { AcademiaDatabase.create(this) }

    /** null si faltan `SUPABASE_URL` y `SUPABASE_ANON_KEY` en local.properties al compilar. */
    val supabaseClient: SupabaseClient? by lazy {
        val url = BuildConfig.SUPABASE_URL.trim()
        val key = BuildConfig.SUPABASE_ANON_KEY.trim()
        if (url.isEmpty() || key.isEmpty()) null
        else {
            createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key,
            ) {
                // Red móvil / transición desde selector de archivos: 10s por defecto suele ser justo.
                requestTimeout = 45.seconds
                install(Auth) {
                    host = "auth"
                    scheme = "academiafutbol"
                }
                install(Postgrest)
                install(Storage)
            }
        }
    }
}
