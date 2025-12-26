package com.agropay.core.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Claims básicos del JWT token del backend
 */
@Serializable
data class JwtClaims(
    @SerialName("sub")
    val subject: String? = null, // username del backend
    
    @SerialName("username")
    val username: String? = null,
    
    @SerialName("exp")
    val expiration: Long? = null,
    
    @SerialName("iat")
    val issuedAt: Long? = null
)

/**
 * Parser simple de JWT para extraer username y grupos
 */
object JwtParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parsea un JWT token y extrae username y otros claims
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun parseJwt(token: String): Result<JwtClaims> {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                return Result.failure(Exception("Invalid JWT format"))
            }

            // Decodificar el payload (parte 2)
            val payload = parts[1]

            // Agregar padding si es necesario (JWT usa Base64 URL-safe sin padding)
            val paddedPayload = when (payload.length % 4) {
                2 -> "$payload=="
                3 -> "$payload="
                else -> payload
            }

            // Decodificar Base64 URL-safe
            val decodedBytes = Base64.UrlSafe.decode(paddedPayload)
            val jsonString = decodedBytes.decodeToString()

            // Parsear JSON
            val claims = json.decodeFromString<JwtClaims>(jsonString)

            Result.success(claims)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}