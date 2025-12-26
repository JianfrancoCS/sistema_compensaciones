package com.agropay.domain.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request de login con username (número de documento) y password
 */
@Serializable
data class LoginRequest(
    val username: String, // Número de documento del usuario
    val password: String,
    @EncodeDefault
    val platform: String = "MOBILE", // "WEB", "MOBILE", "DESKTOP" - Hardcodeado a "MOBILE" en la app móvil
    val profile: String? = null // Perfil del usuario (ej: "Supervisor", "Acopiador") - Requerido para MOBILE
)

/**
 * Request para refrescar token
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken")
    val refreshToken: String
)

/**
 * Item de navegación del menú (desde backend)
 */
@Serializable
data class NavigationItemDTO(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("displayName")
    val label: String,
    
    @SerialName("icon")
    val icon: String? = null, // Icono de PrimeNG (ej: "pi pi-home")
    
    @SerialName("iconUrl")
    val iconUrl: String? = null, // URL de imagen del icono
    
    @SerialName("route")
    val path: String? = null, // Ruta de navegación (ej: "/system/employees")
    
    @SerialName("children")
    val children: List<NavigationItemDTO>? = null
)

/**
 * Respuesta de login del backend
 */
@Serializable
data class LoginResponse(
    val token: String,
    @SerialName("refreshToken")
    val refreshToken: String,
    @SerialName("tokenType")
    val tokenType: String = "Bearer",
    @SerialName("userId")
    val userId: String,
    val username: String,
    @SerialName("expiresIn")
    val expiresIn: Long, // en segundos
    @SerialName("refreshExpiresIn")
    val refreshExpiresIn: Long, // en segundos
    val menu: List<NavigationItemDTO>? = null
)

/**
 * Sesión del usuario con tokens
 */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long, // timestamp en milisegundos
    val refreshExpiresAt: Long, // timestamp en milisegundos
    val userId: String,
    val username: String,
    val menu: List<NavigationItemDTO>? = null,
    val userInfo: UserInfo? = null
)

/**
 * Información básica del usuario desde el JWT token o del endpoint /me
 */
data class UserInfo(
    val username: String,
    val userId: String? = null,
    val groups: List<String> = emptyList()
)

/**
 * Estados de autenticación
 */
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val session: AuthSession) : AuthState()
    data class Error(val message: String) : AuthState()
}