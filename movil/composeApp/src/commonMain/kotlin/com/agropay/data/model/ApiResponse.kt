package com.agropay.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ApiResult<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errors: Map<String, String>? = null,
    val timeStamp: String? = null
)

@Serializable
data class SubsidiarySyncResponse(
    val publicId: String,
    val name: String
)

@Serializable
data class LaborSyncResponse(
    val publicId: String,
    val name: String,
    val description: String? = null,
    val isPiecework: Boolean = false,
    val laborUnitName: String? = null,
    val minTaskRequirement: Double? = null,
    val basePrice: Double? = null
)

@Serializable
data class TareoMotiveSyncResponse(
    val publicId: String,
    val name: String,
    val description: String? = null,
    val isPaid: Boolean = false
)

@Serializable
data class PositionSyncResponse(
    val publicId: String,
    val name: String
)

@Serializable
data class BatchTareoSyncRequest(
    val tareos: List<BatchTareoData>
)

@Serializable
data class BatchTareoData(
    val temporalId: String,
    val laborPublicId: String,
    val lotePublicId: String? = null, // Opcional: NULL para tareos administrativos
    val supervisorDocumentNumber: String,
    val scannerDocumentNumber: String? = null,
    val employees: List<BatchEmployeeData>,
    val isClosing: Boolean = false, // Flag para indicar si es cierre de tareo
    val closingMotivePublicId: String? = null // Motivo de cierre (requerido cuando isClosing = true)
)

@Serializable
data class BatchEmployeeData(
    val documentNumber: String,
    val entryTime: String, // ISO 8601
    val entryMotivePublicId: String? = null,
    val exitTime: String? = null,
    val exitMotivePublicId: String? = null
)

@Serializable
data class BatchTareoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BatchTareoResponseData? = null
)

@Serializable
data class BatchTareoResponseData(
    val items: List<BatchTareoItemResult>,
    val summary: BatchSummary,
    val status: String? = null // Campo opcional del backend, puede ser SUCCESS, ERROR, PARTIAL_SUCCESS
)

@Serializable
data class BatchTareoItemResult(
    val identifier: String, // temporalId
    val status: String, // SUCCESS, ERROR, PARTIAL_SUCCESS
    val data: BatchTareoResultData? = null,
    val errors: List<BatchErrorDetail>? = null,
    val success: Boolean
)

@Serializable
data class BatchTareoResultData(
    val tareoPublicId: String,
    val summary: BatchSummary? = null
)

@Serializable
data class BatchHarvestSyncRequest(
    val records: List<BatchHarvestRecordData>
)

@Serializable
data class BatchHarvestRecordData(
    val localId: String, // UUID generado en móvil
    val qrCodePublicId: String, // UUID del QR code escaneado
    val qrRollEmployeeId: String, // UUID de qr_roll_employees
    val tareoEmployeeId: String, // UUID de tareo_employees
    val quantity: Double,
    val scannedAt: Long // Timestamp epoch en segundos
)

@Serializable
data class BatchHarvestResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BatchHarvestResponseData? = null
)

@Serializable
data class BatchHarvestResponseData(
    val items: List<BatchHarvestItemResult>,
    val summary: BatchSummary
)

@Serializable
data class BatchHarvestItemResult(
    val identifier: String,
    val status: String,
    val data: BatchHarvestResultData? = null,
    val errors: List<BatchErrorDetail>? = null,
    val success: Boolean
)

@Serializable
data class BatchHarvestResultData(
    val publicId: String
)

@Serializable
data class BatchSummary(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val partialSuccess: Int
)

@Serializable
data class BatchErrorDetail(
    val field: String? = null,
    val message: String,
    val code: String? = null
)

@Serializable
data class EmployeeSearchResponse(
    val documentNumber: String,
    val names: String,
    val paternalLastname: String,
    val maternalLastname: String,
    val subsidiaryId: String,
    val positionId: String
)

@Serializable
data class PagedResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

@Serializable
data class LoteSyncResponse(
    val id: String, // publicId (UUID)
    val name: String,
    val hectareage: Double? = null,
    val subsidiaryId: String
)

@Serializable
data class UserInfoResponse(
    val code: String, // publicId del empleado (UUID)
    val documentNumber: String,
    val names: String,
    val paternalLastname: String,
    val maternalLastname: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val positionName: String,
    val subsidiaryId: String,
    val subsidiaryName: String
) {
    val fullName: String
        get() = "$names $paternalLastname${maternalLastname?.let { " $it" } ?: ""}"
}

@Serializable
data class EmployeeWithQrRollsResponse(
    val publicId: String, // UUID del empleado
    val documentNumber: String,
    val names: String,
    val paternalLastname: String,
    val maternalLastname: String,
    val positionName: String? = null,
    val qrRolls: List<QrRollWithCodesResponse> = emptyList()
) {
    val fullName: String
        get() = "$names $paternalLastname $maternalLastname"
}

@Serializable
data class QrRollWithCodesResponse(
    val qrRollEmployeeId: String, // UUID de la asignación
    val qrRollId: String, // UUID del roll
    val maxQrCodesPerDay: Int? = null,
    val qrCodes: List<QrCodeResponse> = emptyList()
)

@Serializable
data class QrCodeResponse(
    val publicId: String, // UUID del QR code
    val isUsed: Boolean = false,
    val isPrinted: Boolean = false
)

@Serializable
data class BatchQrRollAssignmentRequest(
    val assignments: List<QrRollAssignment>
)

@Serializable
data class QrRollAssignment(
    val qrCodePublicId: String, // UUID de un QR code del rollo
    val employeeDocumentNumber: String
)

@Serializable
data class BatchQrRollAssignmentResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BatchQrRollAssignmentData? = null
)

@Serializable
data class BatchQrRollAssignmentData(
    val summary: BatchSummary,
    val items: List<BatchQrRollAssignmentItem>
)

@Serializable
data class BatchQrRollAssignmentItem(
    val itemId: String, // employeeDocumentNumber
    val status: String, // SUCCESS, ERROR
    @Contextual
    val data: Any? = null,
    val errors: List<BatchErrorDetail> = emptyList()
)