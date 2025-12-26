package com.agropay.presentation.tareo

import com.agropay.data.repository.CacheRepository
import com.agropay.db.Labors_cache
import com.agropay.db.Lotes_cache
import com.agropay.db.Subsidiaries_cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de creación de tareo
 */
class CreateTareoViewModel(
    private val cacheRepository: CacheRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Fundos (subsidiaries)
    private val _fundos = MutableStateFlow<List<Subsidiaries_cache>>(emptyList())
    val fundos: StateFlow<List<Subsidiaries_cache>> = _fundos.asStateFlow()

    // Lotes (filtrados por fundo seleccionado)
    private val _lotes = MutableStateFlow<List<Lotes_cache>>(emptyList())
    val lotes: StateFlow<List<Lotes_cache>> = _lotes.asStateFlow()

    // Labores
    private val _labors = MutableStateFlow<List<Labors_cache>>(emptyList())
    val labors: StateFlow<List<Labors_cache>> = _labors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    /**
     * Carga datos desde BD local
     */
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            // Cargar fundos (subsidiaries)
            launch {
                cacheRepository.getAllSubsidiaries().collect { subsidiaries ->
                    _fundos.value = subsidiaries
                }
            }

            // Cargar labores
            launch {
                cacheRepository.getAllLabors().collect { labors ->
                    _labors.value = labors
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Carga lotes según el fundo seleccionado
     */
    fun loadLotesByFundo(fundoId: String) {
        viewModelScope.launch {
            cacheRepository.getLotesBySubsidiary(fundoId).collect { lotes ->
                _lotes.value = lotes
            }
        }
    }

    /**
     * Recargar datos (útil después de sincronización)
     */
    fun reloadData() {
        loadData()
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
