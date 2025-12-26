package com.agropay.data.repository

import com.agropay.data.local.Database
import com.agropay.data.model.*
import com.agropay.data.remote.SyncApiService
import com.agropay.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock

/**
 * Repository para gestionar datos de cache (sucursales, labores, motivos)
 */
class CacheRepository(
    private val syncApi: SyncApiService
) {
    private val db = Database.instance


    /**
     * Descarga sucursales del servidor y las guarda en cache local
     */
    suspend fun downloadSubsidiaries(): Result<Unit> {
        return try {
            val result = syncApi.syncSubsidiaries()

            result.fold(
                onSuccess = { subsidiaries ->
                    // Guardar en cache local
                    subsidiaries.forEach { subsidiary ->
                        db.subsidiariesCacheQueries.insertOrReplace(
                            id = subsidiary.publicId,
                            name = subsidiary.name,
                            cached_at = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las sucursales desde cache local
     */
    fun getAllSubsidiaries(): Flow<List<Subsidiaries_cache>> {
        return db.subsidiariesCacheQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca sucursal por ID
     */
    fun getSubsidiaryById(id: String): Subsidiaries_cache? {
        return db.subsidiariesCacheQueries.getById(id).executeAsOneOrNull()
    }

    // ========================================
    // LABORES
    // ========================================

    /**
     * Descarga labores del servidor y las guarda en cache local
     */
    suspend fun downloadLabors(): Result<Unit> {
        return try {
            val result = syncApi.syncLabors()

            result.fold(
                onSuccess = { labors ->
                    // Guardar en cache local
                    labors.forEach { labor ->
                        db.laborsCacheQueries.insertOrReplace(
                            id = labor.publicId,
                            name = labor.name,
                            description = labor.description,
                            is_piecework = labor.isPiecework,
                            labor_unit_name = labor.laborUnitName,
                            min_task_requirement = labor.minTaskRequirement,
                            base_price = labor.basePrice,
                            cached_at = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las labores desde cache local
     */
    fun getAllLabors(): Flow<List<Labors_cache>> {
        return db.laborsCacheQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Obtiene labores a destajo (piecework)
     */
    fun getPieceworkLabors(): Flow<List<Labors_cache>> {
        return db.laborsCacheQueries
            .getPieceworkLabors()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca labor por ID
     */
    fun getLaborById(id: String): Labors_cache? {
        return db.laborsCacheQueries.getById(id).executeAsOneOrNull()
    }

    // ========================================
    // POSICIONES
    // ========================================

    /**
     * Descarga posiciones del servidor y las guarda en cache local
     */
    suspend fun downloadPositions(): Result<Unit> {
        return try {
            val result = syncApi.syncPositions()

            result.fold(
                onSuccess = { positions ->
                    // Guardar en cache local
                    positions.forEach { position ->
                        db.positionsCacheQueries.insertOrReplace(
                            id = position.publicId,
                            name = position.name,
                            cached_at = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las posiciones desde cache local
     */
    fun getAllPositions(): Flow<List<Positions_cache>> {
        return db.positionsCacheQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca posición por ID
     */
    fun getPositionById(id: String): Positions_cache? {
        return db.positionsCacheQueries.getById(id).executeAsOneOrNull()
    }

    // ========================================
    // MOTIVOS DE TAREO
    // ========================================

    /**
     * Descarga motivos de tareo del servidor y los guarda en cache local
     */
    suspend fun downloadTareoMotives(): Result<Unit> {
        return try {
            val result = syncApi.syncTareoMotives()

            result.fold(
                onSuccess = { motives ->
                    // Guardar en cache local
                    motives.forEach { motive ->
                        db.tareoMotivesCacheQueries.insertOrReplace(
                            id = motive.publicId,
                            name = motive.name,
                            description = motive.description,
                            is_paid = motive.isPaid,
                            cached_at = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los motivos de tareo desde cache local
     */
    fun getAllTareoMotives(): Flow<List<Tareo_motives_cache>> {
        return db.tareoMotivesCacheQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Obtiene solo motivos pagados
     */
    fun getPaidTareoMotives(): Flow<List<Tareo_motives_cache>> {
        return db.tareoMotivesCacheQueries
            .getPaidMotives()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca motivo por ID
     */
    fun getTareoMotiveById(id: String): Tareo_motives_cache? {
        return db.tareoMotivesCacheQueries.getById(id).executeAsOneOrNull()
    }

    // ========================================
    // LOTES
    // ========================================

    /**
     * Descarga lotes del servidor y los guarda en cache local
     */
    suspend fun downloadLotes(): Result<Unit> {
        return try {
            val result = syncApi.syncLotes()

            result.fold(
                onSuccess = { lotes ->
                    // Guardar en cache local
                    lotes.forEach { lote ->
                        db.lotesCacheQueries.insertOrReplace(
                            id = lote.id,
                            name = lote.name,
                            hectareage = lote.hectareage,
                            subsidiary_id = lote.subsidiaryId,
                            cached_at = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los lotes desde cache local
     */
    fun getAllLotes(): Flow<List<Lotes_cache>> {
        return db.lotesCacheQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Obtiene lotes por sucursal
     */
    fun getLotesBySubsidiary(subsidiaryId: String): Flow<List<Lotes_cache>> {
        return db.lotesCacheQueries
            .getBySubsidiary(subsidiaryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca lote por ID
     */
    fun getLoteById(id: String): Lotes_cache? {
        return db.lotesCacheQueries.getById(id).executeAsOneOrNull()
    }

    // ========================================
    // EMPLEADOS CACHE
    // ========================================

    /**
     * Obtiene todos los empleados desde cache local
     */
    fun getAllEmployees(): Flow<List<Employees_cache>> {
        return db.employeesCacheQueries
            .getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca empleado por número de documento (DNI)
     */
    fun getEmployeeByDocumentNumber(documentNumber: String): Employees_cache? {
        return db.employeesCacheQueries.getByDocumentNumber(documentNumber).executeAsOneOrNull()
    }

    // ========================================
    // QR ROLLS Y CODES
    // ========================================

    /**
     * Descarga empleados de un tareo con sus QR rolls y codes asignados
     * Este método pobla 4 tablas: employees_cache, qr_rolls_cache, qr_roll_employees, qr_codes
     * NOTA: Este endpoint ya NO se usa para búsqueda de empleados, solo para QR rolls
     */
    suspend fun downloadTareoEmployees(tareoPublicId: String): Result<Unit> {
        return try {
            val result = syncApi.syncTareoEmployees(tareoPublicId)

            result.fold(
                onSuccess = { employees ->
                    val now = Clock.System.now().toEpochMilliseconds()

                    employees.forEach { employee ->
                        // 1. Guardar empleado en cache (sin position_id, porque este endpoint no lo provee)
                        // NOTA: Este método es legacy, se mantiene solo para QR rolls
                        // La búsqueda de empleados ahora se hace por /v1/employees/cache
                        db.employeesCacheQueries.insertOrReplace(
                            document_number = employee.documentNumber,
                            names = employee.names,
                            paternal_lastname = employee.paternalLastname,
                            maternal_lastname = employee.maternalLastname,
                            subsidiary_id = "", // No disponible en este endpoint
                            position_id = "", // No disponible en este endpoint
                            cached_at = now
                        )

                        // 2. Guardar QR rolls y codes del empleado
                        employee.qrRolls.forEach { qrRoll ->
                            // Guardar el roll
                            db.qrRollsCacheQueries.insertOrReplace(
                                id = qrRoll.qrRollId,
                                max_qr_codes_per_day = qrRoll.maxQrCodesPerDay?.toLong(),
                                cached_at = now
                            )

                            // Guardar la asignación empleado-roll
                            db.qrRollEmployeesQueries.insertOrReplace(
                                id = qrRoll.qrRollEmployeeId,
                                qr_roll_id = qrRoll.qrRollId,
                                employee_document_number = employee.documentNumber,
                                assigned_date = now,
                                cached_at = now
                            )

                            // Guardar los códigos QR del roll
                            qrRoll.qrCodes.forEach { qrCode ->
                                db.qrCodesQueries.insertOrReplace(
                                    public_id = qrCode.publicId,
                                    qr_roll_id = qrRoll.qrRollId,
                                    is_used = qrCode.isUsed,
                                    is_printed = qrCode.isPrinted,
                                    cached_at = now
                                )
                            }
                        }
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los QR rolls asignados a un empleado por su DNI
     */
    fun getQrRollsByEmployee(documentNumber: String): Flow<List<Qr_roll_employees>> {
        return db.qrRollEmployeesQueries
            .getByEmployeeDocumentNumber(documentNumber)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Obtiene todos los códigos QR de un roll específico
     */
    fun getQrCodesByRoll(qrRollId: String): Flow<List<Qr_codes>> {
        return db.qrCodesQueries
            .getByRollId(qrRollId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Obtiene códigos QR disponibles (no usados) de un roll
     */
    fun getAvailableQrCodesByRoll(qrRollId: String): Flow<List<Qr_codes>> {
        return db.qrCodesQueries
            .getAvailableByRollId(qrRollId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    /**
     * Busca código QR por su publicId
     */
    fun getQrCodeById(publicId: String): Qr_codes? {
        return db.qrCodesQueries.getByPublicId(publicId).executeAsOneOrNull()
    }

    /**
     * Asigna QR rolls a empleados en batch
     */
    suspend fun batchAssignQrRolls(assignments: List<QrRollAssignment>): Result<BatchQrRollAssignmentResponse> {
        return try {
            val request = BatchQrRollAssignmentRequest(assignments = assignments)
            syncApi.batchAssignQrRolls(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // SINCRONIZACIÓN COMPLETA
    // ========================================

    /**
     * Descarga todos los datos de cache del servidor
     */
    suspend fun downloadAllCacheData(): Result<Unit> {
        return try {
            // Descargar en paralelo (o secuencial, según prefieras)
            val subsidiariesResult = downloadSubsidiaries()
            if (subsidiariesResult.isFailure) {
                return Result.failure(subsidiariesResult.exceptionOrNull()!!)
            }

            val lotesResult = downloadLotes()
            if (lotesResult.isFailure) {
                return Result.failure(lotesResult.exceptionOrNull()!!)
            }

            val laborsResult = downloadLabors()
            if (laborsResult.isFailure) {
                return Result.failure(laborsResult.exceptionOrNull()!!)
            }

            val motivesResult = downloadTareoMotives()
            if (motivesResult.isFailure) {
                return Result.failure(motivesResult.exceptionOrNull()!!)
            }

            val positionsResult = downloadPositions()
            if (positionsResult.isFailure) {
                return Result.failure(positionsResult.exceptionOrNull()!!)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sube todos los datos pendientes al servidor (tareos y harvest records)
     * Valida que los datos estén completos antes de sincronizar
     */
    suspend fun uploadAllPendingData(): Result<String> {
        return try {
            var tareosUploaded = 0
            var tareosFailed = 0
            val harvestRecordsUploaded = 0
            val errors = mutableListOf<String>()

            // 1. Subir tareos pendientes
            val pendingTareos = db.tareosQueries.getNotSynced().executeAsList()

            if (pendingTareos.isNotEmpty()) {
                val batchTareos = mutableListOf<BatchTareoData>()

                pendingTareos.forEach { tareo ->
                    // Validar tareo antes de incluir en el batch
                    val tareoValidation = com.agropay.data.validation.TareoValidation.validateTareoForSync(tareo)
                    if (tareoValidation is com.agropay.data.validation.ValidationResult.Invalid) {
                        errors.add("Tareo ${tareo.id}: ${tareoValidation.message}")
                        tareosFailed++
                        return@forEach
                    }

                    // Obtener empleados del tareo
                    val employees = db.tareoEmployeesQueries.getByTareoId(tareo.id).executeAsList()

                    // Validar que tenga empleados
                    val employeesValidation = com.agropay.data.validation.TareoValidation.validateTareoHasEmployees(employees)
                    if (employeesValidation is com.agropay.data.validation.ValidationResult.Invalid) {
                        errors.add("Tareo ${tareo.id}: ${employeesValidation.message}")
                        tareosFailed++
                        return@forEach
                    }

                    // Mapear empleados y filtrar los que no tienen entryTime
                    val validEmployees = employees.mapNotNull { emp ->
                        // Validar que tenga entryTime (requerido por el backend)
                        val entryTime = emp.start_time
                        if (entryTime.isNullOrBlank()) {
                            // Si no tiene entryTime, no incluir este empleado (debería haber sido validado antes)
                            errors.add("Tareo ${tareo.id} - Empleado ${emp.employee_document_number}: falta hora de entrada")
                            null
                        } else {
                            BatchEmployeeData(
                                documentNumber = emp.employee_document_number,
                                entryTime = entryTime, // Formato HH:mm (compatible con LocalTime del backend)
                                entryMotivePublicId = emp.entry_motive_id, // UUID como String (Jackson lo convierte a UUID)
                                exitTime = emp.end_time, // Puede ser null (Jackson lo convierte a LocalTime o null)
                                exitMotivePublicId = emp.exit_motive_id // Puede ser null (Jackson lo convierte a UUID o null)
                            )
                        }
                    }

                    // Solo agregar el tareo si tiene al menos un empleado válido
                    if (validEmployees.isEmpty()) {
                        errors.add("Tareo ${tareo.id}: no tiene empleados válidos (todos sin hora de entrada)")
                        tareosFailed++
                        return@forEach
                    }

                    batchTareos.add(
                        BatchTareoData(
                            temporalId = tareo.id,
                            laborPublicId = tareo.labor_id,
                            lotePublicId = tareo.lote_id, // Puede ser null para tareos administrativos
                            supervisorDocumentNumber = tareo.supervisor_employee_document_number,
                            scannerDocumentNumber = tareo.scanner_employee_document_number,
                            employees = validEmployees,
                            isClosing = tareo.is_closing ?: false, // Campo para indicar si es cierre
                            closingMotivePublicId = tareo.closing_motive_id // Motivo de cierre
                        )
                    )
                }

                // Solo enviar si hay tareos válidos
                if (batchTareos.isNotEmpty()) {
                    println("📦 Preparando batch de ${batchTareos.size} tareos para subir al backend")
                    
                    val batchRequest = BatchTareoSyncRequest(tareos = batchTareos)
                    println("🚀 Llamando a uploadTareosBatch...")
                    val uploadResult = syncApi.uploadTareosBatch(batchRequest)

                    uploadResult.fold(
                        onSuccess = { response ->
                            println("✅ Respuesta de uploadTareosBatch recibida exitosamente")
                            println("   - Total items en respuesta: ${response.data?.items?.size ?: 0}")
                            
                            // Marcar como sincronizados los exitosos
                            response.data?.items?.forEach { item ->
                                if (item.success) {
                                    println("   ✅ Marcando tareo ${item.identifier} como sincronizado")
                                    db.tareosQueries.markAsSynced(item.identifier)
                                    // También marcar empleados como sincronizados
                                    db.tareoEmployeesQueries.markAllAsSyncedByTareoId(item.identifier)
                                    tareosUploaded++
                                    println("      - Tareo y empleados marcados como sincronizados")
                                } else {
                                    println("   ❌ Tareo ${item.identifier} falló en el backend")
                                    // Agregar error del servidor
                                    val errorMessage = item.errors?.firstOrNull()?.message ?: "Error desconocido"
                                    println("      - Error: $errorMessage")
                                    item.errors?.forEach { error ->
                                        if (error.field != null) {
                                            println("        - Campo '${error.field}': ${error.message}")
                                        } else {
                                            println("        - Error: ${error.message}")
                                        }
                                        if (error.code != null) {
                                            println("          Código: ${error.code}")
                                        }
                                    }
                                    errors.add("Tareo ${item.identifier}: $errorMessage")
                                    tareosFailed++
                                }
                            }
                            println("📊 Resumen: $tareosUploaded exitosos, $tareosFailed fallidos")
                        },
                        onFailure = { error ->
                            println("❌ Error al subir tareos:")
                            println("   - Tipo: ${error.javaClass.simpleName}")
                            println("   - Mensaje: ${error.message}")
                            error.printStackTrace()
                            // Log error pero continuar con harvest records
                            errors.add("Error al subir tareos: ${error.message}")
                            tareosFailed += batchTareos.size
                        }
                    )
                } else if (pendingTareos.isNotEmpty()) {
                    // Todos los tareos tienen errores de validación
                    errors.add("Ningún tareo pasó la validación. Revisa los datos.")
                }
            }

            // 2. Subir harvest records pendientes
            // TODO: Implementar cuando tengas la tabla de harvest records
            // val pendingHarvests = db.harvestRecordsQueries.getNotSynced().executeAsList()
            // if (pendingHarvests.isNotEmpty()) { ... }

            // Construir mensaje de resultado
            val messages = mutableListOf<String>()
            if (tareosUploaded > 0) {
                messages.add("$tareosUploaded tareos sincronizados")
            }
            if (harvestRecordsUploaded > 0) {
                messages.add("$harvestRecordsUploaded registros de cosecha")
            }
            if (tareosFailed > 0) {
                messages.add("$tareosFailed tareos fallaron")
            }

            val resultMessage = when {
                messages.isEmpty() -> "No hay datos pendientes para cargar"
                errors.isEmpty() -> "Cargados exitosamente: ${messages.joinToString(", ")}"
                else -> {
                    val successMsg = if (tareosUploaded > 0) "Cargados: $tareosUploaded tareos. " else ""
                    val errorMsg = if (errors.size <= 3) {
                        errors.joinToString(". ")
                    } else {
                        "${errors.take(3).joinToString(". ")} y ${errors.size - 3} más..."
                    }
                    "$successMsg Errores: $errorMsg"
                }
            }

            // Si hay errores pero también éxitos, retornar success con mensaje detallado
            // Si solo hay errores, retornar failure
            if (tareosUploaded > 0 || harvestRecordsUploaded > 0) {
                Result.success(resultMessage)
            } else if (errors.isNotEmpty()) {
                Result.failure(Exception(resultMessage))
            } else {
                Result.success(resultMessage)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca empleado en tiempo real al escanear QR
     * Si se encuentra, lo persiste en cache local
     */
    suspend fun searchEmployee(documentNumber: String): Result<EmployeeSearchResponse> {
        return try {
            val result = syncApi.searchEmployee(documentNumber)

            result.onSuccess { employee ->
                // Persistir en cache local
                db.employeesCacheQueries.insertOrReplace(
                    document_number = employee.documentNumber,
                    names = employee.names,
                    paternal_lastname = employee.paternalLastname,
                    maternal_lastname = employee.maternalLastname,
                    subsidiary_id = employee.subsidiaryId,
                    position_id = employee.positionId,
                    cached_at = Clock.System.now().toEpochMilliseconds()
                )
            }

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
