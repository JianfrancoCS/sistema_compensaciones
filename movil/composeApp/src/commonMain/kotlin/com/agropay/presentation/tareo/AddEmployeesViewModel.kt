package com.agropay.presentation.tareo

import com.agropay.data.model.EmployeeSearchResponse
import com.agropay.data.repository.CacheRepository
import com.agropay.data.repository.TareoRepository
import com.agropay.db.Labors_cache
import com.agropay.db.Tareo_motives_cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.agropay.core.util.randomUUID
import com.agropay.core.util.isValidUUID

class AddEmployeesViewModel(
    private val cacheRepository: CacheRepository,
    private val tareoRepository: TareoRepository,
    private val userInfoRepository: com.agropay.data.repository.UserInfoRepository? = null
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val motives: StateFlow<List<Tareo_motives_cache>> = cacheRepository.getAllTareoMotives()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedLabor = MutableStateFlow<Labors_cache?>(null)
    val selectedLabor: StateFlow<Labors_cache?> = _selectedLabor.asStateFlow()

    init {
        // Cargar información del usuario al inicializar
        viewModelScope.launch {
            userInfoRepository?.loadUserInfo()
        }
    }

    fun loadLaborDetails(laborId: String) {
        viewModelScope.launch {
            _selectedLabor.value = cacheRepository.getLaborById(laborId)
        }
    }

    /**
     * Busca empleado en tiempo real por DNI al escanear QR
     * Hace petición HTTP al servidor para traer datos actualizados
     * Si se encuentra, se persiste automáticamente en cache
     *
     * @return Result con el empleado si se encuentra, o Exception con mensaje de error
     */
    suspend fun searchEmployeeByDNI(
        dni: String,
        loteId: String
    ): Result<EmployeeSearchResponse> {
        // Petición HTTP al backend para buscar empleado
        return cacheRepository.searchEmployee(dni)
    }

    /**
     * Crea un nuevo tareo en la base de datos local.
     * Recibe loteId y laborId según lo que espera la tabla tareos
     * Valida los datos antes de crear
     * NOTA: loteId puede ser null para tareos administrativos
     * 
     * @param scannerDocumentNumber DNI del pedeteador/acopiador (opcional, solo para labores a destajo)
     */
    fun createNewTareo(
        laborId: String,
        loteId: String? = null, // NULL para tareos administrativos
        supervisorDocumentNumber: String? = null, // Si es null, se obtiene del usuario logueado
        scannerDocumentNumber: String? = null, // CORREGIDO: DNI del pedeteador para labores a destajo
        onTareoCreated: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📝 CREANDO NUEVO TAREO")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📋 Labor ID: $laborId")
        println("📋 Lote ID: ${loteId ?: "null (administrativo)"}")
        println("👤 Scanner DNI: ${scannerDocumentNumber ?: "null"}")
        
        viewModelScope.launch {
            // Obtener DNI del supervisor (del usuario logueado si no se proporciona)
            val supervisorDni = supervisorDocumentNumber 
                ?: userInfoRepository?.getCurrentUserDocumentNumber()
                ?: run {
                    onError("No se pudo obtener el DNI del supervisor. Por favor, inicia sesión nuevamente.")
                    return@launch
                }

            // Validar datos antes de crear
            val validation = com.agropay.data.validation.TareoValidation.validateTareoCreation(
                laborId = laborId,
                loteId = loteId,
                supervisorDocumentNumber = supervisorDni
            )

            if (validation is com.agropay.data.validation.ValidationResult.Invalid) {
                onError(validation.message)
                return@launch
            }

            // CORREGIDO: Usar UUID en lugar de random para evitar colisiones
            val newTareoId = randomUUID()
            println("🆔 Nuevo Tareo ID generado: $newTareoId")
            println("👤 Supervisor DNI: $supervisorDni")

            // Guardar en BD
            println("💾 Guardando tareo en BD local...")
            tareoRepository.createTareo(
                id = newTareoId,
                supervisorDocumentNumber = supervisorDni,
                laborId = laborId,
                loteId = loteId, // Puede ser null para tareos administrativos
                scannerDocumentNumber = scannerDocumentNumber, // CORREGIDO: Guardar pedeteador
                isClosing = false
            )
            println("✅ Tareo guardado en BD local exitosamente")

            // Agregar al estado temporal (TareoState) para que se pueda visualizar
            // Obtener nombres para mostrar
            val lote = loteId?.let { cacheRepository.getLoteById(it) }
            val labor = cacheRepository.getLaborById(laborId)
            val fundo = lote?.let { cacheRepository.getSubsidiaryById(it.subsidiary_id) }

            val tareoData = com.agropay.ui.state.TareoData(
                id = newTareoId,
                titulo = "${fundo?.name ?: "Fundo"} - ${labor?.name ?: "Labor"}",
                fundo = fundo?.name ?: "Fundo desconocido",
                loteId = loteId ?: "",
                labor = labor?.name ?: "Labor desconocida",
                laborId = laborId,
                fecha = kotlinx.datetime.Clock.System.now().toString().substring(0, 10),
                tipoLabor = if (labor?.is_piecework == true)
                    com.agropay.ui.state.TipoLabor.DESTAJO
                else
                    com.agropay.ui.state.TipoLabor.JORNAL
            )
            com.agropay.ui.state.TareoState.agregarTareo(tareoData)

            onTareoCreated(newTareoId)
        }
    }

    /**
     * Agrega empleados al tareo con sus motivos
     * CORREGIDO: Ahora recibe motiveId (UUID) en lugar de motiveName
     * @param tareoId ID del tareo
     * @param employees Lista de empleados con DNI y UUID del motivo
     * @return Result con lista de errores si los hay
     */
    suspend fun addEmployeesToTareo(
        tareoId: String,
        employees: List<Pair<String, String>> // Pair<documentNumber, motiveId>
    ): Result<Unit> {
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("👥 AGREGANDO EMPLEADOS AL TAREO")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📋 Tareo ID: $tareoId")
        println("👥 Total empleados a agregar: ${employees.size}")
        
        val errors = mutableListOf<String>()

        employees.forEachIndexed { index, (documentNumber, motiveId) ->
            println("  👤 Procesando empleado ${index + 1}/${employees.size}:")
            println("     - DNI: $documentNumber")
            println("     - Motivo ID: $motiveId")
            // Validar DNI
            if (documentNumber.isBlank()) {
                errors.add("DNI vacío")
                return@forEachIndexed
            }

            if (!documentNumber.matches(Regex("^\\d{8}$"))) {
                errors.add("DNI $documentNumber: debe tener 8 dígitos")
                return@forEachIndexed
            }

            // CORREGIDO: Validar que motiveId sea UUID válido (no buscar por nombre)
            if (motiveId.isBlank()) {
                errors.add("Empleado $documentNumber: motivo no especificado")
                return@forEachIndexed
            }

            // Validar formato UUID
            if (!isValidUUID(motiveId)) {
                errors.add("Empleado $documentNumber: motivo ID inválido (no es UUID)")
                return@forEachIndexed
            }

            // CORREGIDO: Validar que el motivo exista en cache
            val motive = motives.value.find { it.id == motiveId }
            if (motive == null) {
                errors.add("Empleado $documentNumber: motivo con ID '$motiveId' no encontrado en cache")
                return@forEachIndexed
            }

            // Formatear hora actual como HH:mm (formato compatible con backend)
            val currentTime = kotlinx.datetime.Clock.System.now()
            val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
            val localDateTime = currentTime.toLocalDateTime(timeZone)
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val formattedTime = "$hour:$minute"

            // CORREGIDO: Usar UUID en lugar de random para IDs de empleados
            val employeeId = randomUUID()
            println("     - Generando ID de empleado: $employeeId")
            println("     - Hora de entrada: $formattedTime")
            
            tareoRepository.addEmployeeToTareo(
                id = employeeId,
                tareoId = tareoId,
                employeeDocumentNumber = documentNumber,
                startTime = formattedTime,
                entryMotiveId = motiveId, // CORREGIDO: Usar UUID directamente
                endTime = null,
                exitMotiveId = null
            )
            println("     ✅ Empleado agregado exitosamente a la BD local")
        }

        if (errors.isEmpty()) {
            println("✅ Todos los empleados agregados exitosamente")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return Result.success(Unit)
        } else {
            println("❌ Errores encontrados:")
            errors.forEach { error ->
                println("   - $error")
            }
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return Result.failure(Exception("Errores al agregar empleados: ${errors.joinToString(", ")}"))
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
