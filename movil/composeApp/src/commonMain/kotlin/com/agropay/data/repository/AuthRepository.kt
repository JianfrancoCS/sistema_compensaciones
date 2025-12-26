package com.agropay.data.repository

import com.agropay.core.util.JwtParser
import com.agropay.data.remote.AuthService
import com.agropay.domain.model.AuthSession
import com.agropay.domain.model.LoginRequest
import com.agropay.domain.model.LoginResponse
import com.agropay.domain.model.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

/**
 * Repositorio para manejar autenticación y sesión del usuario
 */
class AuthRepository(
    private val authService: AuthService
) {
    // Session state
    private val _currentSession = MutableStateFlow<AuthSession?>(null)
    val currentSession: StateFlow<AuthSession?> = _currentSession.asStateFlow()

    /**
     * Verifica si hay una sesión activa y válida
     */
    fun isAuthenticated(): Boolean {
        val session = _currentSession.value ?: return false
        return !isTokenExpired(session)
    }

    /**
     * Verifica si el token ha expirado
     */
    private fun isTokenExpired(session: AuthSession): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        // Consideramos expirado si quedan menos de 60 segundos
        return now >= (session.expiresAt - 60_000)
    }

    /**
     * Inicia sesión con username (número de documento), password y perfil
     * Envía "MOBILE" como plataforma para filtrar el menú
     * El perfil es requerido para validar que el usuario tenga el perfil correcto
     */
    suspend fun login(username: String, password: String, profile: String? = null): Result<AuthSession> {
        return authService.login(LoginRequest(username, password, "MOBILE", profile))
            .mapCatching { loginResponse ->
                val session = createSessionFromLoginResponse(loginResponse)
                saveSession(session)
                session
            }
    }

    /**
     * Refresca el access token si es necesario
     */
    suspend fun refreshTokenIfNeeded(): Result<Unit> {
        val session = _currentSession.value ?: return Result.failure(Exception("No session"))

        if (!isTokenExpired(session)) {
            return Result.success(Unit)
        }

        // Verificar si el refresh token también expiró
        val now = Clock.System.now().toEpochMilliseconds()
        if (now >= session.refreshExpiresAt) {
            // Ambos tokens expirados, necesitamos login de nuevo
            _currentSession.value = null
            return Result.failure(Exception("Refresh token expired"))
        }

        return authService.refreshToken(session.refreshToken)
            .mapCatching { loginResponse ->
                val newSession = createSessionFromLoginResponse(loginResponse, session)
                saveSession(newSession)
            }
    }

    /**
     * Cierra la sesión del usuario
     */
    suspend fun logout() {
        val session = _currentSession.value

        // Intentar cerrar sesión en el backend
        session?.let { s ->
            authService.logout(s.accessToken)
                .onFailure { error ->
                    println("⚠️ Logout failed: ${error.message}")
                    // No es crítico si falla
                }
        }

        // Limpiar sesión local
        _currentSession.value = null
    }

    /**
     * Crea una sesión desde la respuesta de login
     */
    private fun createSessionFromLoginResponse(
        loginResponse: LoginResponse,
        existingSession: AuthSession? = null
    ): AuthSession {
        val now = Clock.System.now().toEpochMilliseconds()
        val expiresAt = now + (loginResponse.expiresIn * 1000L)
        val refreshExpiresAt = now + (loginResponse.refreshExpiresIn * 1000L)

        // Parsear JWT para extraer información del usuario
        val userInfo = JwtParser.parseJwt(loginResponse.token)
            .map { claims ->
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("🔐 JWT PARSEADO EXITOSAMENTE")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                val username = claims.username ?: claims.subject ?: loginResponse.username
                println("👤 Username: $username")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                UserInfo(
                    username = username,
                    userId = loginResponse.userId
                )
            }
            .onFailure { error ->
                println("❌ ERROR parseando JWT: ${error.message}")
            }
            .getOrNull()

        return AuthSession(
            accessToken = loginResponse.token,
            refreshToken = loginResponse.refreshToken,
            expiresAt = expiresAt,
            refreshExpiresAt = refreshExpiresAt,
            userId = loginResponse.userId,
            username = loginResponse.username,
            menu = loginResponse.menu,
            userInfo = userInfo ?: UserInfo(
                username = loginResponse.username,
                userId = loginResponse.userId
            )
        )
    }

    /**
     * Guarda la sesión
     * TODO: Implementar persistencia segura con DataStore/Keychain
     */
    private fun saveSession(session: AuthSession) {
        _currentSession.value = session
    }

    /**
     * Obtiene el access token actual si es válido
     */
    suspend fun getValidAccessToken(): String? {
        val session = _currentSession.value ?: return null

        if (isTokenExpired(session)) {
            refreshTokenIfNeeded().onFailure {
                return null
            }
        }

        return _currentSession.value?.accessToken
    }
}
