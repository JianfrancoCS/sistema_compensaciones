package com.agropay.core.payroll.controller;

import com.agropay.core.payroll.service.usecase.IPayrollConfigurationMasterService;
import com.agropay.core.payroll.model.masterconfig.CommandPayrollConfigurationResponse;
import com.agropay.core.payroll.model.masterconfig.CreatePayrollConfigurationRequest;
import com.agropay.core.payroll.model.masterconfig.PayrollConfigurationConceptAssignmentDTO;
import com.agropay.core.payroll.model.masterconfig.UpdateConceptAssignmentsRequest;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payroll-configurations")
@RequiredArgsConstructor
@Tag(name = "Gestión de Configuración Maestra de Planilla", description = "Endpoints para la configuración maestra de conceptos de planilla.")
public class PayrollConfigurationMasterController {

    private final IPayrollConfigurationMasterService payrollConfigurationMasterService;

    @Operation(
        summary = "Crear una configuración maestra de planilla",
        description = "Crea una nueva configuración maestra. Si ya existe una activa, la anterior es archivada (soft-delete)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración creada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. un ID de concepto no existe).")
    })
    @PostMapping
    public ResponseEntity<ApiResult<CommandPayrollConfigurationResponse>> createConfiguration(@RequestBody @Valid CreatePayrollConfigurationRequest request) {
        CommandPayrollConfigurationResponse response = payrollConfigurationMasterService.createConfiguration(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Obtener la configuración maestra de planilla activa",
        description = "Obtiene la única configuración maestra de planilla que se encuentra activa en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración obtenida exitosamente."),
        @ApiResponse(responseCode = "400", description = "Regla de negocio: No se encontró ninguna configuración activa.")
    })
    @GetMapping
    public ResponseEntity<ApiResult<CommandPayrollConfigurationResponse>> getActiveConfiguration() {
        CommandPayrollConfigurationResponse response = payrollConfigurationMasterService.getActiveConfiguration();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Obtener asignaciones de conceptos de la configuración activa",
        description = "Lista todos los conceptos del sistema, marcando cuáles están asignados a la configuración maestra activa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente."),
        @ApiResponse(responseCode = "400", description = "Regla de negocio: No se encontró ninguna configuración activa.")
    })
    @GetMapping("/concepts")
    public ResponseEntity<ApiResult<List<PayrollConfigurationConceptAssignmentDTO>>> getConceptAssignmentsForActiveConfiguration() {
        List<PayrollConfigurationConceptAssignmentDTO> response = payrollConfigurationMasterService.getConceptAssignmentsForActiveConfiguration();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Actualizar los conceptos asignados a la configuración activa",
        description = "Actualiza la lista de conceptos asignados. Si la configuración actual ya está en uso, se archiva y se crea una nueva versión con los cambios."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conceptos actualizados exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o una regla de negocio fue violada (ej. no hay configuración activa o un ID de concepto no existe)."),
    })
    @PutMapping("/concepts")
    public ResponseEntity<ApiResult<List<PayrollConfigurationConceptAssignmentDTO>>> updateConceptAssignmentsForActiveConfiguration(
        @RequestBody @Valid UpdateConceptAssignmentsRequest request
    ) {
        List<PayrollConfigurationConceptAssignmentDTO> response = payrollConfigurationMasterService.updateConceptAssignmentsForActiveConfiguration(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Eliminar la configuración maestra de planilla activa",
        description = "Realiza un borrado lógico de la configuración activa. No se permite si está siendo referenciada por planillas existentes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración eliminada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Regla de negocio: No se encontró una configuración activa para eliminar o la configuración está en uso y no puede ser borrada.")
    })
    @DeleteMapping
    public ResponseEntity<ApiResult<Void>> deleteActiveConfiguration() {
        payrollConfigurationMasterService.deleteActiveConfiguration();
        return ResponseEntity.ok(ApiResult.success(null));
    }
}
