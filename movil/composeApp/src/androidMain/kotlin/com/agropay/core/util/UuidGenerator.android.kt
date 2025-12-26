package com.agropay.core.util

import java.util.UUID

/**
 * Implementación Android de generación de UUID usando java.util.UUID
 */
actual fun randomUUID(): String {
    return UUID.randomUUID().toString()
}

/**
 * Implementación Android de validación de UUID usando java.util.UUID
 */
actual fun isValidUUID(uuid: String): Boolean {
    return try {
        UUID.fromString(uuid)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

