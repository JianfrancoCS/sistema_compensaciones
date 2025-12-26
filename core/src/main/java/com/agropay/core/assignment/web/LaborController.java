package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.ILaborUseCase;
import com.agropay.core.assignment.model.labor.*;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(LaborController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Gestión de Labores", description = "Endpoints para crear, actualizar, listar y eliminar las labores agrícolas.")
public class LaborController {

    public static final String BASE_URL = "/v1/labors";

    private final ILaborUseCase laborUseCase;

    @Operation(summary = "Crear una nueva labor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Labor creada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "La unidad de labor (LaborUnit) especificada no fue encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto: ya existe una labor con el mismo nombre.")
    })
    @PostMapping
    public ResponseEntity<ApiResult<CommandLaborResponse>> create(@Valid @RequestBody CreateLaborRequest request) {
        CommandLaborResponse response = laborUseCase.create(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Actualizar una labor existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Labor actualizada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "La labor o la unidad de labor especificada no fue encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto: ya existe otra labor con el mismo nombre.")
    })
    @PutMapping("/{publicId}")
    public ResponseEntity<ApiResult<CommandLaborResponse>> update(
            @Parameter(description = "Identificador UUID de la labor a actualizar.", required = true)
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateLaborRequest request) {
        CommandLaborResponse response = laborUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Eliminar una labor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Labor eliminada exitosamente."),
        @ApiResponse(responseCode = "404", description = "La labor con el UUID especificado no fue encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto de integridad: no se puede eliminar la labor porque está siendo usada en partes de trabajo (tareos)."),
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> delete(
            @Parameter(description = "Identificador UUID de la labor a eliminar.", required = true)
            @PathVariable UUID publicId) {
        laborUseCase.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @Operation(summary = "Obtener todas las labores como opciones de selección")
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<LaborSelectOptionDTO>>> getSelectOptions() {
        List<LaborSelectOptionDTO> options = laborUseCase.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(options));
    }

    @Operation(summary = "Obtener lista paginada de labores con filtros")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente."),
        @ApiResponse(responseCode = "400", description = "Parámetros de paginación o filtrado inválidos.")
    })
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<LaborListDTO>>> findAllPaged(
            @Valid LaborPageableRequest pageableRequest) {
        Pageable pageable = pageableRequest.toPageable();
        PagedResult<LaborListDTO> result = laborUseCase.findAllPaged(
                pageableRequest.getName(),
                pageableRequest.getIsPiecework(),
                pageableRequest.getLaborUnitPublicId(),
                pageable
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(summary = "Obtener todas las labores para sincronización móvil")
    @GetMapping("/sync")
    public ResponseEntity<ApiResult<List<LaborSyncResponse>>> findAllForSynced(){
        List<LaborSyncResponse> labors = laborUseCase.findAllForSync();
        return ResponseEntity.ok(ApiResult.success(labors));
    }

}
