package com.agropay.core.auth.web;

import com.agropay.core.auth.application.usecase.IElementUseCase;
import com.agropay.core.auth.model.element.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/elements")
@RequiredArgsConstructor
@Tag(name = "Gestión de Elementos", description = "Operaciones para administrar los elementos del sistema de seguridad.")
@Slf4j
public class ElementController {

    private final IElementUseCase elementService;

    @PostMapping
    @Operation(summary = "Crear un nuevo elemento", description = "Registra un nuevo elemento en el sistema.")
    public ResponseEntity<ApiResult<CommandElementResponse>> create(@RequestBody @Valid CreateElementRequest request) {
        CommandElementResponse response = elementService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un elemento existente", description = "Actualiza los datos de un elemento, identificado por su ID público.")
    public ResponseEntity<ApiResult<CommandElementResponse>> update(
            @RequestBody @Valid UpdateElementRequest request,
            @PathVariable("publicId") UUID publicId) {
        CommandElementResponse response = elementService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un elemento", description = "Elimina un elemento del sistema por su ID público.")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId) {
        elementService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar un elemento por ID", description = "Obtiene los detalles completos de un elemento específico a través de su ID público.")
    public ResponseEntity<ApiResult<ElementDetailsDTO>> findById(@PathVariable(name = "publicId") UUID publicId) {
        ElementDetailsDTO response = elementService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/command/{publicId}")
    @Operation(summary = "Obtener un elemento para edición", description = "Obtiene los datos de un elemento para ser utilizada en formularios de edición.")
    public ResponseEntity<ApiResult<CommandElementResponse>> getCommandResponseById(
            @PathVariable(name = "publicId") UUID publicId) {
        CommandElementResponse response = elementService.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de elementos", description = "Devuelve una lista paginada de todos los elementos. Permite la búsqueda por nombre y filtrar por contenedor.")
    public ResponseEntity<ApiResult<PagedResult<ElementListDTO>>> getPagedList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID containerPublicId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "orderIndex") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
        log.info("REST request to get all Elements with query: {}, containerPublicId: {}", query, containerPublicId);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        PagedResult<ElementListDTO> response = elementService.findAllPaged(query, containerPublicId, pageable);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}

