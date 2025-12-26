package com.agropay.core.organization.web;

import com.agropay.core.organization.application.usecase.IPositionUseCase;
import com.agropay.core.organization.model.position.*;
import com.agropay.core.shared.generic.controller.ICrudController;
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
@RequestMapping("/v1/positions")
@RequiredArgsConstructor
@Tag(name = "Gestión de Posiciones", description = "Operaciones para administrar los cargos y posiciones dentro de la organización.")
@Slf4j
public class PositionController  {

    private final IPositionUseCase positionService;

    @PostMapping
    @Operation(summary = "Crear una nueva posición", description = "Registra una nueva posición o cargo. Permite definir si requiere un supervisor y qué posición debe tener dicho supervisor.")
    public ResponseEntity<ApiResult<CommandPositionResponse>> create(@RequestBody @Valid CreatePositionRequest request) {
        CommandPositionResponse response = positionService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar una posición existente", description = "Actualiza los datos de una posición, identificada por su ID público.")
    public ResponseEntity<ApiResult<CommandPositionResponse>> update(@RequestBody @Valid UpdatePositionRequest request, @PathVariable("publicId") UUID publicId) {
        CommandPositionResponse response = positionService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar una posición", description = "Elimina una posición del sistema por su ID público. La operación fallará si la posición tiene empleados asignados.")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId) {
        positionService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar una posición por ID", description = "Obtiene los detalles completos de una posición específica a través de su ID público.")
    public ResponseEntity<ApiResult<PositionDetailsDTO>> findById(@PathVariable(name = "publicId") UUID publicId) {
        PositionDetailsDTO response = positionService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/command/{publicId}")
    @Operation(summary = "Obtener una posición para edición", description = "Obtiene los datos de una posición, incluyendo IDs públicos de entidades relacionadas, para ser utilizada en formularios de edición.")
    public ResponseEntity<ApiResult<CommandPositionResponse>> getCommandResponseById(@PathVariable(name = "publicId") UUID publicId) {
        CommandPositionResponse response = positionService.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de posiciones", description = "Devuelve una lista paginada de todas las posiciones. Permite la búsqueda por nombre.")
    public ResponseEntity<ApiResult<PagedResult<PositionListDTO>>> getPagedList(
            @ModelAttribute @Validated PositionPageableRequest request) {
        log.info("REST request to get all Positions with request: {}", request);
        PagedResult<PositionListDTO> response = positionService.findAllPaged(request.getCode(), request.getName(), request.getAreaPublicId(), request.toPageable());
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/select-options")
    @Operation(summary = "Obtener posiciones para campos de selección", description = "Devuelve una lista simplificada de posiciones (ID y nombre) para ser utilizada en componentes de UI. Permite filtrar por el ID público del área.")
    public ResponseEntity<ApiResult<List<PositionSelectOptionDTO>>> getSelectOptions(@RequestParam(required = false) UUID areaPublicId) {
        List<PositionSelectOptionDTO> response = positionService.getSelectOptions(areaPublicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/manager-select-options")
    @Operation(summary = "Obtener todas las posiciones para selección de manager", description = "Devuelve una lista de todas las posiciones disponibles para ser asignadas como manager, sin restricción de área. Útil para roles transversales como CEO.")
    public ResponseEntity<ApiResult<List<PositionSelectOptionDTO>>> getManagerSelectOptions() {
        List<PositionSelectOptionDTO> response = positionService.getManagerSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/sync")
    @Operation(summary = "Obtener todas las posiciones para sincronización móvil", description = "Retorna todas las posiciones con publicId y name para almacenar en la base de datos local del móvil.")
    public ResponseEntity<ApiResult<List<PositionSyncResponse>>> findAllForSync() {
        List<PositionSyncResponse> response = positionService.findAllForSync();
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
