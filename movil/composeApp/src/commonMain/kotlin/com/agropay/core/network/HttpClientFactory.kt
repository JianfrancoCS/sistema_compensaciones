package com.agropay.core.network

import com.agropay.data.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * Factory para crear HttpClient configurado
 */
object HttpClientFactory {

    /**
     * Crea un HttpClient básico sin autenticación
     */
    fun create(): HttpClient {
        return HttpClient {
            // Configuración de timeout
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000 // 30 segundos para conectar
                requestTimeoutMillis = 30_000 // 30 segundos para la petición
                socketTimeoutMillis = 30_000 // 30 segundos para socket
            }

            // Content Negotiation para JSON
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }

            // Logging para debug
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
    }

    /**
     * Crea un HttpClient con interceptor de autenticación
     */
    fun createWithAuth(authRepository: AuthRepository): HttpClient {
        return HttpClient {
            // Configuración de timeout
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000 // 30 segundos para conectar
                requestTimeoutMillis = 30_000 // 30 segundos para la petición
                socketTimeoutMillis = 30_000 // 30 segundos para socket
            }

            // Content Negotiation para JSON
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }

            // Logging para debug
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Interceptor para agregar Bearer token en cada petición
            install(HttpSend) {
                maxSendCount = 3 // Máximo de reintentos
            }
            
            // Agregar token automáticamente en cada petición
            defaultRequest {
                val token = runBlocking {
                    authRepository.getValidAccessToken()
                }
                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    println("🔐 Token agregado a la petición: ${token.take(20)}...")
                } else {
                    println("⚠️ No se pudo obtener token para la petición")
                }
            }
        }
    }
}
