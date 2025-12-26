package com.agropay.core.hiring.model.variable;

import java.time.LocalDateTime;
import java.util.UUID;

public record VariableListDTO(
        UUID publicId,
        String code,
        String name,
        String defaultValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long validationMethodsCount
) {
}
