package com.agropay.ui.resources

/**
 * Archivo centralizado de strings de la aplicación
 * Similar a messages.properties en Spring Boot
 */
object Strings {
    // Generales
    object Common {
        const val back = "Volver"
        const val cancel = "Cancelar"
        const val confirm = "Confirmar"
        const val add = "Agregar"
        const val delete = "Eliminar"
        const val scan = "Escanear"
        const val save = "Guardar"
        const val edit = "Editar"
    }

    // Tareo - Pantallas
    object Tareo {
        const val title = "Tareos"
        const val detailTitle = "Detalle del Tareo"
        const val createTitle = "Crear Tareo"
        const val addEmployeesTitle = "Agregar Empleados"

        // Estados
        const val statusActive = "Activo"
        const val statusCompleted = "Completado"
        const val statusCancelled = "Cancelado"

        // Labels
        const val labelFundo = "Fundo:"
        const val labelLabor = "Labor:"
        const val labelSupervisor = "Supervisor:"
        const val labelStatus = "Estado:"
        const val labelDate = "Fecha:"

        // Botones
        const val btnAddEmployees = "Agregar"
        const val btnBulkExit = "Salida Grupal"
        const val btnCreateTareo = "Crear Tareo"
        const val btnAddMore = "Agregar Empleados"

        // Secciones
        const val sectionPedeteador = "Pedeteador"
        const val sectionEmployees = "Empleados"
        const val sectionGlobalMotive = "Motivo de Entrada Global"

        // Placeholders
        const val selectMotive = "Selecciona motivo"
        const val selectExitMotive = "Motivo de salida"

        // Contador
        fun employeeCount(count: Int) = "Empleados ($count)"
        fun pedeteadorCount(count: Int) = if (count > 0) "($count)" else ""
    }

    // Motivos de entrada/salida
    object Motives {
        // Entrada
        const val normalAttendance = "Asistencia normal"
        const val lateArrival = "Llegada tardía"
        const val doubleShift = "Doble turno"
        const val halfShift = "Medio turno"
        const val replacement = "Reemplazo"

        // Salida
        const val endOfDay = "Fin de jornada"
        const val earlyTermination = "Término anticipado"
        const val emergency = "Emergencia"
        const val permission = "Permiso"
    }

    // Diálogos
    object Dialogs {
        const val scanDniTitle = "Escanear DNI"
        const val scanDniMessage = "Simula el escaneo ingresando el número de DNI:"
        const val dniLabel = "Número de DNI"

        const val bulkExitTitle = "Salida Grupal"
        const val bulkExitMessage = "Selecciona el motivo de salida para todos los empleados sin salida registrada:"

        const val individualExitTitle = "Marcar Salida"
        const val employeeLabel = "Empleado:"
        const val dniLabelShort = "DNI:"
    }

    // Empleados
    object Employee {
        const val positionOperario = "Operario"
        const val positionPedeteador = "Pedeteador"
        const val defaultName = "Empleado"

        fun nameWithDni(dni: String) = "Empleado $dni"
        fun pedeteadorWithDni(dni: String) = "Pedeteador $dni"
    }

    // Marcas de hora
    object TimeMarks {
        const val entryPrefix = "E:"
        const val exitPrefix = "S:"
    }

    // Validaciones y Mensajes de error
    object Validation {
        const val requiredField = "Este campo es requerido"
        const val invalidDni = "DNI inválido"
        const val selectMotiveFirst = "Primero selecciona un motivo"
        const val noEmployeesAdded = "Debes agregar al menos un empleado"
    }

    // Producción
    object Production {
        const val title = "Producción"
        const val detailTitle = "Detalle de Producción"
    }
}