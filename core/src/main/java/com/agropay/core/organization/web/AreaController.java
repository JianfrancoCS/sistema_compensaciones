package com.agropay.core.organization.web;

import com.agropay.core.organization.application.usecase.IAreaUseCase;
import com.agropay.core.organization.model.area.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/areas")
@RequiredArgsConstructor
@Tag(name = "Gestión de Áreas", description = "Operaciones para administrar las áreas de la organización, como campos, oficinas o almacenes.")
public class AreaController  {

    private final IAreaUseCase areaService;

    @PostMapping
    @Operation(summary = "Crear una nueva área", description = "Registra una nueva área en el sistema. Requiere un nombre único.")
    public ResponseEntity<ApiResult<CommandAreaResponse>> create(@RequestBody @Valid CreateAreaRequest request) {
        CommandAreaResponse response = areaService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un área existente", description = "Actualiza los datos de un área existente, identificada por su ID público.")
    public ResponseEntity<ApiResult<CommandAreaResponse>> update(@RequestBody @Valid UpdateAreaRequest request, @PathVariable("publicId") UUID publicId) {
        CommandAreaResponse response = areaService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un área", description = "Elimina un área del sistema por su ID público. La operación fallará si el área tiene posiciones asociadas.")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId) {
        areaService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar un área por ID", description = "Obtiene los detalles completos de un área específica a través de su ID público.")
    public ResponseEntity<ApiResult<AreaDetailsDTO>> findById(@PathVariable(name = "publicId") UUID publicId) {
        AreaDetailsDTO response = areaService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de áreas", description = "Devuelve una lista paginada de todas las áreas. Permite la búsqueda por nombre y la ordenación por diferentes campos.")
    public ResponseEntity<ApiResult<PagedResult<AreaListDTO>>> getPagedList(
            @Valid @ModelAttribute AreaPageableRequest request) {
        PagedResult<AreaListDTO> response = areaService.findAllPaged(request.getName(), request.toPageable());
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/select-options")
    @Operation(summary = "Obtener áreas para campos de selección", description = "Devuelve una lista simplificada de áreas (ID y nombre) para ser utilizada en componentes de UI como menús desplegables.")
    public ResponseEntity<ApiResult<List<AreaSelectOptionDTO>>> getSelectOptions() {
        List<AreaSelectOptionDTO> response = areaService.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
