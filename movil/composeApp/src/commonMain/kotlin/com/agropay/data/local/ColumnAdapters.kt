package com.agropay.data.local

import app.cash.sqldelight.ColumnAdapter

/**
 * Adapter para convertir Boolean a Long (SQLite)
 * - true = 1
 * - false = 0
 */
object BooleanColumnAdapter : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean {
        return databaseValue == 1L
    }

    override fun encode(value: Boolean): Long {
        return if (value) 1L else 0L
    }
}
