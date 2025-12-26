package com.agropay.core.util

import platform.Foundation.NSUUID

/**
 * Implementación iOS de generación de UUID usando NSUUID
 */
actual fun randomUUID(): String {
    return NSUUID().UUIDString()
}

/**
 * Implementación iOS de validación de UUID usando NSUUID
 */
actual fun isValidUUID(uuid: String): Boolean {
    return try {
        NSUUID(uuid)
        true
    } catch (e: Exception) {
        false
    }
}

