package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.ITareoMotiveUseCase;
import com.agropay.core.assignment.model.tareomotive.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping(TareoMotiveController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Motivos de Tareo", description = "Endpoints para gestionar motivos de tareo (razones de ajuste de horas)")
public class TareoMotiveController {

    public static final String BASE_URL = "/v1/tareo-motives";

    private final ITareoMotiveUseCase tareoMotiveUseCase;

    @PostMapping
    @Operation(summary = "Crear un nuevo motivo de tareo")
    public ResponseEntity<ApiResult<CommandTareoMotiveResponse>> create(@Valid @RequestBody CreateTareoMotiveRequest request) {
        CommandTareoMotiveResponse response = tareoMotiveUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(response));
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un motivo de tareo existente")
    public ResponseEntity<ApiResult<CommandTareoMotiveResponse>> update(
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateTareoMotiveRequest request) {
        CommandTareoMotiveResponse response = tareoMotiveUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un motivo de tareo")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable UUID publicId) {
        tareoMotiveUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/select-options")
    @Operation(summary = "Obtener todos los motivos de tareo como opciones de selección")
    public ResponseEntity<ApiResult<List<TareoMotiveSelectOptionDTO>>> getSelectOptions() {
        List<TareoMotiveSelectOptionDTO> options = tareoMotiveUseCase.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(options));
    }

    @GetMapping
    @Operation(summary = "Obtener lista paginada de motivos de tareo con filtros")
    public ResponseEntity<ApiResult<PagedResult<TareoMotiveListDTO>>> findAllPaged(
            @Valid TareoMotivePageableRequest pageableRequest) {
        Pageable pageable = pageableRequest.toPageable();
        PagedResult<TareoMotiveListDTO> result = tareoMotiveUseCase.findAllPaged(
                pageableRequest.getName(),
                pageableRequest.getIsPaid(),
                pageable
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }


    @GetMapping("/sync")
    @Operation(summary = "Obtener todos los motivos de tareo para sincronización móvil")
    public ResponseEntity<ApiResult<List<TareoMotiveSyncReponse>>> findAllForSynced(){
        List<TareoMotiveSyncReponse> motives = tareoMotiveUseCase.findAllForSync();
        return ResponseEntity.ok(ApiResult.success(motives));
    }
}