package com.agropay.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Implementación Android usando Chrome Custom Tabs
 * Proporciona una experiencia más integrada que abrir Chrome directamente
 */
actual class BrowserLauncher(private val context: Context) {

    actual fun openUrl(url: String) {
        try {
            // Intentar con Chrome Custom Tabs primero (mejor UX)
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback: abrir con navegador predeterminado
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}