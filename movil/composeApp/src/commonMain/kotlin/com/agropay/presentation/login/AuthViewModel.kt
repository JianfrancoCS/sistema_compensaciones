package com.agropay.presentation.login

import com.agropay.core.presentation.ViewModel
import com.agropay.data.repository.AuthRepository
import com.agropay.domain.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar el estado de autenticación
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Verifica el estado de autenticación al iniciar
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (authRepository.isAuthenticated()) {
                authRepository.currentSession.collect { session ->
                    _authState.value = if (session != null) {
                        AuthState.Authenticated(session)
                    } else {
                        AuthState.Unauthenticated
                    }
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Inicia sesión con username (número de documento), password y perfil
     */
    fun login(username: String, password: String, profile: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            authRepository.login(username, password, profile)
                .onSuccess { session ->
                    _authState.value = AuthState.Authenticated(session)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(
                        error.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Limpia el estado de error
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
