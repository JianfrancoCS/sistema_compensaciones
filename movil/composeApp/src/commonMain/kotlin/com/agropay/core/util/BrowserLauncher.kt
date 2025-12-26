package com.agropay.core.util

/**
 * Abre una URL en el navegador del sistema
 * Usa Chrome Custom Tabs en Android y SFSafariViewController en iOS
 */
expect class BrowserLauncher {
    /**
     * Abre la URL de autorización en el navegador
     * @param url URL completa de autorización de Keycloak
     */
    fun openUrl(url: String)
}