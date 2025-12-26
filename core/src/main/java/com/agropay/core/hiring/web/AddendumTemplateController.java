package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IAddendumTemplateUseCase;
import com.agropay.core.hiring.model.addendumtemplate.*;
import com.agropay.core.states.models.StateSelectOptionDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AddendumTemplateController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Plantillas de Adenda", description = "API para la gestión de plantillas de adenda")
public class AddendumTemplateController {

    public static final String BASE_URL = "/v1/hiring/addendum-templates";

    private final IAddendumTemplateUseCase addendumTemplateUseCase;

    @Operation(summary = "Crear una nueva plantilla de adenda")
    @PostMapping
    public ResponseEntity<ApiResult<CommandAddendumTemplateResponse>> create(@Valid @RequestBody CreateAddendumTemplateRequest request) {
        log.info("REST request to create AddendumTemplate: {}", request);
        CommandAddendumTemplateResponse response = addendumTemplateUseCase.create(request);
        return new ResponseEntity<>(ApiResult.success(response, "Plantilla de adenda creada exitosamente."), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una plantilla de adenda existente")
    @PutMapping("/{publicId}")
    public ResponseEntity<ApiResult<CommandAddendumTemplateResponse>> update(@PathVariable UUID publicId, @Valid @RequestBody UpdateAddendumTemplateRequest request) {
        log.info("REST request to update AddendumTemplate {}: {}", publicId, request);
        CommandAddendumTemplateResponse response = addendumTemplateUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response, "Plantilla de adenda actualizada exitosamente."));
    }

    @Operation(summary = "Eliminar una plantilla de adenda por ID público")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable UUID publicId) {
        log.info("REST request to delete AddendumTemplate: {}", publicId);
        addendumTemplateUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null, "Plantilla de adenda eliminada exitosamente."));
    }

    @Operation(summary = "Obtener plantilla de adenda para formulario por ID público")
    @GetMapping("/{publicId}/command")
    public ResponseEntity<ApiResult<CommandAddendumTemplateResponse>> getCommandResponse(@PathVariable UUID publicId) {
        log.info("REST request to get command response for AddendumTemplate: {}", publicId);
        CommandAddendumTemplateResponse response = addendumTemplateUseCase.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Obtener todas las plantillas de adenda paginadas y filtradas")
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<AddendumTemplateListDTO>>> getAll(
            @ModelAttribute @Validated AddendumTemplatePageableRequest request) {
        log.info("REST request to get all AddendumTemplates with request: {}", request);
        PagedResult<AddendumTemplateListDTO> result = addendumTemplateUseCase.findAllPaged(request.getCode(), request.getName(), request.getAddendumTypePublicId(), request.toPageable());
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(summary = "Obtener opciones de selección para plantillas de adenda")
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<AddendumTemplateSelectOptionDTO>>> getSelectOptions(@RequestParam(required = false) UUID addendumTypePublicId) {
        log.info("REST request to get select options for AddendumTemplates with addendumTypePublicId: {}", addendumTypePublicId);
        List<AddendumTemplateSelectOptionDTO> options = addendumTemplateUseCase.getSelectOptions(addendumTypePublicId);
        return ResponseEntity.ok(ApiResult.success(options));
    }

    @GetMapping("/states/select-options")
    @Operation(summary = "Obtener estados para campos de selección de plantillas de adenda")
    public ResponseEntity<ApiResult<List<StateSelectOptionDTO>>> getStatesForSelect() {
        List<StateSelectOptionDTO> response = addendumTemplateUseCase.getStatesSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
