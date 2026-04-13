package com.escuelafutbol.academia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.push.AcademiaFcmMessagingService
import com.escuelafutbol.academia.ui.AcademiaRoot
import com.escuelafutbol.academia.ui.AcademiaViewModelFactory
import com.escuelafutbol.academia.ui.auth.isPasswordRecoverySession
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        procesarNotificacionAbrirPadres(intent)
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
        procesarNotificacionAbrirPadres(intent)
        procesarDeepLinkAuth(intent)
    }

    private fun procesarNotificacionAbrirPadres(intent: Intent?) {
        if (intent?.getBooleanExtra(AcademiaFcmMessagingService.EXTRA_OPEN_PADRES, false) == true) {
            (application as AcademiaApplication).emitPendingNavigation(AcademiaFcmMessagingService.NAV_ROUTE_PADRES)
        }
    }

    /** Confirmación de correo o recuperación de contraseña vía academiafutbol://auth/... */
    private fun procesarDeepLinkAuth(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "academiafutbol" || uri.host != "auth") return
        val client = (application as AcademiaApplication).supabaseClient ?: return
        val recoveryInFragment = uri.fragment?.contains("type=recovery") == true
        client.handleDeeplinks(intent) { session ->
            val recovery = recoveryInFragment || isPasswordRecoverySession(session)
            Toast.makeText(
                this@MainActivity,
                getString(
                    if (recovery) R.string.auth_recovery_toast
                    else R.string.auth_email_confirmed_toast,
                ),
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
