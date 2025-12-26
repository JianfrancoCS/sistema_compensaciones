package com.agropay.core.hiring.model.variable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssociateMethodsRequest(
    @Size(max = 500, message = "El mensaje de error no puede exceder 500 caracteres")
    String errorMessage,

    @NotEmpty(message = "Al menos un método es requerido")
    @Valid
    List<VariableMethodRequest> methods
) {
}