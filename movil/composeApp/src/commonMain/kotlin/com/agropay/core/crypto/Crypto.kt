package com.agropay.core.crypto

/**
 * Calcula el hash SHA256 de los bytes de entrada
 */
expect fun sha256(input: ByteArray): ByteArray

/**
 * Codifica bytes a Base64 estándar
 */
expect fun ByteArray.toBase64(): String