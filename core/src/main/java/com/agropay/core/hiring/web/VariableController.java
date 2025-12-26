package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IVariableUseCase;
import com.agropay.core.hiring.model.variable.CommandVariableResponse;
import com.agropay.core.hiring.model.variable.CreateVariableRequest;
import com.agropay.core.hiring.model.variable.UpdateVariableRequest;
import com.agropay.core.hiring.model.variable.CreateVariableWithValidationRequest;
import com.agropay.core.hiring.model.variable.UpdateVariableWithValidationRequest;
import com.agropay.core.hiring.model.variable.VariableListDTO;
import com.agropay.core.hiring.model.variable.VariablePageableRequest;
import com.agropay.core.hiring.model.variable.VariableSelectOptionDTO;
import com.agropay.core.hiring.model.variable.AssociateMethodsRequest;
import com.agropay.core.hiring.model.variable.VariableWithValidationDTO;
import com.agropay.core.validation.application.usecase.IValidationMethodUseCase;
import com.agropay.core.validation.model.validationmethod.ValidationMethodSelectOptionDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(VariableController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Variables", description = "API for managing variables")
public class VariableController {

    public static final String BASE_URL = "/v1/hiring/variables";

    private final IVariableUseCase variableUseCase;
    private final IValidationMethodUseCase validationMethodUseCase;

    @Operation(summary = "Create a new variable")
    @PostMapping
    public ResponseEntity<ApiResult<CommandVariableResponse>> createVariable(
            @Valid @RequestBody CreateVariableRequest request) {
        log.info("REST request to create Variable: {}", request);
        CommandVariableResponse response = variableUseCase.create(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Update an existing variable")
    @PutMapping("/{publicId}")
    public ResponseEntity<ApiResult<CommandVariableResponse>> updateVariable(
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateVariableRequest request) {
        log.info("REST request to update Variable with publicId: {}, data: {}", publicId, request);
        CommandVariableResponse response = variableUseCase.update(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Delete a variable by public ID")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> deleteVariable(@PathVariable UUID publicId) {
        log.info("REST request to delete Variable with publicId: {}", publicId);
        variableUseCase.deleteByPublicId(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @Operation(summary = "Get variable select options")
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<VariableSelectOptionDTO>>> getVariableSelectOptions(
            @RequestParam(required = false) String name) {
        log.info("REST request to get Variable select options with name filter: '{}'", name);
        List<VariableSelectOptionDTO> options = variableUseCase.getSelectOptions(name);
        return ResponseEntity.ok(ApiResult.success(options));
    }

    @Operation(summary = "Get all variables with pagination and filters")
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<VariableListDTO>>> getAllVariables(
            @Valid @ModelAttribute VariablePageableRequest request) {
        log.info("REST request to get all Variables with filters: {}", request);
        PagedResult<VariableListDTO> result = variableUseCase.findAllPaged(
                request.getCode(),
                request.getName(),
                request.toPageable()
        );
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(summary = "Associate validation methods to a variable",
               description = "Associates multiple validation methods to an existing variable, creating dynamic validation rules")
    @PostMapping("/{publicId}/methods")
    public ResponseEntity<ApiResult<CommandVariableResponse>> associateMethods(
            @PathVariable UUID publicId,
            @Valid @RequestBody AssociateMethodsRequest request) {
        log.info("REST request to associate methods to Variable with publicId: {}, methods: {}", publicId, request);
        CommandVariableResponse response = variableUseCase.associateMethods(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Update validation methods of a variable",
               description = "Updates the validation methods associated with a variable and regenerates validation rules")
    @PutMapping("/{publicId}/methods")
    public ResponseEntity<ApiResult<CommandVariableResponse>> updateMethods(
            @PathVariable UUID publicId,
            @Valid @RequestBody AssociateMethodsRequest request) {
        log.info("REST request to update methods for Variable with publicId: {}, methods: {}", publicId, request);
        CommandVariableResponse response = variableUseCase.updateMethods(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Remove all validation methods from a variable",
               description = "Disassociates all validation methods from a variable, making it a simple variable again")
    @DeleteMapping("/{publicId}/methods")
    public ResponseEntity<ApiResult<Void>> disassociateMethods(@PathVariable UUID publicId) {
        log.info("REST request to disassociate methods from Variable with publicId: {}", publicId);
        variableUseCase.disassociateMethods(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @Operation(summary = "Get variable with its validation details",
               description = "Retrieves a variable along with its associated validation methods and final regex pattern")
    @GetMapping("/{publicId}/validation")
    public ResponseEntity<ApiResult<VariableWithValidationDTO>> getVariableWithValidation(@PathVariable UUID publicId) {
        log.info("REST request to get Variable with validation details for publicId: {}", publicId);
        VariableWithValidationDTO response = variableUseCase.getVariableWithValidation(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Get available validation methods",
               description = "Retrieves all available validation methods that can be associated with variables")
    @GetMapping("/validation-methods")
    public ResponseEntity<ApiResult<List<ValidationMethodSelectOptionDTO>>> getValidationMethods() {
        log.info("REST request to get available validation methods");
        List<ValidationMethodSelectOptionDTO> methods = validationMethodUseCase.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(methods));
    }

    @Operation(summary = "Create a new variable with optional validation",
               description = "Creates a variable with basic information and optionally associates validation methods in a single operation")
    @PostMapping("/with-validation")
    public ResponseEntity<ApiResult<CommandVariableResponse>> createVariableWithValidation(
            @Valid @RequestBody CreateVariableWithValidationRequest request) {
        log.info("REST request to create Variable with validation: {}", request);
        CommandVariableResponse response = variableUseCase.createWithValidation(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "Update a variable with optional validation",
               description = "Updates a variable with basic information and optionally manages validation methods in a single operation")
    @PutMapping("/{publicId}/with-validation")
    public ResponseEntity<ApiResult<CommandVariableResponse>> updateVariableWithValidation(
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateVariableWithValidationRequest request) {
        log.info("REST request to update Variable with validation for publicId: {}, data: {}", publicId, request);
        CommandVariableResponse response = variableUseCase.updateWithValidation(publicId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

}
