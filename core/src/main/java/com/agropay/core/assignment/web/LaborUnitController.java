package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.ILaborUnitUseCase;
import com.agropay.core.assignment.model.laborunit.*;
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
@RequestMapping(LaborUnitController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Unidades de Labor", description = "Endpoints para gestionar unidades de medida de labores agrícolas")
public class LaborUnitController {

    public static final String BASE_URL = "/v1/labor-units";

    private final ILaborUnitUseCase laborUnitUseCase;

    @PostMapping
    @Operation(summary = "Crear una nueva unidad de labor")
    public ResponseEntity<ApiResult<CommandLaborUnitResponse>> create(@Valid @RequestBody CreateLaborUnitRequest request) {
        CommandLaborUnitResponse response = laborUnitUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(response));
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar una unidad de labor existente")
    public ResponseEntity<ApiResult<CommandLaborUnitResponse>> update(
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateLaborUnitRequest request) {
        CommandLaborUnitResponse response = laborUnitUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar una unidad de labor")
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable UUID publicId) {
        laborUnitUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/select-options")
    @Operation(summary = "Obtener todas las unidades de labor como opciones de selección")
    public ResponseEntity<ApiResult<List<LaborUnitSelectOptionDTO>>> getSelectOptions() {
        List<LaborUnitSelectOptionDTO> options = laborUnitUseCase.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(options));
    }

    @GetMapping
    @Operation(summary = "Obtener lista paginada de unidades de labor con filtros")
    public ResponseEntity<ApiResult<PagedResult<LaborUnitListDTO>>> findAllPaged(
            @Valid LaborUnitPageableRequest pageableRequest) {
        Pageable pageable = pageableRequest.toPageable();
        PagedResult<LaborUnitListDTO> result = laborUnitUseCase.findAllPaged(
                pageableRequest.getName(),
                pageableRequest.getAbbreviation(),
                pageable
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }
}