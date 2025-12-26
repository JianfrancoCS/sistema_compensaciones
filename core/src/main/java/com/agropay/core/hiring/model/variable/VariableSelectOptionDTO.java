package com.agropay.core.hiring.model.variable;

import java.util.UUID;

public record VariableSelectOptionDTO(
    UUID publicId,
    String code,
    String name,
    String defaultValue,
    boolean isRequired
) {}
