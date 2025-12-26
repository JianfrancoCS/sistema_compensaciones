package com.agropay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.agropay.core.util.AuthCallbackHandler
import com.agropay.data.local.Database
import com.agropay.data.local.DatabaseDriverFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializar base de datos SQLDelight
        Database.initialize(DatabaseDriverFactory(applicationContext))

        // Manejar deep link si existe
        handleDeepLink(intent)

        setContent {
            App(koinDeclaration = {
                androidContext(applicationContext)
            })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data

        println("🔗 Deep link received: $data")

        if (data == null) {
            println("❌ No deep link data")
            return
        }

        println("🔍 Scheme: ${data.scheme}, Host: ${data.host}, Port: ${data.port}, Path: ${data.path}")

        // Verificar si es el callback de OAuth
        val isOAuthCallback = when {
            // Desarrollo: http://localhost:8081/oauth/callback
            data.scheme == "http" &&
            data.host == "localhost" &&
            data.port == 8081 &&
            data.path?.startsWith("/oauth/callback") == true -> true

            // Producción: agropay://callback
            data.scheme == "agropay" && data.host == "callback" -> true

            else -> false
        }

        println("✅ Is OAuth callback: $isOAuthCallback")

        if (isOAuthCallback) {
            val code = data.getQueryParameter("code")
            println("🎫 Authorization code: $code")

            if (code != null) {
                // Enviar el authorization code al handler
                CoroutineScope(Dispatchers.Main).launch {
                    println("📤 Sending code to AuthCallbackHandler")
                    AuthCallbackHandler.handleAuthCode(code)
                }
            } else {
                println("❌ No code parameter in callback")
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}