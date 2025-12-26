package com.agropay

import androidx.compose.ui.window.ComposeUIViewController
import com.agropay.data.local.Database
import com.agropay.data.local.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    // Inicializar base de datos SQLDelight
    Database.initialize(DatabaseDriverFactory())

    App()
}