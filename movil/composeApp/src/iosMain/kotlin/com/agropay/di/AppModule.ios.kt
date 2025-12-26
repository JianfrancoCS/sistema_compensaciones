package com.agropay.di

import com.agropay.core.util.BrowserLauncher

/**
 * Implementación iOS para crear BrowserLauncher
 */
actual fun createBrowserLauncher(): BrowserLauncher {
    return BrowserLauncher()
}