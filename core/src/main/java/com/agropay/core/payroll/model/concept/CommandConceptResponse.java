package com.agropay.core.payroll.model.concept;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommandConceptResponse(
    UUID publicId,
    String code,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

