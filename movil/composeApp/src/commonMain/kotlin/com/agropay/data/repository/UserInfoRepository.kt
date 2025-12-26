package com.agropay.data.repository

import com.agropay.data.model.UserInfoResponse
import com.agropay.data.remote.SyncApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio para obtener información del usuario logueado
 * Se usa para obtener el DNI del supervisor al crear tareos
 */
class UserInfoRepository(
    private val syncApiService: SyncApiService,
    private val authRepository: AuthRepository
) {
    private val _userInfo = MutableStateFlow<UserInfoResponse?>(null)
    val userInfo: StateFlow<UserInfoResponse?> = _userInfo.asStateFlow()

    /**
     * Obtiene la información del usuario logueado desde el backend
     * Se usa para obtener el DNI del supervisor
     */
    suspend fun loadUserInfo(): Result<UserInfoResponse> {
        return try {
            val result = syncApiService.getMyInfo()
            result.onSuccess { info ->
                _userInfo.value = info
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el DNI del usuario logueado
     * Primero intenta desde userInfo cargado, luego desde la sesión (username puede ser el DNI)
     */
    fun getCurrentUserDocumentNumber(): String? {
        // Intentar desde userInfo cargado (endpoint /v1/auth/me)
        _userInfo.value?.documentNumber?.let { 
            if (it.isNotBlank()) return it 
        }

        // Intentar desde username de la sesión (puede ser el DNI si el login usa DNI como username)
        val session = authRepository.currentSession.value
        val username = session?.username ?: session?.userInfo?.username
        return username?.takeIf { it.matches(Regex("^\\d{8}$")) }
    }
}

