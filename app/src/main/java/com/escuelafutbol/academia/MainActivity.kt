package com.escuelafutbol.academia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.escuelafutbol.academia.ui.AcademiaRoot
import com.escuelafutbol.academia.ui.AcademiaViewModelFactory
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        procesarDeepLinkAuth(intent)
        enableEdgeToEdge()
        val app = application as AcademiaApplication
        val factory = AcademiaViewModelFactory(application, app.database)
        setContent {
            AcademiaRoot(factory = factory)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarDeepLinkAuth(intent)
    }

    /** Tras confirmar el correo, si Supabase redirige a academiafutbol://auth/... se muestra un mensaje en la app. */
    private fun procesarDeepLinkAuth(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "academiafutbol" || uri.host != "auth") return
        val client = (application as AcademiaApplication).supabaseClient ?: return
        client.handleDeeplinks(intent) {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.auth_email_confirmed_toast),
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
