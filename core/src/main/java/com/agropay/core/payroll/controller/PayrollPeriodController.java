package com.agropay.core.payroll.controller;

import com.agropay.core.payroll.service.usecase.IPayrollPeriodService;
import com.agropay.core.payroll.model.period.CommandPayrollPeriodResponse;
import com.agropay.core.payroll.model.period.CreatePayrollPeriodRequest;
import com.agropay.core.payroll.model.period.PayrollPeriodSelectOptionDTO;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/periods")
@RequiredArgsConstructor
@Tag(name = "Gestión de Períodos de Planilla", description = "Endpoints para crear, listar y eliminar los períodos de pago (quincenas, meses, etc.)")
public class PayrollPeriodController {

    private final IPayrollPeriodService payrollPeriodService;

    @Operation(
        summary = "Listar todos los períodos de planilla",
        description = "Obtiene una lista de todos los períodos de planilla que han sido creados en el sistema, ordenados cronológicamente."
    )
    @GetMapping
    public ResponseEntity<ApiResult<List<CommandPayrollPeriodResponse>>> getAllPeriods() {
        List<CommandPayrollPeriodResponse> periods = payrollPeriodService.getAllPeriods();
        return ResponseEntity.ok(ApiResult.success(periods));
    }

    @Operation(
        summary = "Crear un nuevo período de planilla",
        description = "Crea un nuevo período. Si no se especifica 'explicitStartDate', el sistema lo calcula a partir del último período existente. Requiere que la empresa principal esté configurada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Período creado exitosamente."),
        @ApiResponse(responseCode = "400", description = "La fecha de inicio explícita no es válida."),
        @ApiResponse(responseCode = "404", description = "La configuración de la empresa principal no fue encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto al crear el período (ej. no hay período anterior para continuar).")
    })
    @PostMapping
    public ResponseEntity<ApiResult<CommandPayrollPeriodResponse>> createPayrollPeriod(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Opcionalmente, especificar una fecha de inicio. Si es nulo, se calcula automáticamente.")
        @RequestBody @Valid CreatePayrollPeriodRequest request
    ) {
        CommandPayrollPeriodResponse newPeriod = payrollPeriodService.createPeriod(request);
        return ResponseEntity.ok(ApiResult.success(newPeriod));
    }

    @Operation(
        summary = "Obtener períodos como opciones para un select",
        description = "Devuelve una lista simplificada de períodos, ideal para rellenar menús desplegables. Incluye un indicador para saber si el período ya tiene planillas generadas."
    )
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<PayrollPeriodSelectOptionDTO>>> getSelectOptions() {
        List<PayrollPeriodSelectOptionDTO> selectOptions = payrollPeriodService.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(selectOptions));
    }

    @Operation(
        summary = "Eliminar un período de planilla",
        description = "Realiza un borrado lógico (soft delete) de un período. Solo es posible si el período no tiene ninguna planilla asociada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Período eliminado exitosamente."),
        @ApiResponse(responseCode = "404", description = "El período con el UUID especificado no fue encontrado."),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar el período porque tiene planillas asociadas.")
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> deletePayrollPeriod(
        @Parameter(description = "Identificador UUID del período a eliminar.", required = true)
        @PathVariable UUID publicId
    ) {
        payrollPeriodService.deletePeriod(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }
}
