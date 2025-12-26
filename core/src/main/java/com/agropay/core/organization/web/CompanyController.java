package com.agropay.core.organization.web;

import com.agropay.core.organization.application.usecase.ICompanyUseCase;
import com.agropay.core.organization.model.company.*;
import com.agropay.core.sunat.models.CompanyExternalInfo;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/company")
@RequiredArgsConstructor
@Tag(name = "Gestión de Empresa Principal", description = "Endpoints para administrar la información y configuración de la empresa principal.")
@Validated
public class CompanyController {
    private final ICompanyUseCase companyService;

    @Operation(
        summary = "Obtener datos de la empresa principal",
        description = "Devuelve la información completa de la empresa que ha sido configurada como la principal en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos de la empresa obtenidos exitosamente (el resultado puede ser nulo si no hay empresa configurada)." )
    })
    @GetMapping()
    public ResponseEntity<ApiResult<CompanyDTO>> getCompany() {
        Optional<CompanyDTO> companyOptional = companyService.getPrimaryCompany();
        return ResponseEntity.ok(ApiResult.success(companyOptional.orElse(null)));
    }

    @Operation(
        summary = "Consultar RUC en servicio externo",
        description = "Realiza una búsqueda del RUC proporcionado en un servicio externo (SUNAT) para obtener información de la empresa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente."),
        @ApiResponse(responseCode = "502", description = "Error del servicio externo: el RUC no fue encontrado o el servicio no está disponible.")
    })
    @GetMapping("/external-lookup/{ruc}")
    public ResponseEntity<ApiResult<CompanyExternalInfo>> getExternalCompany(
        @Parameter(description = "Número de RUC a consultar (11 dígitos).", required = true)
        @PathVariable @Pattern(regexp = "^\\d{11}$", message = "{company.ruc.pattern}")
        String ruc) {
        CompanyExternalInfo externalInfo = companyService.fetchExternalCompany(ruc);
        return ResponseEntity.ok(ApiResult.success(externalInfo));
    }

    @Operation(
        summary = "Registrar la empresa principal",
        description = "Crea la empresa principal en el sistema. Esta operación solo puede realizarse una vez."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa creada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. campos nulos o con formato incorrecto)."),
        @ApiResponse(responseCode = "409", description = "Conflicto: ya existe una empresa principal configurada.")
    })
    @PostMapping()
    public ResponseEntity<ApiResult<CommandCompanyResponse>> createCompany(@RequestBody @Valid CreateCompanyRequest createCompanyRequest) {
        CommandCompanyResponse commandCompanyResponse = companyService.create(createCompanyRequest);
        return ResponseEntity.ok(ApiResult.success(commandCompanyResponse));
    }

    @Operation(
        summary = "Actualizar la empresa (Reemplazo completo)",
        description = "Actualiza todos los datos de la empresa principal. Requiere que se envíen todos los campos, ya que reemplaza el recurso completo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "No se encontró una empresa principal para actualizar.")
    })
    @PutMapping()
    public ResponseEntity<ApiResult<CommandCompanyResponse>> updateCompany(@RequestBody @Valid UpdateCompanyRequest updateCompanyRequest) {
        CommandCompanyResponse commandCompanyResponse = companyService.updatePrimaryCompany(updateCompanyRequest);
        return ResponseEntity.ok(ApiResult.success(commandCompanyResponse));
    }

    @Operation(
        summary = "Actualizar horas máximas de trabajo (Parcial)",
        description = "Actualiza únicamente el campo de horas máximas de trabajo mensuales de la empresa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campo actualizado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "No se encontró una empresa principal para actualizar.")
    })
    @PatchMapping("/max-monthly-working-hours")
    public ResponseEntity<ApiResult<CommandCompanyResponse>> updateMaxMonthlyWorkingHours(@RequestBody @Valid UpdateMaxMonthlyWorkingHoursRequest request) {
        CommandCompanyResponse response = companyService.updateMaxMonthlyWorkingHours(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Actualizar intervalo de días de pago (Parcial)",
        description = "Actualiza únicamente el campo del intervalo de días de pago de la empresa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campo actualizado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "No se encontró una empresa principal para actualizar.")
    })
    @PatchMapping("/payment-interval-days")
    public ResponseEntity<ApiResult<CommandCompanyResponse>> updatePaymentIntervalDays(@RequestBody @Valid UpdatePaymentIntervalDaysRequest request) {
        CommandCompanyResponse response = companyService.updatePaymentIntervalDays(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Actualizar día de declaración de planilla (Parcial)",
        description = "Actualiza únicamente el día del mes en que se debe declarar la planilla."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campo actualizado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "No se encontró una empresa principal para actualizar.")
    })
    @PatchMapping("/payroll-declaration-day")
    public ResponseEntity<ApiResult<CommandCompanyResponse>> updatePayrollDeclarationDay(@RequestBody @Valid UpdatePayrollDeclarationDayRequest request) {
        CommandCompanyResponse response = companyService.updatePayrollDeclarationDay(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
