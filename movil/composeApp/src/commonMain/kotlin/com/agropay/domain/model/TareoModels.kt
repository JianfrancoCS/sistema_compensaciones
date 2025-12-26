package com.agropay.domain.model

import kotlinx.serialization.Serializable

/**
 * Modelos de datos basados en la API real
 * Ver: context/api-doc.json y context/feedback.txt
 */

// ============================================
// DTOs de Respuesta (desde API)
// ============================================

@Serializable
data class TareoListDTO(
    val publicId: String,
    val labor: LaborInfo,
    val subsidiary: SubsidiaryInfo,
    val createdBy: String,
    val createdAt: String  // ISO-8601 format
) {
    @Serializable
    data class LaborInfo(
        val publicId: String,
        val name: String,
        // TODO: Agregar cuando backend implemente
        // val requiresPedeteador: Boolean? = null,
        // val laborType: String? = null  // "DESTAJO" o "JORNAL"
    )

    @Serializable
    data class SubsidiaryInfo(
        val publicId: String,
        val name: String
    )
}

// TODO: Este modelo aún no existe en el backend
// Ver feedback.txt punto #1
@Serializable
data class TareoDetailDTO(
    val publicId: String,
    val labor: LaborInfo,
    val subsidiary: SubsidiaryInfo,
    val createdBy: String,
    val createdAt: String,
    val employees: List<TareoEmployeeDTO>,
    val pedeteador: PedeteadorDTO? = null,
    // TODO: Agregar cuando backend implemente
    // val supervisor: SupervisorInfo? = null
) {
    @Serializable
    data class LaborInfo(
        val publicId: String,
        val name: String,
        val requiresPedeteador: Boolean = false,
        val laborType: String? = null
    )

    @Serializable
    data class SubsidiaryInfo(
        val publicId: String,
        val name: String
    )

    // TODO: Agregar cuando backend implemente
    // @Serializable
    // data class SupervisorInfo(
    //     val publicId: String,
    //     val fullName: String
    // )
}

@Serializable
data class TareoEmployeeDTO(
    val publicId: String,
    val documentNumber: String,
    val fullName: String,
    val position: String,
    val entryTime: String,  // HH:mm format
    val entryMotive: MotiveInfo,
    val exitTime: String? = null,  // HH:mm format
    val exitMotive: MotiveInfo? = null
) {
    @Serializable
    data class MotiveInfo(
        val publicId: String,
        val name: String
    )
}

@Serializable
data class PedeteadorDTO(
    val publicId: String,
    val documentNumber: String,
    val fullName: String,
    val position: String
)

// ============================================
// Request Models (hacia API)
// ============================================

@Serializable
data class CreateTareoRequest(
    val laborPublicId: String,
    val subsidiaryPublicId: String
)

@Serializable
data class BulkEntryRequest(
    val employeePublicIds: List<String>,
    val entryTime: String,  // HH:mm:ss format
    val entryMotivePublicId: String? = null
)

@Serializable
data class BulkExitRequest(
    val employeePublicIds: List<String>,
    val exitTime: String,  // HH:mm:ss format
    val exitMotivePublicId: String? = null
)

@Serializable
data class IndividualExitRequest(
    val exitTime: String,  // HH:mm:ss format
    val exitMotivePublicId: String? = null
)

@Serializable
data class LateEntryRequest(
    val employeePublicId: String,
    val entryMotivePublicId: String? = null
)

// TODO: Este modelo aún no existe en el backend
// Ver feedback.txt punto #2
@Serializable
data class AssignPedeteadorRequest(
    val employeePublicId: String
)

// ============================================
// Models de UI/Estado Local
// ============================================

data class TareoUIModel(
    val id: String,
    val titulo: String,
    val fundo: String,
    val labor: String,
    val fecha: String,
    val supervisor: String,
    val estado: String,
    val empleados: List<EmpleadoUIModel>,
    val tipoLabor: TipoLabor,
    val pedeteador: EmpleadoUIModel? = null
)

data class EmpleadoUIModel(
    val id: String? = null,  // publicId del empleado (opcional si aún no está guardado)
    val dni: String,
    val nombre: String,
    val posicion: String,
    val motivoEntrada: String? = null,
    val horaEntrada: String? = null,
    val horaSalida: String? = null,
    val motivoSalida: String? = null,
    val esPedeteador: Boolean = false
)

enum class TipoLabor {
    JORNAL,    // Sin producción
    DESTAJO    // Con producción, requiere pedeteadores
}

// ============================================
// Response Wrapper (API estándar)
// ============================================

@Serializable
data class ApiResult<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errors: Map<String, String>? = null,
    val timeStamp: String
)

@Serializable
data class PagedResult<T>(
    val data: List<T>,
    val totalElements: Long,
    val pageNumber: Int,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

// ============================================
// Extensiones de conversión
// ============================================

/**
 * Convierte TareoDetailDTO a TareoUIModel para uso en la UI
 */
fun TareoDetailDTO.toUIModel(): TareoUIModel {
    return TareoUIModel(
        id = publicId,
        titulo = "${labor.name} - ${subsidiary.name}",
        fundo = subsidiary.name,
        labor = labor.name,
        fecha = createdAt.substringBefore('T'),  // Extraer solo la fecha
        supervisor = "TODO: Implementar en backend",  // Ver feedback.txt punto #6
        estado = "Activo",  // TODO: Agregar estados en backend
        empleados = employees.map { it.toUIModel() },
        tipoLabor = if (labor.requiresPedeteador) TipoLabor.DESTAJO else TipoLabor.JORNAL,
        pedeteador = pedeteador?.toUIModel()
    )
}

/**
 * Convierte TareoEmployeeDTO a EmpleadoUIModel
 */
fun TareoEmployeeDTO.toUIModel(): EmpleadoUIModel {
    return EmpleadoUIModel(
        id = publicId,
        dni = documentNumber,
        nombre = fullName,
        posicion = position,
        motivoEntrada = entryMotive.name,
        horaEntrada = entryTime,
        horaSalida = exitTime,
        motivoSalida = exitMotive?.name,
        esPedeteador = false
    )
}

/**
 * Convierte PedeteadorDTO a EmpleadoUIModel
 */
fun PedeteadorDTO.toUIModel(): EmpleadoUIModel {
    return EmpleadoUIModel(
        id = publicId,
        dni = documentNumber,
        nombre = fullName,
        posicion = position,
        motivoEntrada = null,  // Pedeteadores no tienen motivos
        horaEntrada = null,    // Pedeteadores no tienen horas
        horaSalida = null,
        motivoSalida = null,
        esPedeteador = true
    )
}