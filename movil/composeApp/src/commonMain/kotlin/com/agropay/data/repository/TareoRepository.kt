package com.agropay.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.agropay.data.local.Database
import com.agropay.db.Tareo_employees
import com.agropay.db.Tareos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class TareoRepository {
    private val db = Database.instance
    private val tareosQueries = db.tareosQueries
    private val employeesQueries = db.tareoEmployeesQueries

    fun getAllTareos(): Flow<List<Tareos>> {
        return tareosQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    suspend fun createTareo(
        id: String,
        supervisorDocumentNumber: String,
        laborId: String,
        loteId: String? = null, // NULL para tareos administrativos
        scannerDocumentNumber: String? = null,
        isClosing: Boolean = false
    ): Unit = withContext(Dispatchers.Default) {
        tareosQueries.insert(
            id = id,
            supervisor_employee_document_number = supervisorDocumentNumber,
            labor_id = laborId,
            lote_id = loteId,
            scanner_employee_document_number = scannerDocumentNumber,
            is_closing = isClosing,
            created_at = Clock.System.now().toEpochMilliseconds()
        )
    }

    suspend fun getTareoById(id: String): Tareos? = withContext(Dispatchers.Default) {
        tareosQueries.getById(id).executeAsOneOrNull()
    }

    fun getNotSyncedTareos(): Flow<List<Tareos>> {
        return tareosQueries.getNotSynced()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    suspend fun markTareoAsSynced(id: String) = withContext(Dispatchers.Default) {
        tareosQueries.markAsSynced(id)
    }

    /**
     * Elimina un tareo de la base de datos
     * IMPORTANTE: Solo debe usarse para tareos no sincronizados
     */
    suspend fun deleteTareo(id: String) = withContext(Dispatchers.Default) {
        tareosQueries.deleteById(id)
    }

    suspend fun addEmployeeToTareo(
        id: String,
        tareoId: String,
        employeeDocumentNumber: String,
        startTime: String? = null,
        endTime: String? = null,
        entryMotiveId: String? = null,
        exitMotiveId: String? = null,
        actualHours: Double? = null,
        paidHours: Double? = null
    ): Unit = withContext(Dispatchers.Default) {
        employeesQueries.insert(
            id = id,
            tareo_id = tareoId,
            employee_document_number = employeeDocumentNumber,
            start_time = startTime,
            end_time = endTime,
            entry_motive_id = entryMotiveId,
            exit_motive_id = exitMotiveId,
            actual_hours = actualHours,
            paid_hours = paidHours
        )
    }

    fun getEmployeesByTareoId(tareoId: String): Flow<List<Tareo_employees>> {
        return employeesQueries.getByTareoId(tareoId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    fun getNotSyncedEmployeesByTareoId(tareoId: String): Flow<List<Tareo_employees>> {
        return employeesQueries.getNotSyncedByTareoId(tareoId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    suspend fun markTareoEmployeeAsSynced(id: String) = withContext(Dispatchers.Default) {
        employeesQueries.markAsSynced(id)
    }

    /**
     * Marca un tareo como cerrado con motivo
     */
    suspend fun markTareoAsClosing(id: String, closingMotiveId: String) = withContext(Dispatchers.Default) {
        tareosQueries.markAsClosing(closingMotiveId, id)
    }

    /**
     * Actualiza la salida de un empleado (end_time y exit_motive_id)
     */
    suspend fun updateEmployeeExit(
        tareoId: String,
        employeeDocumentNumber: String,
        endTime: String,
        exitMotiveId: String
    ) = withContext(Dispatchers.Default) {
        employeesQueries.updateEmployeeExit(endTime, exitMotiveId, tareoId, employeeDocumentNumber)
    }

    /**
     * Actualiza la salida de múltiples empleados y cierra el tareo
     */
    suspend fun bulkExitEmployeesAndCloseTareo(
        tareoId: String,
        employees: List<String>, // Lista de DNI de empleados
        exitTime: String,
        exitMotiveId: String,
        closingMotiveId: String
    ) = withContext(Dispatchers.Default) {
        // Actualizar salida de cada empleado
        employees.forEach { employeeDni ->
            employeesQueries.updateEmployeeExit(exitTime, exitMotiveId, tareoId, employeeDni)
        }
        // Cerrar el tareo
        tareosQueries.markAsClosing(closingMotiveId, tareoId)
    }
}
