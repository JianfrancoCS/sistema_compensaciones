package com.agropay.core.auth.web;

import com.agropay.core.auth.application.usecase.IContainerUseCase;
import com.agropay.core.auth.model.container.*;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/containers")
@RequiredArgsConstructor
@Tag(name = "Gestión de Contenedores", description = "Operaciones para administrar los contenedores del sistema de seguridad.")
@Slf4j
public class ContainerController {

    private final IContainerUseCase containerService;

    @PostMapping
    @Operation(summary = "Crear un nuevo contenedor", description = "Registra un nuevo contenedor en el sistema.")
    public ResponseEntity<ApiResult<CommandContainerResponse>> create(@RequestBody @Valid CreateContainerRequest request) {
        CommandContainerResponse response = containerService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un contenedor existente", description = "Actualiza los datos de un contenedor, identificado por su ID público.")
    public ResponseEntity<ApiResult<CommandContainerResponse>> update(
            @RequestBody @Valid UpdateContainerRequest request,
            @PathVariable("publicId") UUID publicId) {
        CommandContainerResponse response = containerService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un contenedor", description = "Elimina un contenedor del sistema por su ID público.")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId) {
        containerService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar un contenedor por ID", description = "Obtiene los detalles completos de un contenedor específico a través de su ID público.")
    public ResponseEntity<ApiResult<ContainerDetailsDTO>> findById(@PathVariable(name = "publicId") UUID publicId) {
        ContainerDetailsDTO response = containerService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/command/{publicId}")
    @Operation(summary = "Obtener un contenedor para edición", description = "Obtiene los datos de un contenedor para ser utilizada en formularios de edición.")
    public ResponseEntity<ApiResult<CommandContainerResponse>> getCommandResponseById(
            @PathVariable(name = "publicId") UUID publicId) {
        CommandContainerResponse response = containerService.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de contenedores", description = "Devuelve una lista paginada de todos los contenedores. Permite la búsqueda por nombre.")
    public ResponseEntity<ApiResult<PagedResult<ContainerListDTO>>> getPagedList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "orderIndex") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
        log.info("REST request to get all Containers with query: {}", query);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        PagedResult<ContainerListDTO> response = containerService.findAllPaged(query, pageable);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}

