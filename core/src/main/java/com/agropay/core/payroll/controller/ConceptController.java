package com.agropay.core.payroll.controller;

import com.agropay.core.payroll.model.concept.ConceptCategoryOptionDTO;
import com.agropay.core.payroll.model.concept.*;
import com.agropay.core.payroll.service.usecase.IConceptService;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/concepts")
@RequiredArgsConstructor
@Tag(name = "Concepts", description = "Catálogo de conceptos de planilla (sueldo básico, AFP, ONP, ESSALUD, bonos)")
public class ConceptController {

    private final IConceptService conceptService;

    @Operation(summary = "Obtener conceptos como opciones de select", description = "Lista todos los conceptos activos en formato para dropdowns (para asignar AFP/ONP a empleados)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente")
    })
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<ConceptSelectOptionDTO>>> getSelectOptions() {
        List<ConceptSelectOptionDTO> selectOptions = conceptService.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(selectOptions));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo concepto", description = "Registra un nuevo concepto de planilla en el sistema.")
    public ResponseEntity<ApiResult<CommandConceptResponse>> create(@RequestBody @Valid CreateConceptRequest request) {
        CommandConceptResponse response = conceptService.create(request);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @Operation(summary = "Actualizar un concepto existente", description = "Actualiza los datos de un concepto existente, identificado por su ID público.")
    public ResponseEntity<ApiResult<CommandConceptResponse>> update(
        @Parameter(description = "Identificador UUID del concepto.", required = true) @PathVariable("publicId") UUID publicId,
        @RequestBody @Valid UpdateConceptRequest request) {
        CommandConceptResponse response = conceptService.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar un concepto", description = "Elimina un concepto del sistema por su ID público. La operación fallará si el concepto está en uso en alguna configuración de planilla.")
    public ResponseEntity<ApiResult<Void>> delete(
        @Parameter(description = "Identificador UUID del concepto.", required = true) @PathVariable("publicId") UUID publicId) {
        conceptService.delete(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar un concepto por ID", description = "Obtiene los detalles completos de un concepto específico a través de su ID público.")
    public ResponseEntity<ApiResult<ConceptDetailsDTO>> findById(
        @Parameter(description = "Identificador UUID del concepto.", required = true) @PathVariable("publicId") UUID publicId) {
        ConceptDetailsDTO response = conceptService.getByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtener listado paginado de conceptos", description = "Devuelve una lista paginada de todos los conceptos. Permite la búsqueda por nombre/código, filtro por categoría y la ordenación por diferentes campos.")
    public ResponseEntity<ApiResult<PagedResult<ConceptListDTO>>> getPagedList(
        @Parameter(description = "Filtro opcional por nombre o código del concepto.") @RequestParam(required = false) String name,
        @Parameter(description = "Filtro opcional por categoría (publicId de la categoría).") @RequestParam(required = false) UUID categoryPublicId,
        @Parameter(description = "Número de página (0-indexed).") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página.") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Campo por el cual ordenar.") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Dirección de ordenamiento (ASC o DESC).") @RequestParam(defaultValue = "DESC") String sortDirection) {
        PagedResult<ConceptListDTO> response = conceptService.findAllPaged(name, categoryPublicId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/categories")
    @Operation(summary = "Obtener categorías de conceptos", description = "Lista todas las categorías de conceptos disponibles (INCOME, DEDUCTION, RETIREMENT, etc.)")
    public ResponseEntity<ApiResult<List<ConceptCategoryOptionDTO>>> getCategories() {
        List<ConceptCategoryOptionDTO> categories = conceptService.getCategories();
        return ResponseEntity.ok(ApiResult.success(categories));
    }

    @GetMapping("/select-options/by-category/{categoryCode}")
    @Operation(summary = "Obtener conceptos por categoría", description = "Lista conceptos filtrados por código de categoría (RETIREMENT, EMPLOYEE_CONTRIBUTION, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente")
    })
    public ResponseEntity<ApiResult<List<ConceptSelectOptionDTO>>> getSelectOptionsByCategory(
        @Parameter(description = "Código de la categoría (ej: RETIREMENT, EMPLOYEE_CONTRIBUTION)", required = true) 
        @PathVariable("categoryCode") String categoryCode) {
        List<ConceptSelectOptionDTO> selectOptions = conceptService.getSelectOptionsByCategoryCode(categoryCode);
        return ResponseEntity.ok(ApiResult.success(selectOptions));
    }
}