package com.agropay.presentation.sync

import com.agropay.data.repository.CacheRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados de sincronización
 */
sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * ViewModel para gestionar sincronización de datos
 */
class SyncViewModel(
    private val cacheRepository: CacheRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /**
     * Descarga todos los datos del servidor
     */
    fun downloadAllData() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading

            try {
                val result = cacheRepository.downloadAllCacheData()

                result.fold(
                    onSuccess = {
                        _syncState.value = SyncState.Success("Datos descargados correctamente")
                    },
                    onFailure = { error ->
                        _syncState.value = SyncState.Error(
                            error.message ?: "Error al descargar datos"
                        )
                    }
                )
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(
                    e.message ?: "Error inesperado"
                )
            }
        }
    }

    /**
     * Descarga solo sucursales
     */
    fun downloadSubsidiaries() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading

            try {
                val result = cacheRepository.downloadSubsidiaries()

                result.fold(
                    onSuccess = {
                        _syncState.value = SyncState.Success("Sucursales descargadas")
                    },
                    onFailure = { error ->
                        _syncState.value = SyncState.Error(
                            error.message ?: "Error al descargar sucursales"
                        )
                    }
                )
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    /**
     * Descarga solo labores
     */
    fun downloadLabors() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading

            try {
                val result = cacheRepository.downloadLabors()

                result.fold(
                    onSuccess = {
                        _syncState.value = SyncState.Success("Labores descargadas")
                    },
                    onFailure = { error ->
                        _syncState.value = SyncState.Error(
                            error.message ?: "Error al descargar labores"
                        )
                    }
                )
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    /**
     * Descarga solo motivos de tareo
     */
    fun downloadTareoMotives() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading

            try {
                val result = cacheRepository.downloadTareoMotives()

                result.fold(
                    onSuccess = {
                        _syncState.value = SyncState.Success("Motivos descargados")
                    },
                    onFailure = { error ->
                        _syncState.value = SyncState.Error(
                            error.message ?: "Error al descargar motivos"
                        )
                    }
                )
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    /**
     * Sube todos los datos pendientes al servidor (tareos y harvest records)
     * Se llama cuando el usuario presiona "Cargar datos"
     */
    fun uploadAllPendingData() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading

            try {
                val result = cacheRepository.uploadAllPendingData()

                result.fold(
                    onSuccess = { message ->
                        _syncState.value = SyncState.Success(message)
                    },
                    onFailure = { error ->
                        _syncState.value = SyncState.Error(
                            error.message ?: "Error al cargar datos"
                        )
                    }
                )
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(
                    e.message ?: "Error inesperado al cargar datos"
                )
            }
        }
    }

    /**
     * Resetea el estado de sincronización
     */
    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
