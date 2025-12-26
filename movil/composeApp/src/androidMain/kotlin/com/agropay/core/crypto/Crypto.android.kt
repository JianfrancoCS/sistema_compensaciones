package com.agropay.core.crypto

import android.util.Base64
import java.security.MessageDigest

/**
 * Implementación Android de SHA256 usando MessageDigest
 */
actual fun sha256(input: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input)
}

/**
 * Implementación Android de Base64 usando android.util.Base64
 */
actual fun ByteArray.toBase64(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}