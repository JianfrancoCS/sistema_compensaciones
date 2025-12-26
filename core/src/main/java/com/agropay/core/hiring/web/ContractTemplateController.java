package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IContractTemplateUseCase;
import com.agropay.core.hiring.model.contracttemplate.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ContractTemplateController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Plantillas de Contrato", description = "API para la gestión de plantillas de contrato")
public class ContractTemplateController {

    public static final String BASE_URL = "/v1/hiring/contract-templates";

    private final IContractTemplateUseCase contractTemplateUseCase;

    @Operation(summary = "Crear una nueva plantilla de contrato")
    @PostMapping
    public ResponseEntity<ApiResult<CommandContractTemplateResponse>> createContractTemplate(@Valid @RequestBody CreateContractTemplateRequest request) {
        log.info("Solicitud REST para crear Plantilla de Contrato: {}", request);
        CommandContractTemplateResponse response = contractTemplateUseCase.create(request);
        return new ResponseEntity<>(ApiResult.success(response, "Plantilla de contrato creada exitosamente"), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una plantilla de contrato existente")
    @PutMapping("/{publicId}")
    public ResponseEntity<ApiResult<CommandContractTemplateResponse>> updateContractTemplate(@PathVariable UUID publicId, @Valid @RequestBody UpdateContractTemplateRequest request) {
        log.info("Solicitud REST para actualizar Plantilla de Contrato {} : {}", publicId, request);
        CommandContractTemplateResponse response = contractTemplateUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response, "Plantilla de contrato actualizada exitosamente"));
    }

    @Operation(summary = "Eliminar una plantilla de contrato por ID público")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> deleteContractTemplate(@PathVariable UUID publicId) {
        log.info("Solicitud REST para eliminar Plantilla de Contrato: {}", publicId);
        contractTemplateUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null, "Plantilla de contrato eliminada exitosamente"));
    }

    @Operation(summary = "Obtener plantilla de contrato para respuesta de comando por ID público")
    @GetMapping("/{publicId}/command")
    public ResponseEntity<ApiResult<CommandContractTemplateResponse>> getContractTemplateCommandResponse(@PathVariable UUID publicId) {
        log.info("Solicitud REST para obtener respuesta de comando de Plantilla de Contrato: {}", publicId);
        CommandContractTemplateResponse response = contractTemplateUseCase.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener contenido de plantilla de contrato por ID público")
    @GetMapping("/{publicId}/content")
    public ResponseEntity<ApiResult<ContractTemplateContentDTO>> getContractTemplateContent(@PathVariable UUID publicId) {
        log.info("Solicitud REST para obtener contenido de Plantilla de Contrato: {}", publicId);
        ContractTemplateContentDTO response = contractTemplateUseCase.getContentByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener todas las plantillas de contrato con paginación y filtros")
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<ContractTemplateListDTO>>> getAllContractTemplates(
            @Valid @ModelAttribute ContractTemplatePageableRequest request
    ) {
        log.info("Solicitud REST para obtener todas las Plantillas de Contrato con filtros: {}", request);
        PagedResult<ContractTemplateListDTO> result = contractTemplateUseCase.findAllPaged(
                request.getCode(),
                request.getName(),
                request.getContractTypePublicId(),
                request.getStatePublicId(),
                request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(summary = "Obtener opciones de selección de plantilla de contrato por ID público de tipo de contrato")
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<ContractTemplateSelectOptionDTO>>> getContractTemplateSelectOptions(
            @RequestParam(required = false) UUID contractTypePublicId
    ) {
        log.info("Solicitud REST para obtener opciones de selección de Plantilla de Contrato para contractTypePublicId: {}", contractTypePublicId);
        List<ContractTemplateSelectOptionDTO> options = contractTemplateUseCase.getSelectOptions(contractTypePublicId);
        return ResponseEntity.ok(ApiResult.success(options));
    }

    @GetMapping("/states/select-options")
    @Operation(summary = "Obtener estados para campos de selección de plantillas de contrato", description = "Devuelve una lista simplificada de estados para ser utilizada en componentes de UI.")
    public ResponseEntity<ApiResult<List<StateSelectOptionDTO>>> getStatesForSelect() {
        List<StateSelectOptionDTO> response = contractTemplateUseCase.getStatesSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener variables de plantilla con información de validación dinámica",
               description = "Devuelve las variables de una plantilla incluyendo regex y reglas de validación para aplicar en el frontend")
    @GetMapping("/{publicId}/variables-with-validation")
    public ResponseEntity<ApiResult<List<ContractTemplateVariableWithValidationDTO>>> getVariablesWithValidation(@PathVariable UUID publicId) {
        log.info("REST request to get Contract Template variables with validation for publicId: {}", publicId);
        List<ContractTemplateVariableWithValidationDTO> result = contractTemplateUseCase.getVariablesWithValidationByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(result));
    }
}
