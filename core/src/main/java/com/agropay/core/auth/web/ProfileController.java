package com.agropay.core.auth.web;

import com.agropay.core.auth.application.usecase.IProfileUseCase;
import com.agropay.core.auth.model.profile.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Gestión de Perfiles", description = "Operaciones para administrar los perfiles del sistema de seguridad.")
@Slf4j
public class ProfileController {

    private final IProfileUseCase profileService;

    @PostMapping
    @Operation(summary = "Crear un nuevo perfil", description = "Registra un nuevo perfil en el sistema.")
    public ResponseEntity<ApiResult<CommandProfileResponse>> create(@RequestBody @Valid CreateProfileRequest request) {
        CommandProfileResponse response = profileService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un perfil existente", description = "Actualiza los datos de un perfil, identificado por su ID público.")
    public ResponseEntity<ApiResult<CommandProfileResponse>> update(
            @RequestBody @Valid UpdateProfileRequest request,
            @PathVariable("publicId") UUID publicId) {
        CommandProfileResponse response = profileService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un perfil", description = "Elimina un perfil del sistema por su ID público.")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId) {
        profileService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar un perfil por ID", description = "Obtiene los detalles completos de un perfil específico a través de su ID público.")
    public ResponseEntity<ApiResult<ProfileDetailsDTO>> findById(@PathVariable(name = "publicId") UUID publicId) {
        ProfileDetailsDTO response = profileService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/command/{publicId}")
    @Operation(summary = "Obtener un perfil para edición", description = "Obtiene los datos de un perfil para ser utilizada en formularios de edición.")
    public ResponseEntity<ApiResult<CommandProfileResponse>> getCommandResponseById(
            @PathVariable(name = "publicId") UUID publicId) {
        CommandProfileResponse response = profileService.getCommandResponseByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de perfiles", description = "Devuelve una lista paginada de todos los perfiles. Permite la búsqueda por nombre.")
    public ResponseEntity<ApiResult<PagedResult<ProfileListDTO>>> getPagedList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
        log.info("REST request to get all Profiles with query: {}", query);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        PagedResult<ProfileListDTO> response = profileService.findAllPaged(query, pageable);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping("/{publicId}/elements")
    @Operation(summary = "Asignar elementos a un perfil", description = "Asigna elementos a un perfil. Reemplaza las asignaciones anteriores.")
    public ResponseEntity<ApiResult<Void>> assignElements(
            @PathVariable("publicId") UUID publicId,
            @RequestBody @Valid AssignElementsRequest request) {
        profileService.assignElements(publicId, request);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}/elements-by-container")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener elementos agrupados por contenedor", description = "Obtiene todos los elementos disponibles agrupados por contenedor, indicando cuáles están asignados al perfil.")
    public ResponseEntity<ApiResult<ProfileElementsByContainerDTO>> getElementsByContainer(
            @PathVariable("publicId") UUID publicId) {
        ProfileElementsByContainerDTO response = profileService.getElementsByContainer(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}

