package com.agropay.core.network

import com.agropay.data.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

/**
 * Plugin de Ktor que agrega el Bearer token a todas las peticiones
 */
class AuthInterceptor(
    private val authRepository: AuthRepository
) {

    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            // Obtener el token válido
            val token = runBlocking {
                authRepository.getValidAccessToken()
            }

            // Si hay token, agregarlo al header
            if (token != null) {
                request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            }

            // Continuar con la petición
            execute(request)
        }
    }
}

/**
 * Extension function para facilitar la instalación
 */
fun HttpClientConfig<*>.installAuthInterceptor(authRepository: AuthRepository) {
    install(HttpSend) {
        // Interceptar antes de enviar la petición
    }
}