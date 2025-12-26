package com.agropay.core.organization.web;

import com.agropay.core.organization.application.usecase.ISubsidiaryUseCase;
import com.agropay.core.organization.model.subsidiary.*;
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
@RequestMapping("/v1/subsidiaries")
@RequiredArgsConstructor
@Tag(name = "Gestión de Sucursales", description = "Operaciones para administrar las sucursales o sedes de la empresa.")
public class SubsidiaryController  {

    private final ISubsidiaryUseCase subsidiaryService;

    @PostMapping
    @Operation(summary = "Crear una nueva sucursal", description = "Registra una nueva sucursal o sede en el sistema.")
    public ResponseEntity<ApiResult<CommandSubsidiaryResponse>> create(@RequestBody @Valid CreateSubsidiaryRequest request) {
        CommandSubsidiaryResponse response = subsidiaryService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar una sucursal existente", description = "Actualiza los datos de una sucursal, identificada por su ID público.")
    public ResponseEntity<ApiResult<CommandSubsidiaryResponse>> update(@RequestBody @Valid UpdateSubsidiaryRequest request, @PathVariable("publicId") UUID publicId) {
        CommandSubsidiaryResponse response = subsidiaryService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar una sucursal", description = "Elimina una sucursal del sistema por su ID público. La operación fallará si la sucursal tiene empleados asociados.")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId) {
        subsidiaryService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar una sucursal por ID", description = "Obtiene los detalles completos de una sucursal específica a través de su ID público.")
    public ResponseEntity<ApiResult<SubsidiaryDetailsDTO>> findById(@PathVariable(name = "publicId") UUID publicId) {
        SubsidiaryDetailsDTO response = subsidiaryService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de sucursales", description = "Devuelve una lista paginada de todas las sucursales. Permite la búsqueda por nombre.")
    public ResponseEntity<ApiResult<PagedResult<SubsidiaryListDTO>>> getPagedList(
            @Valid @ModelAttribute SubsidiaryPageableRequest request) {
        PagedResult<SubsidiaryListDTO> response = subsidiaryService.findAllPaged(request.getName(), request.toPageable());
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/select-options")
    @Operation(summary = "Obtener sucursales para campos de selección", description = "Devuelve una lista simplificada de sucursales (ID y nombre) para ser utilizada en componentes de UI.")
    public ResponseEntity<ApiResult<List<SubsidiarySelectOptionDTO>>> getSelectOptions() {
        List<SubsidiarySelectOptionDTO> response = subsidiaryService.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/sync")
    @Operation(summary = "Obtener todas las sucursales para sincronización móvil")
    public ResponseEntity<ApiResult<List<SubsidiarySyncResponse>>> findAllForSynced(){
        List<SubsidiarySyncResponse> subsidiaries = subsidiaryService.findAllForSync();
        return ResponseEntity.ok(ApiResult.success(subsidiaries));
    }
}
