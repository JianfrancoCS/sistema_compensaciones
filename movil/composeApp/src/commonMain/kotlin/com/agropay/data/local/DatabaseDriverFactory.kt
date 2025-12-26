package com.agropay.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory para crear SqlDriver específico de cada plataforma
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
