package com.agropay.presentation.tareo

import com.agropay.data.repository.CacheRepository
import com.agropay.data.repository.TareoRepository
import com.agropay.db.Tareo_employees
import com.agropay.db.Tareos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TareoDetailViewModel(
    private val tareoRepository: TareoRepository,
    private val cacheRepository: CacheRepository,
    tareoId: String
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Cargar tareo desde BD local
    val tareo: StateFlow<Tareos?> = flow {
        val tareo = tareoRepository.getTareoById(tareoId)
        println("📋 TareoDetailViewModel - Tareo cargado desde BD: ${tareo?.id}")
        emit(tareo)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Cargar empleados del tareo
    val employees: StateFlow<List<Tareo_employees>> = tareoRepository.getEmployeesByTareoId(tareoId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Información enriquecida del tareo
    val tareoInfo: StateFlow<TareoInfo?> = combine(
        tareo,
        employees,
        cacheRepository.getAllLabors(),
        cacheRepository.getAllLotes(),
        cacheRepository.getAllEmployees()
    ) { tareo, employees, labors, lotes, allEmployees ->
        if (tareo == null) {
            println("❌ TareoDetailViewModel - Tareo es null")
            return@combine null
        }

        println("📊 TareoDetailViewModel - Enriqueciendo información del tareo")
        println("   - Tareo ID: ${tareo.id}")
        println("   - Labor ID: ${tareo.labor_id}")
        println("   - Lote ID: ${tareo.lote_id}")
        println("   - Total empleados: ${employees.size}")

        val labor = labors.find { it.id == tareo.labor_id }
        val lote = tareo.lote_id?.let { loteId -> lotes.find { it.id == loteId } }
        val supervisor = allEmployees.find { it.document_number == tareo.supervisor_employee_document_number }
        val scanner = tareo.scanner_employee_document_number?.let { scannerDocNumber ->
            allEmployees.find { it.document_number == scannerDocNumber } 
        }

        // Formatear fecha
        val createdAt = Instant.fromEpochMilliseconds(tareo.created_at)
        val localDateTime = createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val fechaFormateada = "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"

        TareoInfo(
            tareo = tareo,
            laborName = labor?.name ?: "Labor no encontrada",
            loteName = lote?.name,
            supervisorName = supervisor?.let { "${it.names} ${it.paternal_lastname}" } ?: tareo.supervisor_employee_document_number,
            scannerName = scanner?.let { "${it.names} ${it.paternal_lastname}" },
            fecha = fechaFormateada,
            empleados = employees
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onCleared() {
        viewModelScope.cancel()
    }
}

data class TareoInfo(
    val tareo: Tareos,
    val laborName: String,
    val loteName: String?,
    val supervisorName: String,
    val scannerName: String?,
    val fecha: String,
    val empleados: List<Tareo_employees>
)

