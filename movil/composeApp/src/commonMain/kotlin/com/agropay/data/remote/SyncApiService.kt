package com.agropay.data.remote

import com.agropay.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Servicio para endpoints de sincronización y búsqueda
 */
class SyncApiService(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) {

    // ========================================================================
    // DESCARGA DE DATOS (SYNC)
    // ========================================================================

    /**
     * Descarga todas las sucursales activas
     * GET /v1/subsidiaries/sync
     */
    suspend fun syncSubsidiaries(): Result<List<SubsidiarySyncResponse>> {
        return try {
            val response = client.get("$baseUrl/v1/subsidiaries/sync")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<List<SubsidiarySyncResponse>> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga todas las labores activas
     * GET /v1/labors/sync
     */
    suspend fun syncLabors(): Result<List<LaborSyncResponse>> {
        return try {
            val response = client.get("$baseUrl/v1/labors/sync")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<List<LaborSyncResponse>> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga todos los motivos de tareo activos
     * GET /v1/tareo-motives/sync
     */
    suspend fun syncTareoMotives(): Result<List<TareoMotiveSyncResponse>> {
        return try {
            val response = client.get("$baseUrl/v1/tareo-motives/sync")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<List<TareoMotiveSyncResponse>> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga todas las posiciones activas
     * GET /v1/positions/sync
     */
    suspend fun syncPositions(): Result<List<PositionSyncResponse>> {
        return try {
            val response = client.get("$baseUrl/v1/positions/sync")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<List<PositionSyncResponse>> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga todos los lotes activos
     * GET /v1/lotes/sync
     */
    suspend fun syncLotes(): Result<List<LoteSyncResponse>> {
        return try {
            val response = client.get("$baseUrl/v1/lotes/sync")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<List<LoteSyncResponse>> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga empleados de un tareo con sus QR rolls y codes asignados
     * GET /v1/tareos/{tareoPublicId}/employees/sync
     */
    suspend fun syncTareoEmployees(tareoPublicId: String): Result<List<EmployeeWithQrRollsResponse>> {
        return try {
            val response = client.get("$baseUrl/v1/tareos/$tareoPublicId/employees/sync")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<List<EmployeeWithQrRollsResponse>> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene información del usuario logueado
     * GET /v1/auth/me
     */
    suspend fun getMyInfo(): Result<UserInfoResponse> {
        return try {
            val response = client.get("$baseUrl/v1/auth/me")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<UserInfoResponse> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(apiResult.data)
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // SUBIDA DE DATOS (BATCH SYNC)
    // ========================================================================

    /**
     * Sube tareos pendientes en batch
     * POST /v1/tareos/batch-sync
     */
    suspend fun uploadTareosBatch(request: BatchTareoSyncRequest): Result<BatchTareoResponse> {
        return try {
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("📤 INICIANDO SUBIDA DE TAREOS AL BACKEND")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("📊 Total de tareos a subir: ${request.tareos.size}")
            
            request.tareos.forEachIndexed { index, tareo ->
                println("  📋 Tareo ${index + 1}/${request.tareos.size}:")
                println("     - ID Temporal: ${tareo.temporalId}")
                println("     - Labor Public ID: ${tareo.laborPublicId}")
                println("     - Lote Public ID: ${tareo.lotePublicId ?: "null (administrativo)"}")
                println("     - Supervisor DNI: ${tareo.supervisorDocumentNumber}")
                println("     - Scanner DNI: ${tareo.scannerDocumentNumber ?: "null"}")
                println("     - Es Cierre: ${tareo.isClosing}")
                println("     - Total Empleados: ${tareo.employees.size}")
                tareo.employees.forEachIndexed { empIndex, emp ->
                    println("       👤 Empleado ${empIndex + 1}:")
                    println("          - DNI: ${emp.documentNumber}")
                    println("          - Hora Entrada: ${emp.entryTime}")
                    println("          - Motivo Entrada ID: ${emp.entryMotivePublicId}")
                    println("          - Hora Salida: ${emp.exitTime ?: "null"}")
                    println("          - Motivo Salida ID: ${emp.exitMotivePublicId ?: "null"}")
                }
            }
            
            println("🌐 Enviando petición POST a: $baseUrl/v1/tareos/batch-sync")
            val response = client.post("$baseUrl/v1/tareos/batch-sync") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            println("📥 Respuesta recibida - Status: ${response.status.value} ${response.status.description}")

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<BatchTareoResponseData> = response.body()
                println("✅ Respuesta exitosa del backend")
                println("   - Success: ${apiResult.success}")
                println("   - Message: ${apiResult.message}")
                
                if (apiResult.success && apiResult.data != null) {
                    println("   - Total items procesados: ${apiResult.data.items.size}")
                    apiResult.data.items.forEachIndexed { index, item ->
                        if (item.success) {
                            println("   ✅ Item ${index + 1} (${item.identifier}): EXITOSO")
                        } else {
                            println("   ❌ Item ${index + 1} (${item.identifier}): FALLIDO")
                            item.errors?.forEach { error ->
                                if (error.field != null) {
                                    println("      - Campo '${error.field}': ${error.message}")
                                } else {
                                    println("      - Error: ${error.message}")
                                }
                                if (error.code != null) {
                                    println("        Código: ${error.code}")
                                }
                            }
                        }
                    }
                    
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Result.success(
                        BatchTareoResponse(
                            success = true,
                            message = apiResult.message,
                            data = apiResult.data
                        )
                    )
                } else {
                    println("❌ Respuesta del backend indica error")
                    println("   - Message: ${apiResult.message}")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Result.failure(Exception(apiResult.message ?: "Error al subir tareos"))
                }
            } else {
                println("❌ Error HTTP: ${response.status.value} ${response.status.description}")
                val errorBody = response.body<String>()
                println("   - Response body: $errorBody")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            println("❌ EXCEPCIÓN al subir tareos:")
            println("   - Tipo: ${e.javaClass.simpleName}")
            println("   - Mensaje: ${e.message}")
            e.printStackTrace()
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Result.failure(e)
        }
    }

    /**
     * Sube harvest records pendientes en batch
     * POST /v1/harvest-records/batch-sync
     */
    suspend fun uploadHarvestRecordsBatch(request: BatchHarvestSyncRequest): Result<BatchHarvestResponse> {
        return try {
            val response = client.post("$baseUrl/v1/harvest-records/batch-sync") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<BatchHarvestResponseData> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(
                        BatchHarvestResponse(
                            success = true,
                            message = apiResult.message,
                            data = apiResult.data
                        )
                    )
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error al subir harvest records"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Asigna QR rolls a empleados en batch
     * POST /v1/qr-rolls/batch-assign
     */
    suspend fun batchAssignQrRolls(request: BatchQrRollAssignmentRequest): Result<BatchQrRollAssignmentResponse> {
        return try {
            val response = client.post("$baseUrl/v1/qr-rolls/batch-assign") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val apiResult: ApiResult<BatchQrRollAssignmentData> = response.body()
                if (apiResult.success && apiResult.data != null) {
                    Result.success(
                        BatchQrRollAssignmentResponse(
                            success = true,
                            message = apiResult.message,
                            data = apiResult.data
                        )
                    )
                } else {
                    Result.failure(Exception(apiResult.message ?: "Error al asignar QR rolls"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // BÚSQUEDA DE EMPLEADOS EN TIEMPO REAL
    // ========================================================================

    /**
     * Busca un empleado por DNI (cache lookup en tiempo real)
     * GET /v1/employees/cache?documentNumber=xxx
     *
     * Response 200: Empleado encontrado y habilitado
     * Response 400: Empleado no encontrado o no habilitado
     */
    suspend fun searchEmployee(
        documentNumber: String
    ): Result<EmployeeSearchResponse> {
        return try {
            val response = client.get("$baseUrl/v1/employees/cache") {
                parameter("documentNumber", documentNumber)
            }

            // SIEMPRE parsear el body para obtener el message, sin importar el código HTTP
            val apiResult: ApiResult<EmployeeSearchResponse> = response.body()

            if (apiResult.success && apiResult.data != null) {
                // ✅ success: true con data
                Result.success(apiResult.data)
            } else {
                // ❌ success: false o data null - Construir mensaje de error con detalles
                val errorMessage = com.agropay.data.error.ErrorHandler.buildErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
