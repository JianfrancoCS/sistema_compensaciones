package com.agropay.core.validation.model.validationmethod;

import java.util.UUID;

public record ValidationMethodSelectOptionDTO(
    UUID publicId,
    String name,
    String description,
    Boolean requiresValue,
    String methodType
) {
}