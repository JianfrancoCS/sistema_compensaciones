package com.agropay.core.hiring.model.variable;

import java.util.UUID;

public record CommandVariableResponse(
    UUID publicId,
    String code,
    String name,
    String defaultValue
) {
}
