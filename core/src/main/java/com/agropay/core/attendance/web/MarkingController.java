package com.agropay.core.attendance.web;

import com.agropay.core.attendance.application.usecase.IMarkingUseCase;
import com.agropay.core.attendance.model.marking.EmployeeMarkRequest;
import com.agropay.core.attendance.model.marking.ExternalMarkRequest;
import com.agropay.core.attendance.model.marking.MarkingResponse;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(MarkingController.BASE_URL)
@RequiredArgsConstructor
@Validated
@Tag(name = "Gestión de asistencia", description = "API para gestión integral de asistencias, marcaciones y razones de marcado")
public class MarkingController {
    public static final String BASE_URL = "/v1/attendance/markings";
    private final IMarkingUseCase markingUseCase;

    @PostMapping("/employee")
    @Operation(
        summary = "Marcar asistencia de empleado",
        description = "Endpoint principal para marcado de empleados activos. " +
                     "Requiere especificar el tipo de marcación (TRABAJO, TRAMITE, etc.) y si es entrada o salida. " +
                     "Valida que el empleado pertenezca a la subsidiaria y previene marcaciones duplicadas. " +
                     "Si falla, indica al guardia usar el endpoint de externos."
    )
    public ResponseEntity<ApiResult<MarkingResponse>> markEmployee(
        @Valid @RequestBody EmployeeMarkRequest request
    ) {
        MarkingResponse response = markingUseCase.markEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResult.success(response));
    }

    @PostMapping("/external")
    @Operation(
        summary = "Marcar asistencia de persona externa",
        description = "Endpoint de contingencia para personas no empleadas. " +
                     "Requiere seleccionar una razón específica (VISITA, SUPERVISION, etc.) y si es entrada o salida. " +
                     "Crea la persona automáticamente si no existe. Previene marcaciones duplicadas."
    )
    public ResponseEntity<ApiResult<MarkingResponse>> markExternal(
        @Valid @RequestBody ExternalMarkRequest request
    ) {
        MarkingResponse response = markingUseCase.markExternal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResult.success(response));
    }
}