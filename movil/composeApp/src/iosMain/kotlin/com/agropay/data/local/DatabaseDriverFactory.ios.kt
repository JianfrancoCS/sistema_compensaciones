package com.agropay.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.agropay.db.AgropayDatabase

/**
 * Implementación iOS del DatabaseDriverFactory
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = AgropayDatabase.Schema,
            name = "agropay.db"
        )
    }
}
