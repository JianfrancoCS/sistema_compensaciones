package com.agropay.core.assignment.model.labor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LaborListDTO(
        UUID publicId,
        String name,
        String description,
        BigDecimal minTaskRequirement,
        String laborUnitName,
        Boolean isPiecework,
        BigDecimal basePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}