package com.agropay.core.assignment.model.laborunit;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommandLaborUnitResponse(
        UUID publicId,
        String name,
        String abbreviation,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}