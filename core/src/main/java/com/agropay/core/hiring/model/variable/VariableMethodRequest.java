package com.agropay.core.hiring.model.variable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VariableMethodRequest(
    @NotNull(message = "El ID público del método es requerido")
    UUID methodPublicId,

    @Size(max = 100, message = "El valor no puede exceder 100 caracteres")
    String value,

    @NotNull(message = "El orden de ejecución es requerido")
    @Min(value = 1, message = "El orden de ejecución debe ser mayor que 0")
    Integer executionOrder
) {
}