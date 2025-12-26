package com.agropay.core.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIViewController

/**
 * Implementación iOS usando SFSafariViewController
 * Proporciona una experiencia segura y nativa
 */
actual class BrowserLauncher {

    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)

        if (nsUrl != null) {
            // Abrir con el navegador predeterminado del sistema
            UIApplication.sharedApplication.openURL(nsUrl)

            // TODO: Para una mejor UX, usar SFSafariViewController
            // Requiere pasar el UIViewController actual
        }
    }
}