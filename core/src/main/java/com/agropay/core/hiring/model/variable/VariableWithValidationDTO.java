package com.agropay.core.hiring.model.variable;

import java.util.List;
import java.util.UUID;

public record VariableWithValidationDTO(
    UUID publicId,
    String code,
    String name,
    String defaultValue,
    String finalRegex,
    String errorMessage,
    List<AssociatedMethodDTO> methods
) {
}