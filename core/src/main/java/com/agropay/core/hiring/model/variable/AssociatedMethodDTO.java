package com.agropay.core.hiring.model.variable;

import java.util.UUID;

public record AssociatedMethodDTO(
    UUID methodPublicId,
    String methodName,
    String methodDescription,
    Boolean requiresValue,
    String methodType,
    String value,
    Integer executionOrder
) {
}