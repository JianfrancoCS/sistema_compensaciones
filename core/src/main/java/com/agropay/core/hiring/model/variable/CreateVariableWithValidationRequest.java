package com.agropay.core.hiring.model.variable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateVariableWithValidationRequest(
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "{validation.variable.code.pattern}")
    String code,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s]+$", message = "{validation.variable.name.pattern}")
    String name,

    @Size(max = 500, message = "El valor por defecto no puede exceder 500 caracteres")
    String defaultValue,

    @Valid
    VariableValidationRequest validation
) {
}