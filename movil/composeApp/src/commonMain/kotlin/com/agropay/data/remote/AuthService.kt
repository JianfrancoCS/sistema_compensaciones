package com.agropay.data.remote

import com.agropay.data.model.ApiResult
import com.agropay.domain.model.LoginRequest
import com.agropay.domain.model.LoginResponse
import com.agropay.domain.model.RefreshTokenRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Servicio para manejar autenticación JWT con el backend
 */
class AuthService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) {

    /**
     * Inicia sesión con username y password
     * POST /v1/auth/login
     */
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = httpClient.post("$baseUrl/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<LoginResponse> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    // Construir mensaje de error con detalles de campos si existen
                    val errorMessage = com.agropay.data.error.ErrorHandler.buildErrorMessage(response)
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Construir mensaje de error con detalles de campos si existen
                val errorMessage = com.agropay.data.error.ErrorHandler.buildErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresca el access token usando el refresh token
     * POST /v1/auth/refresh
     */
    suspend fun refreshToken(refreshToken: String): Result<LoginResponse> {
        return try {
            val response = httpClient.post("$baseUrl/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<LoginResponse> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    val errorMessage = com.agropay.data.error.ErrorHandler.buildErrorMessage(response)
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = com.agropay.data.error.ErrorHandler.buildErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra sesión
     * POST /v1/auth/logout
     * Requiere autenticación (Bearer token)
     */
    suspend fun logout(accessToken: String): Result<Unit> {
        return try {
            val response = httpClient.post("$baseUrl/v1/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                // No es crítico si falla el logout
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // No es crítico si falla el logout
            Result.success(Unit)
        }
    }
}
