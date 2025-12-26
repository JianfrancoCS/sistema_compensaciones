package com.agropay.core.hiring.model.variable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VariableValidationRequest(
    @Size(max = 500, message = "El mensaje de error no puede exceder 500 caracteres")
    String errorMessage,

    @Valid
    List<VariableMethodRequest> methods
) {
}