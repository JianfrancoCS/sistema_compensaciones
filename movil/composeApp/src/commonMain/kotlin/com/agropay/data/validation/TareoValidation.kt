package com.agropay.data.validation

import com.agropay.db.Tareos
import com.agropay.db.Tareo_employees

/**
 * Resultado de validación
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * Validador para tareos y empleados
 */
object TareoValidation {

    /**
     * Valida que un tareo tenga todos los datos necesarios antes de sincronizar
     * NOTA: lote_id puede ser null para tareos administrativos
     */
    fun validateTareoForSync(tareo: Tareos): ValidationResult {
        // Validar que tenga supervisor
        if (tareo.supervisor_employee_document_number.isBlank()) {
            return ValidationResult.Invalid("El tareo debe tener un supervisor asignado")
        }

        // Validar que tenga labor
        if (tareo.labor_id.isBlank()) {
            return ValidationResult.Invalid("El tareo debe tener una labor asignada")
        }

        // lote_id puede ser null para tareos administrativos, no se valida

        return ValidationResult.Valid
    }

    /**
     * Valida que un empleado de tareo tenga los datos mínimos necesarios
     */
    fun validateTareoEmployee(employee: Tareo_employees): ValidationResult {
        // Validar DNI
        if (employee.employee_document_number.isBlank()) {
            return ValidationResult.Invalid("El empleado debe tener un número de documento")
        }

        // Validar formato de DNI (8 dígitos)
        if (!employee.employee_document_number.matches(Regex("^\\d{8}$"))) {
            return ValidationResult.Invalid("El número de documento debe tener 8 dígitos")
        }

        // Si tiene hora de entrada, debe tener motivo de entrada
        if (!employee.start_time.isNullOrBlank() && employee.entry_motive_id.isNullOrBlank()) {
            return ValidationResult.Invalid("Si hay hora de entrada, debe haber un motivo de entrada")
        }

        // Si tiene hora de salida, debe tener motivo de salida
        if (!employee.end_time.isNullOrBlank() && employee.exit_motive_id.isNullOrBlank()) {
            return ValidationResult.Invalid("Si hay hora de salida, debe haber un motivo de salida")
        }

        // Validar formato de hora (HH:mm o ISO datetime)
        // Acepta tanto HH:mm como formato ISO completo
        if (!employee.start_time.isNullOrBlank()) {
            val timePattern = Regex("^(\\d{2}:\\d{2}|\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})")
            if (!timePattern.containsMatchIn(employee.start_time)) {
                return ValidationResult.Invalid("La hora de entrada debe tener formato HH:mm o ISO")
            }
        }

        if (!employee.end_time.isNullOrBlank()) {
            val timePattern = Regex("^(\\d{2}:\\d{2}|\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})")
            if (!timePattern.containsMatchIn(employee.end_time)) {
                return ValidationResult.Invalid("La hora de salida debe tener formato HH:mm o ISO")
            }
        }

        return ValidationResult.Valid
    }

    /**
     * Valida que un tareo tenga al menos un empleado antes de sincronizar
     */
    fun validateTareoHasEmployees(employees: List<Tareo_employees>): ValidationResult {
        if (employees.isEmpty()) {
            return ValidationResult.Invalid("El tareo debe tener al menos un empleado")
        }

        // Validar cada empleado
        employees.forEach { employee ->
            val result = validateTareoEmployee(employee)
            if (result is ValidationResult.Invalid) {
                return result
            }
        }

        return ValidationResult.Valid
    }

    /**
     * Valida que todos los datos necesarios estén presentes antes de crear un tareo
     * NOTA: loteId puede ser null para tareos administrativos
     */
    fun validateTareoCreation(
        laborId: String,
        loteId: String?,
        supervisorDocumentNumber: String
    ): ValidationResult {
        if (laborId.isBlank()) {
            return ValidationResult.Invalid("Debe seleccionar una labor")
        }

        // loteId puede ser null para tareos administrativos, no se valida

        if (supervisorDocumentNumber.isBlank()) {
            return ValidationResult.Invalid("Debe especificar un supervisor")
        }

        if (!supervisorDocumentNumber.matches(Regex("^\\d{8}$"))) {
            return ValidationResult.Invalid("El DNI del supervisor debe tener 8 dígitos")
        }

        return ValidationResult.Valid
    }
}

