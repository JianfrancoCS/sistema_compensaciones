package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.ILoteUseCase;
import com.agropay.core.assignment.model.lote.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(LoteController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Endpoints para gestionar lotes de cultivo")
public class LoteController {

    public static final String BASE_URL = "/v1/lotes";

    private final ILoteUseCase loteUseCase;

    @PostMapping
    @Operation(summary = "Crear un nuevo lote")
    public ResponseEntity<ApiResult<CommandLoteResponse>> create(@Valid @RequestBody CreateLoteRequest request) {
        CommandLoteResponse response = loteUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(response));
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un lote existente")
    public ResponseEntity<ApiResult<CommandLoteResponse>> update(
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateLoteRequest request) {
        CommandLoteResponse response = loteUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un lote")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable UUID publicId) {
        loteUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener lista paginada de lotes con filtros")
    public ResponseEntity<ApiResult<PagedResult<LoteListDTO>>> findAllPaged(
            @Valid LotePageableRequest pageableRequest) {
        Pageable pageable = pageableRequest.toPageable();
        PagedResult<LoteListDTO> result = loteUseCase.findAllPaged(
                pageableRequest.getName(),
                pageableRequest.getSubsidiaryPublicId(),
                pageable
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @GetMapping("/sync")
    @Operation(summary = "Obtener todos los lotes activos para sincronización móvil (offline-first)")
    public ResponseEntity<ApiResult<List<LoteSyncResponse>>> sync() {
        List<LoteSyncResponse> lotes = loteUseCase.getAllForSync();
        return ResponseEntity.ok(ApiResult.success(lotes));
    }

    @GetMapping("/{publicId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener un lote por ID", description = "Obtiene los detalles de un lote específico por su ID público")
    public ResponseEntity<ApiResult<CommandLoteResponse>> findById(@PathVariable UUID publicId) {
        CommandLoteResponse response = loteUseCase.findById(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}