package com.agropay.data.error

import com.agropay.data.model.ApiResult
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Maneja errores de las respuestas del backend
 * Extrae mensajes de error y errores por campo cuando están disponibles
 */
object ErrorHandler {

    /**
     * Extrae el mensaje de error de una respuesta HTTP
     * Intenta parsear ApiResult para obtener el mensaje del backend
     */
    suspend fun extractErrorMessage(response: HttpResponse): String {
        return try {
            // Intentar parsear como ApiResult
            val apiResult: ApiResult<*> = response.body()
            apiResult.message ?: "Error desconocido"
        } catch (e: Exception) {
            // Si no se puede parsear, usar el código HTTP
            when (response.status) {
                HttpStatusCode.BadRequest -> "Solicitud inválida"
                HttpStatusCode.Unauthorized -> "No autorizado. Por favor, inicie sesión nuevamente"
                HttpStatusCode.Forbidden -> "Acceso denegado"
                HttpStatusCode.NotFound -> "Recurso no encontrado"
                HttpStatusCode.InternalServerError -> "Error interno del servidor"
                else -> "Error: ${response.status.value}"
            }
        }
    }

    /**
     * Extrae errores por campo de una respuesta HTTP
     * Útil para mostrar errores de validación específicos por campo
     */
    suspend fun extractFieldErrors(response: HttpResponse): Map<String, String> {
        return try {
            val apiResult: ApiResult<*> = response.body()
            apiResult.errors ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Construye un mensaje de error completo incluyendo errores por campo
     */
    suspend fun buildErrorMessage(response: HttpResponse): String {
        val mainMessage = extractErrorMessage(response)
        val fieldErrors = extractFieldErrors(response)

        if (fieldErrors.isEmpty()) {
            return mainMessage
        }

        // Construir mensaje con errores por campo
        val fieldMessages = fieldErrors.entries.joinToString("\n") { (field, error) ->
            "- ${formatFieldName(field)}: $error"
        }

        return "$mainMessage\n\n$fieldMessages"
    }

    /**
     * Formatea el nombre del campo para mostrar al usuario
     */
    private fun formatFieldName(field: String): String {
        return field
            .replace(Regex("([a-z])([A-Z])"), "$1 $2") // camelCase -> camel Case
            .replace("_", " ") // snake_case -> snake case
            .split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
}

