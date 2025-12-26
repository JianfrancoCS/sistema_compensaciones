package com.agropay.presentation.tareo

import com.agropay.data.repository.TareoRepository
import com.agropay.db.Tareos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TareoListViewModel(
    private val tareoRepository: TareoRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val tareos: StateFlow<List<Tareos>> = tareoRepository.getAllTareos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Elimina un tareo de la base de datos
     * Solo puede eliminar tareos que NO estén sincronizados (is_synced = false)
     */
    suspend fun deleteTareo(tareoId: String): Result<Unit> {
        return try {
            tareoRepository.deleteTareo(tareoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
