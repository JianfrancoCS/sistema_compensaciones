package com.agropay.core.util

/**
 * Genera un UUID aleatorio como String
 * Implementación multiplataforma usando expect/actual
 */
expect fun randomUUID(): String

/**
 * Valida si un String es un UUID válido
 * Implementación multiplataforma usando expect/actual
 */
expect fun isValidUUID(uuid: String): Boolean

