package com.agropay.di

import android.content.Context
import com.agropay.core.util.BrowserLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

/**
 * Implementación Android para crear BrowserLauncher
 */
actual fun createBrowserLauncher(): BrowserLauncher {
    val context = GlobalContext.get().get<Context>()
    return BrowserLauncher(context)
}