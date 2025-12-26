package com.agropay.core.attendance.web;

import com.agropay.core.attendance.application.usecase.IAttendanceUseCase;
import com.agropay.core.attendance.model.attendance.AttendanceListDTO;
import com.agropay.core.attendance.model.attendance.AttendancePageableRequest;
import com.agropay.core.attendance.model.attendance.AttendanceSummaryDTO;
import com.agropay.core.attendance.model.attendance.AttendanceSummaryRequest;
import com.agropay.core.attendance.model.attendance.AttendanceCountSummaryDTO;
import com.agropay.core.attendance.model.attendance.TodayAttendancePageableRequest;
import com.agropay.core.attendance.model.attendance.EmployeeAttendanceCheckDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(AttendanceController.BASE_URL)
@RequiredArgsConstructor
@Validated
@Tag(name = "Gestión de asistencia", description = "API para gestión integral de asistencias, marcaciones y razones de marcado")
public class AttendanceController {
    public static final String BASE_URL = "/v1/attendance";

    private final IAttendanceUseCase attendanceUseCase;


    @GetMapping
    @Operation(
        summary = "Listar asistencias paginadas",
        description = "Obtiene una lista paginada de todas las asistencias para una fecha específica. " +
                     "Permite filtrar por sucursal, número de documento y tipo de persona (empleado/externo)."
    )
    public ResponseEntity<ApiResult<PagedResult<AttendanceListDTO>>> getAttendanceList(
        @Valid @ModelAttribute AttendancePageableRequest request
    ) {
        PagedResult<AttendanceListDTO> result = attendanceUseCase.getAttendanceList(
            request.getMarkingDate(),
            request.getSubsidiaryPublicId(),
            request.getPersonDocumentNumber(),
            request.getIsEmployee(),
            request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/today/{subsidiaryPublicId}")
    @Operation(
        summary = "Listar asistencias del día actual por subsidiaria",
        description = "Obtiene todas las asistencias registradas el día de hoy para una subsidiaria específica. " +
                     "Usa la fecha actual del servidor para el filtrado."
    )
    public ResponseEntity<ApiResult<PagedResult<AttendanceListDTO>>> getTodayAttendanceBySubsidiary(
        @Parameter(description = "ID público de la subsidiaria", required = true)
        @PathVariable String subsidiaryPublicId,

        @Valid @ModelAttribute TodayAttendancePageableRequest request
    ) {
        PagedResult<AttendanceListDTO> result = attendanceUseCase.getAttendanceList(
            LocalDate.now(), // Fecha del servidor (hoy)
            java.util.UUID.fromString(subsidiaryPublicId),
            request.getPersonDocumentNumber(),
            request.getIsEmployee(),
            request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/summary/{subsidiaryPublicId}")
    @Operation(
        summary = "Obtener resumen flexible de asistencias por subsidiaria",
        description = "Proporciona un resumen simplificado y flexible de asistencias para una subsidiaria. " +
                     "Permite especificar la fecha y filtrar por tipo de persona (empleado/externo). " +
                     "Respuesta optimizada con formato: inside (adentro), outside (afuera), total. " +
                     "Ideal para dashboards y monitoreo en tiempo real con múltiples consultas."
    )
    public ResponseEntity<ApiResult<AttendanceCountSummaryDTO>> getFlexibleAttendanceSummary(
        @Parameter(description = "ID público de la subsidiaria", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String subsidiaryPublicId,

        @Valid @ModelAttribute AttendanceSummaryRequest request
    ) {
        AttendanceCountSummaryDTO result = attendanceUseCase.getFlexibleAttendanceSummary(
            java.util.UUID.fromString(subsidiaryPublicId),
            request.getDate(),
            request.getIsExternal()
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/check/employee/{documentNumber}")
    @Operation(
        summary = "Verificar asistencia de empleado por fecha y subsidiaria",
        description = "Verifica si un empleado específico marcó asistencia en una fecha y subsidiaria determinada. " +
                     "Diseñado especialmente para futuras funcionalidades de tareo. " +
                     "Valida que el empleado pertenezca a la subsidiaria especificada."
    )
    public ResponseEntity<ApiResult<EmployeeAttendanceCheckDTO>> checkEmployeeAttendanceByDate(
        @Parameter(description = "Número de documento del empleado", required = true)
        @PathVariable String documentNumber,

        @Parameter(description = "ID público de la subsidiaria", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @RequestParam java.util.UUID subsidiaryPublicId,

        @Parameter(description = "Fecha a verificar (formato YYYY-MM-DD)", required = true, example = "2024-09-15")
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkDate
    ) {
        EmployeeAttendanceCheckDTO result = attendanceUseCase.checkEmployeeAttendanceByDate(
            documentNumber, subsidiaryPublicId, checkDate
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }
}